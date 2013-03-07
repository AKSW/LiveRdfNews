/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneBoaManager;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneDbpediaManager;
import org.aksw.simba.rdflivenews.rdf.uri.Disambiguation;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.InvalidFileFormatException;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.github.gerbsen.encoding.Encoder;
import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.math.Frequency;

/**
 *
 * @author ngonga
 */
public class FeatureBasedDisambiguation implements Disambiguation {

    public static Logger logger = java.util.logging.Logger.getLogger(FeatureBasedDisambiguation.class.getName());
    public LuceneDbpediaManager dbpediaManager;
    public LuceneBoaManager boaManager = new LuceneBoaManager();
	private Map<String,Map<String,Integer>> contextEntityCache = new HashMap<>();
	private Map<String,List<String>> uriCandidatesCache = new HashMap<>();
	private Map<String,Double> aprioriScoreCache = new HashMap<>();
	
	public Frequency score = new Frequency();
	public Frequency apriori = new Frequency();
	public Frequency local = new Frequency();
	public Frequency global = new Frequency();
	public Frequency stringsim = new Frequency();
	
	 DecimalFormat df = new DecimalFormat("#.###");

    public FeatureBasedDisambiguation(LuceneDbpediaManager luceneDbpediaManager) {
    	this.dbpediaManager = luceneDbpediaManager;
	}

	/**
     * Check for URIs that contain entry as substring
     *
     * @param label Label of entity
     * @return List of mapping URIs. Score is 1 (perfect match)
     */
    public List<String> getUriCandidates(String label) {

    	if ( !this.uriCandidatesCache.containsKey(label) ) this.uriCandidatesCache.put(label,dbpediaManager.getUriForSurfaceForm(label));
    	return this.uriCandidatesCache.get(label);
    }

    /**
     * Computes to a-priori score based on a single uri
     *
     * @param uri URI of the resource
     * @return A-priori score
     */
    public double getAprioriScore(String uri) {
    	
    	if ( !aprioriScoreCache.containsKey(uri) ) this.aprioriScoreCache.put(uri, dbpediaManager.getAprioriScoreForUri(uri)); 
    	return this.aprioriScoreCache.get(uri);
    }

    @Override
    public String getUri(String label, String secondEntity, List<String> contextEntitiesInArticle) {
    	
    	List<String> uris = getUriCandidates(StringUtils.countMatches(label, ".") > 1 ? label.replace(".", "") : label);
    	
//    	for ( String s : uris )System.out.println(label + " " + s);
    	
        // if we dont find uri candidates, we need to generate our own based on dbpedia style
        if (uris.isEmpty()) 
            return Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX + Encoder.urlEncode(label.replace(" ", "_"), Encoding.UTF_8);
        else {
            
            Map<String,List<Double>> urlsToScores = new HashMap<>();
            double contextGlobalMax = 0D;
            double contextLocalMax = 0D;
            double aprioriMax = 0D;
            
            for (String u : uris) {
            	
            	double[] contextScore	= getContextScore(u, secondEntity, contextEntitiesInArticle);
            	double aprioriScore		= getAprioriScore(u);
            	urlsToScores.put(u, Arrays.asList(contextScore[0], contextScore[1], aprioriScore, getStringSimilarityScore(label, u)));
            	contextGlobalMax = Math.max(contextGlobalMax, contextScore[0]);
            	contextLocalMax = Math.max(contextLocalMax, contextScore[1]);
            	aprioriMax = Math.max(aprioriMax, aprioriScore);
            }
            	
            double max = 0, score;
            String uri = "";
            
            for ( Map.Entry<String, List<Double>> scoreEntry : urlsToScores.entrySet() ) {
            	
            	Double contextGlobal = (RdfLiveNews.CONFIG.getDoubleSetting("refiner", "contextGlobal") * (scoreEntry.getValue().get(0) / contextGlobalMax));
            	Double contextLocal = (RdfLiveNews.CONFIG.getDoubleSetting("refiner", "contextLocal") * (scoreEntry.getValue().get(1) / contextLocalMax));
            	Double apriori = (RdfLiveNews.CONFIG.getDoubleSetting("refiner", "apriori") * (scoreEntry.getValue().get(2) / aprioriMax));
            	Double stringsim = (RdfLiveNews.CONFIG.getDoubleSetting("refiner", "stringsim") * (scoreEntry.getValue().get(3)));
            	
//            	System.out.println("Global: " + contextGlobal + " Local:"+ contextLocal +" Apriori:" + apriori + " Stringsim:" + stringsim);
            	
            	contextGlobal = contextGlobal.isNaN() || contextGlobal.isInfinite() ? 0 : contextGlobal;
            	apriori = apriori.isNaN() || apriori.isInfinite() ? 0 : apriori;
            	stringsim = stringsim.isNaN() || stringsim.isInfinite() ? 0 : stringsim;
            	contextLocal = contextLocal.isNaN() || contextLocal.isInfinite() ? 0 : contextLocal;
            	
            	score = (apriori + contextGlobal + stringsim + contextLocal) / 4;
            	
            	this.apriori.addValue(df.format(apriori));
            	this.local.addValue(df.format(contextLocal));
            	this.global.addValue(df.format(contextGlobal));
            	this.stringsim.addValue(df.format(stringsim));
            	this.score.addValue(df.format(score));
            	
//            	if ( score > 1 ) System.out.println(
//            			RdfLiveNews.CONFIG.getDoubleSetting("refiner", "apriori") + "*" + apriori +  "\t" +
//            			RdfLiveNews.CONFIG.getDoubleSetting("refiner", "context") + "*" + context  + "\t "+ 
//            			RdfLiveNews.CONFIG.getDoubleSetting("refiner", "stringsim") + "*" + stringsim + "\t");
                
                if (score >= max) {
                	
//                	System.out.println(label + "  " +  uri +  "    " + score);
                	
                    max = score;
                    uri = scoreEntry.getKey();
                }
            }
            
            if ( max < RdfLiveNews.CONFIG.getDoubleSetting("refiner", "urlScoreThreshold") ) return Constants.NON_GOOD_URL_FOUND;
            return uri;
        }
    }
    
