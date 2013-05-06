/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
	private Map<String,List<String>> uriLabelCandidatesCache = new HashMap<>();
	private Map<String,Double> aprioriScoreCache = new HashMap<>();
	
	public Set<String> possibleSubjectUris = null;
	public Set<String> possibleObjectUris = null;
	
	 DecimalFormat df = new DecimalFormat("#.###");
	private Map<String,String> uriToLabels = new HashMap<String,String>();
	private Map<Set<String>,List<DisambiguationEntity>> urisToPossibleEntities = new HashMap<Set<String>,List<DisambiguationEntity>>();
	private static final String URI_CACHE_DIR = RdfLiveNews.DATA_DIRECTORY + "evaluation/uris.ser";
	private static final String CONTEXT_CACHE_DIR = RdfLiveNews.DATA_DIRECTORY + "evaluation/context.ser";
	private static final String APRIORI_CACHE_DIR = RdfLiveNews.DATA_DIRECTORY + "evaluation/apriori.ser";
	

    public FeatureBasedDisambiguation(LuceneDbpediaManager luceneDbpediaManager) {
    	this.dbpediaManager = luceneDbpediaManager;
    	
		try {
			
			if ( new File(URI_CACHE_DIR).exists() )
				this.uriCandidatesCache = (Map<String, List<String>>) SerializationUtils.deserialize(new FileInputStream(new File(URI_CACHE_DIR)));
			
			System.out.println("Loading of uri cache done.");
			
			if ( new File(CONTEXT_CACHE_DIR).exists() )
				this.contextEntityCache = (Map<String, Map<String, Integer>>) SerializationUtils.deserialize(new FileInputStream(new File(CONTEXT_CACHE_DIR)));
			
			System.out.println("Loading of context cache done.");
			
			if ( new File(APRIORI_CACHE_DIR).exists() )
				this.aprioriScoreCache = (Map<String, Double>) SerializationUtils.deserialize(new FileInputStream(new File(APRIORI_CACHE_DIR)));
			
			System.out.println("Loading of apriori cache done.");
		}	
		catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    	}
	}
    
    public void serializeCache() {
    	
    	try {
    		
    		System.out.println("Starting to write cache files .. DON'T QUIT!");
    		
//    		if ( !new File(URI_CACHE_DIR).exists() )
    			SerializationUtils.serialize((Serializable) this.uriCandidatesCache, new FileOutputStream(new File(URI_CACHE_DIR)));
//    		if ( !new File(CONTEXT_CACHE_DIR).exists() )
    			SerializationUtils.serialize((Serializable) this.contextEntityCache, new FileOutputStream(new File(CONTEXT_CACHE_DIR)));
//    		if ( !new File(APRIORI_CACHE_DIR).exists() )
    			SerializationUtils.serialize((Serializable) this.aprioriScoreCache, new FileOutputStream(new File(APRIORI_CACHE_DIR)));
    			
    		System.out.println("Done writing cache files.");
		}
    	catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/**
     * Check for URIs that contain entry as substring
     *
     * @param label Label of entity
     * @return List of mapping URIs. Score is 1 (perfect match)
     */
    public List<String> getUriSurfaceFormCandidates(String label) {

    	if ( !this.uriCandidatesCache.containsKey(label) ) this.uriCandidatesCache.put(label,dbpediaManager.getUriForSurfaceForm(label));
    	return this.uriCandidatesCache.get(label);
    }
    
    public List<String> getUriLabelCandidates(String label) {

    	if ( !this.uriLabelCandidatesCache.containsKey(label) ) this.uriLabelCandidatesCache.put(label,dbpediaManager.getUriForLabel(label));
    	return this.uriLabelCandidatesCache.get(label);
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
    public String getUri(String label, String refinedLabel, String secondEntity, List<String> contextEntitiesInArticle, boolean isSubject) {
    	
//    	Set<String> uris = new HashSet<String>(getUriCandidates(StringUtils.countMatches(label, ".") > 1 ? label.replace(".", "") : label));
//    	Set<String> uris = new HashSet<String>(getUriSurfaceFormCandidates(refinedLabel.replaceAll(" [A-Z]\\. ", " ")));
//    	uris.addAll(getUriSurfaceFormCandidates(StringUtils.countMatches(refinedLabel, ".") > 1 ? refinedLabel.replace(".", "") : refinedLabel));
//    	uris.addAll(getUriSurfaceFormCandidates(refinedLabel.replaceAll(" [A-z]+ ", " ")));
//    	uris.addAll(getUriSurfaceFormCandidates(label));
//    	uris.addAll(getUriLabelCandidates(label));
//    	
    	long start = System.currentTimeMillis();
    	Set<String> uris = new HashSet<String>(getUriSurfaceFormCandidates(refinedLabel));
    	uris.addAll(getUriSurfaceFormCandidates(label));
    	String[] parts = refinedLabel.split(" ");
    	if ( parts.length == 3 ) {
//    		uris.addAll(getUriLabelCandidates(parts[0] + " " + parts[2]));
    		uris.addAll(getUriSurfaceFormCandidates(parts[0] + " " + parts[2]));
    	}
//    	System.out.println("SurfaceForms: " + (System.currentTimeMillis() -start));
    	
//    	uris.addAll(getUriLabelCandidates(refinedLabel));
//    	uris.addAll(getUriLabelCandidates(label));
    	
    	if ( isSubject ) this.possibleSubjectUris = uris;
    	else this.possibleObjectUris = uris;
    	
        // if we dont find uri candidates, we need to generate our own based on dbpedia style
        if (uris.isEmpty()) 
        	// cahnge this to NO URL FOUND
            return Constants.NON_GOOD_URL_FOUND;
            		//Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX + Encoder.urlEncode(label.replace(" ", "_"), Encoding.UTF_8);
        else {
            
        	if ( !this.urisToPossibleEntities.containsKey(uris) ) {
        		
        		List<DisambiguationEntity> possibleEntities = new ArrayList<DisambiguationEntity>();
                
                for (String u : uris) {
                	
                	start = System.currentTimeMillis();
                	double[] contextScore	= getContextScore(u, secondEntity, contextEntitiesInArticle);
//                	System.out.println("Context: " + (System.currentTimeMillis() -start));
                	
                	DisambiguationEntity entity = new DisambiguationEntity();
                	entity.uri = u;
                	start = System.currentTimeMillis();
                	entity.apriori = getAprioriScore(u);
//                	System.out.println("Apriori: " + (System.currentTimeMillis() -start));
                	entity.contextLocal = contextScore[1];
                	entity.contextGlobal = contextScore[0];
                	start = System.currentTimeMillis();
                	entity.stringsim = getStringSimilarityScore(label, u);
//                	System.out.println("StringSim: " + (System.currentTimeMillis() -start));
                	
                	possibleEntities.add(entity);
                }
                
        		this.urisToPossibleEntities.put(uris, possibleEntities);
        	}
            
            double max = 0, score;
            String uri = "";
            
        	DescriptiveStatistics apriori = new DescriptiveStatistics();
        	DescriptiveStatistics local = new DescriptiveStatistics();
        	DescriptiveStatistics global = new DescriptiveStatistics();
        	DescriptiveStatistics stringsim = new DescriptiveStatistics();
        	Frequency scoreDistribution = new Frequency();
            
            for ( DisambiguationEntity entity : this.urisToPossibleEntities.get(uris) ) {
            	
            	Double apri = entity.apriori * RdfLiveNews.CONFIG.getDoubleSetting("refiner", "apriori");
            	Double loc  = entity.contextLocal * RdfLiveNews.CONFIG.getDoubleSetting("refiner", "contextLocal");
            	Double glob = entity.contextGlobal * RdfLiveNews.CONFIG.getDoubleSetting("refiner", "contextGlobal");
            	Double str  = entity.stringsim * RdfLiveNews.CONFIG.getDoubleSetting("refiner", "stringsim");
            	
            	apriori.addValue(apri);
            	local.addValue(loc);
            	global.addValue(glob);
            	stringsim.addValue(str);
            	
            	Double contextGlobal = (glob - global.getMin()) / (global.getMax() - global.getMin());
            	Double contextLocal = (loc - local.getMin()) / (local.getMax() - local.getMin());
            	Double aprioriScore = (apri - apriori.getMin()) / (apriori.getMax() - apriori.getMin());
            	Double stringsimScore = (str - stringsim.getMin()) / (stringsim.getMax() - stringsim.getMin());
            	
            	contextGlobal	= contextGlobal.isNaN() || contextGlobal.isInfinite() ? 0 : contextGlobal;
            	aprioriScore	= aprioriScore.isNaN() || aprioriScore.isInfinite() ? 0 : aprioriScore;
            	stringsimScore	= stringsimScore.isNaN() || stringsimScore.isInfinite() ? 0 : stringsimScore;
            	contextLocal	= contextLocal.isNaN() || contextLocal.isInfinite() ? 0 : contextLocal;
            	
            	score = (aprioriScore + contextGlobal + stringsimScore + contextLocal) / 4;
            	
//            	System.out.println(score +" " + entity.uri + "\t"+  apriori + "\t" + contextGlobal + "\t" + stringsim + "\t" + contextLocal + "\n");
//            	scoreDistribution.addValue(String.format("%.2f", score));
//            	System.out.println(scoreDistribution);
            	
                if (score >= max) {
                	
                    max = score;
                    uri = entity.uri;
                }
            }
            
//            System.out.println(this.apriori);
//            System.out.println("------------------------");
//            System.out.println(this.stringsim);
//            System.out.println("------------------------");
//            System.out.println(this.local);
//            System.out.println("------------------------");
//            System.out.println(this.global);
            
            if ( max < RdfLiveNews.CONFIG.getDoubleSetting("refiner", "urlScoreThreshold") ) return Constants.NON_GOOD_URL_FOUND;
            return uri;
        }
    }
    
    private double getStringSimilarityScore(String label, String uri) {
		
    	AbstractStringMetric metric = new QGramsDistance();
    	double max = 0D;
    	
//    	System.out.println(uri);
    	
//    	for ( String surfaceForm : this.dbpediaManager.getSurfaceFormsForUri(uri)) {
//    		
//    		System.out.println("\t" + surfaceForm);
//    		
//    		double sim = metric.getSimilarity(label, surfaceForm);
//    		if (label.equals("New York Giants") ) System.out.println("\t" + label + " " + surfaceForm + " " +sim);
//    		max = Math.max(max, sim);
//    	}
    	
    	if ( !this.uriToLabels.containsKey(uri) ) this.uriToLabels .put(uri, this.dbpediaManager.getLabelForUri(uri));
    	max = metric.getSimilarity(label, this.uriToLabels.get(uri));
    	
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
		
		return getUri(label, label, secondEntity, new ArrayList<String>(), true);
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
        System.out.println(uriRetrieval.getUri("Pitt", "Pitt", "", Arrays.asList("Fight Club", "Angelina Jolie"), true));
        System.out.println(uriRetrieval.getUri("Pitt", "Pitt".toLowerCase(), "", Arrays.asList("Fight Club", "Angelina Jolie"), true));
        System.out.println(uriRetrieval.getUri("Pitt", "Brad Pitt", "", Arrays.asList("Fight Club", "Angelina Jolie"), true));
        System.out.println(uriRetrieval.getUri("Pitt", "Brad Pitt".toLowerCase(), "", Arrays.asList("Fight Club", "Angelina Jolie"), true));
        
//        Shaquille O’Neal
//        Shaquille O'Neal
    }
}