    private double getStringSimilarityScore(String label, String uri) {
		
    	AbstractStringMetric metric = new QGramsDistance();
    	double max = 0D;
    	for ( String surfaceForm : this.dbpediaManager.getSurfaceFormsForUri(uri)) {
    		
    		double sim = metric.getSimilarity(label, surfaceForm);
//    		System.out.println(label + " " + surfaceForm + " " +sim);
    		max = Math.max(max, sim);
    	}
//    	System.out.println(max);
		return max;
	}

	private double[] getContextScore(String uriCandidate, String secondEntity, List<String> contextEntitiesInArticle) {

    	if ( !this.contextEntityCache.containsKey(uriCandidate) ) 
    		this.contextEntityCache.put(uriCandidate, boaManager.getContextNamedEntities(uriCandidate));
    	
    	Set<String> contextInArticle = new HashSet<String>(contextEntitiesInArticle);
    	return new double[]{
    			getJaccardSimilarity(contextEntityCache.get(uriCandidate).keySet(), contextInArticle), 
    			this.contextEntityCache.get(uriCandidate).containsKey(secondEntity) ? this.contextEntityCache.get(uriCandidate).get(secondEntity) : 0 };
	}
    
	/**
     * 
     * @param setOne
     * @param setTwo
     * @return
     */
    public static double getJaccardSimilarity(Set<String> setOne, Set<String> setTwo) {
        
        Set<String> copyOfSetOne = new HashSet<String>(setOne);
        copyOfSetOne.retainAll(setTwo);
        double z = (double) copyOfSetOne.size();
        double jaccard = z / (setOne.size() + setTwo.size() - z);
        
//        System.out.println(setOne.size() + " " + setTwo.size());
//        System.out.println(jaccard);
        return jaccard;         
    }
    
    @Override
	public String getUri(String label, String secondEntity) {
		
		return getUri(label, secondEntity, new ArrayList<String>());
	}
    
	public static void main(String[] args) throws InvalidFileFormatException, IOException {

        RdfLiveNews.init();
        
        FeatureBasedDisambiguation uriRetrieval = new FeatureBasedDisambiguation(new LuceneDbpediaManager());
//        System.out.println(uriRetrieval.getUri("Paul Ryan", Arrays.asList("Mitt Romney")));
//        System.out.println(uriRetrieval.getUri("Ryan", Arrays.asList("Mitt Romney")));
//        System.out.println(uriRetrieval.getUri("D.C.", Arrays.asList("Family Research Council", "D.C.", "Cathy Lanier", "The Washington Examiner", "FBI")));
//        System.out.println(uriRetrieval.getUri("D.C.".toLowerCase(), Arrays.asList("Family Research Council", "D.C.", "Cathy Lanier", "The Washington Examiner", "FBI")));
//        System.out.println(uriRetrieval.getUri("Washington, D.C.", Arrays.asList("Family Research Council", "D.C.", "Cathy Lanier", "The Washington Examiner", "FBI")));
//        System.out.println(uriRetrieval.getUri("Washington, D.C.".toLowerCase(), Arrays.asList("Family Research Council", "D.C.", "Cathy Lanier", "The Washington Examiner", "FBI")));
        System.out.println(uriRetrieval.getUri("Pitt", "", Arrays.asList("Fight Club", "Angelina Jolie")));
        System.out.println(uriRetrieval.getUri("Pitt".toLowerCase(), "", Arrays.asList("Fight Club", "Angelina Jolie")));
        System.out.println(uriRetrieval.getUri("Brad Pitt", "", Arrays.asList("Fight Club", "Angelina Jolie")));
        System.out.println(uriRetrieval.getUri("Brad Pitt".toLowerCase(), "", Arrays.asList("Fight Club", "Angelina Jolie")));
        
//        Shaquille O’Neal
//        Shaquille O'Neal
    }
}
