/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pattern.refinement.label.impl.EntityLabelRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneDbpediaManager;
import org.aksw.simba.rdflivenews.rdf.triple.DatatypePropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.aksw.simba.rdflivenews.rdf.uri.Disambiguation;
import org.aksw.simba.rdflivenews.rdf.uri.impl.FeatureBasedDisambiguation;
import org.apache.commons.io.FileUtils;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.math.MathUtil;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class DisambiguationEvaluation {
	
	public static BufferedFileWriter DEBUG_WRITER = new BufferedFileWriter("/Users/gerb/tmp/debug.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE); 

    /**
     * normal triples are basically triples with owl:ObjectProperties
     */
    public static Map<String,ObjectPropertyTriple> GOLD_STANDARD_TRIPLES = new HashMap<String,ObjectPropertyTriple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,Triple> GOLD_STANDARD_SAY_TRIPLES = new HashMap<String,Triple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,ObjectPropertyTriple> EXTRACTED_TRIPLES = new HashMap<String,ObjectPropertyTriple>();
    
    private static NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private static DecimalFormat df = (DecimalFormat)nf;
    
    private static EntityLabelRefiner labelRefiner = null;
    private static FeatureBasedDisambiguation disambiguation 	= null;
    private static Map<Integer,List<String>> entitiesCache = new HashMap<>();
	private static Double stepSize = 0.1D;

	private static Double precision;
	private static Double recall;
	private static Double fScore;;
    
    public static void main(String[] args) throws IOException {

    	nf.setMinimumFractionDigits(4);    	
        RdfLiveNews.init();
        
    	labelRefiner = new EntityLabelRefiner();
    	disambiguation = new FeatureBasedDisambiguation(new LuceneDbpediaManager());
        
        List<DisambiguationEvaluationResult> results = new ArrayList<>();
        
        loadGoldStandard();
//        runEvaluationGridSearch();
        runEvaluationHillClimbing();
//        debugEvaluation();

        Collections.sort(results);
        BufferedFileWriter writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/disambiguation.evaluation", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (DisambiguationEvaluationResult sortedResult : results) writer.write(sortedResult.toString());
        writer.close();
        
        BufferedFileWriter normalTripleWriter = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "goldstandard/normal_extracted.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for ( Map.Entry<String, ObjectPropertyTriple> entry: EXTRACTED_TRIPLES.entrySet()) {
            normalTripleWriter.write(entry.getKey());
        }
        normalTripleWriter.close();
    }

    private static void debugEvaluation() throws UnsupportedEncodingException {
    	
    	BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/tmp/test.arff", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
//    	runEvaluationGridSearch();
//    	System.out.println("F1: " + getCost(writer, Arrays.asList(0.1, 0.2,	0.5, 0.2, 0.2)));

    	System.out.println("Starting debugging ... ");
    	
    	for ( double j = 0 ; j <= 1 ; j += 0.1) {
			
    		// 0,782	0,173	0,377	0,315	0,700 => 0.4 ==> 0.6175637393767706
    		// 0.020853487824139205, 0.9896237217433842, 0.0, 0.35362723051474054
			List<Double> newNeighbour = new ArrayList<>(Arrays.asList(0.020853487824139205, 0.9896237217433842, 0.0, 0.35362723051474054, 0.0));
			newNeighbour.set(4, j);

			getCost(writer, newNeighbour);
			if ( j == 0) {
				disambiguation.serializeCache();
				labelRefiner.serializeCache();
			}
			
			System.out.println("Testing parameters: " + String.format("\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f", newNeighbour.toArray()));
			System.out.println(String.format("Precision: %.3f    Recall: %.3f    F1: %.3f\n", precision, recall, fScore));
		}
    	
//    	System.out.println("F1: " + getCost(writer, Arrays.asList(0.4497828394133033, 0.7768306002641023,	1D, 0.5968259229692493, 0.6106314582518256)));
    	DEBUG_WRITER.close();
	}

	private static void loadGoldStandard() throws IOException {

		Set<String> nlrs = new HashSet<>();
		
        for (String line : FileUtils.readLines(new File(RdfLiveNews.DATA_DIRECTORY + "goldstandard/patterns_annotated_gold.txt"))) {
//        for (String line : FileUtils.readLines(new File(RdfLiveNews.DATA_DIRECTORY + "goldstandard/patterns_annotated_gold_small.txt"))) {
            
            String[] lineParts = line.replace("______", "___ ___").split("___");
            if (lineParts[0].equals("NORMAL")) {
                
                ObjectPropertyTriple triple = new ObjectPropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[4], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
//                if ( GOLD_STANDARD_TRIPLES.containsKey(triple.getKey())) System.out.println(triple.getKey());
                nlrs.add(lineParts[3]);
                
//                if ( triple.getSubjectUri().startsWith("http://rdf") && StringUtils.isAlphanumeric(StringUtils.stripNonAlphaNumerics(triple.getSubjectUri())) ) continue;
//                if ( triple.getObject().startsWith("http://rdf") && StringUtils.isAlphanumeric(StringUtils.stripNonAlphaNumerics(triple.getObject())) ) continue;
                GOLD_STANDARD_TRIPLES.put(triple.getKey(), triple);
                
                if ( triple.getSubjectUri().startsWith("http://rdf") ) triple.setSubjectUri(Constants.NON_GOOD_URL_FOUND);
                if ( triple.getObject().startsWith("http://rdf") ) triple.setObject(Constants.NON_GOOD_URL_FOUND);
            }
            else if (lineParts[0].equals("SAY")) {
                
                DatatypePropertyTriple triple = new DatatypePropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
                GOLD_STANDARD_SAY_TRIPLES.put(triple.getKey(), triple);
            }
            else throw new RuntimeException("WOWOWW: " + line);
        }
//        System.out.println(StringUtils.join(nlrs, "\n"));
//        System.exit(0);
    }
	
	private static void runEvaluationGridSearch() throws UnsupportedEncodingException {
		
		Double globalMaxScore = 0D;
        List<Double> globalMaxSolution = null;
        RdfLiveNews.CONFIG.setStringSetting("refiner", "refineLabel", "ALL");
        DisambiguationEvaluation.stepSize = 0.1;
//        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/tmp/"+stepSize+"-ALL.arff", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter writer1 = new BufferedFileWriter("/Users/gerb/tmp/scores_new_test.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        
		for ( double contextGlobal = 0D; contextGlobal <= 1 ; contextGlobal += DisambiguationEvaluation.stepSize) 
			for ( double contextLocal = 0D; contextLocal <= 1 ; contextLocal += DisambiguationEvaluation.stepSize)
				for ( double apriori = 0D; apriori <= 1 ; apriori += DisambiguationEvaluation.stepSize)
					for ( double stringsim = 0D; stringsim <= 1 ; stringsim += DisambiguationEvaluation.stepSize) 
	    				for ( double threshold = 0.15D; threshold <= 0.25 ; threshold += 0.01) {
	    					
	    					List<Double> paramters = Arrays.asList(contextGlobal, contextLocal, apriori, stringsim, threshold);
//	    					List<Double> paramters = Arrays.asList(0.1,0.2,0.5,0.2, threshold);
	    					
	    					long s = System.currentTimeMillis();
	    					double score = getCost(writer1, paramters);
	    					
	    					if ( score > globalMaxScore ) {
	    	                	
	    	                	globalMaxScore = score;
	    	                	globalMaxSolution = paramters;
	    	                }
//	    					writer1.flush();
	    				}
        
        System.out.println(globalMaxScore + ": " + globalMaxSolution);
        writer1.close();
//        writer.close();
	}
	
	private static void runEvaluationHillClimbing() throws UnsupportedEncodingException {

    	Double globalMaxScore = 0D;
        List<Double> globalMaxSolution = null;
    	
//        for ( Double stepSize : Arrays.asList(0.01, 0.02, 0.05, 0.1) ) { DisambiguationEvaluation.stepSize = stepSize;
//        	for ( String refinementType : Arrays.asList("PERSON"/*, "NONE", "ALL"*/)) { RdfLiveNews.CONFIG.setStringSetting("refiner", "refineLabel", refinementType);
            	for ( int randomIteration = 0; randomIteration < 1_000 ; randomIteration++) {
//            		for ( Boolean forceTyping : Arrays.asList(true, false)  ) {
    	
    	    			BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/tmp/hill.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
    	    	
    	    			List<Double> randomList = MathUtil.getFixedSetOfRandomNumbers(5, Double.class, 0, 1);
    	    			
    	            	List<Double> initialSolution = new ArrayList<>(randomList);
    	            	List<Double> currentSolution = initialSolution;

    	            	Double currentFScore = getCost(writer, initialSolution);
    	            	if ( randomIteration == 0 ) {
    	            		disambiguation.serializeCache();
        	            	labelRefiner.serializeCache();
    	            	}
    	            	
    	            	String output = "\tTesting parameters: " + String.format("\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f", initialSolution.toArray()) + " => " + currentFScore + " ==> "+ globalMaxScore;
                    	System.out.println(output);
    	            	
    	            	int step = 1;
    	                while ( true ) {
    	                	
    	                	// try to replace each single component w/ its neighbors
    	                    Double highestFScore = currentFScore;
    	                    List<Double> highestSolution = currentSolution;
    	                    
    	                    System.out.println(String.format("Hill-climbing f-Score in %s iteration at step %s: %s", randomIteration, step, highestFScore));
    	                    List<List<Double>> neighbours = getNeighbours(currentSolution);
    	                    
    	                    for (List<Double> newSolution : neighbours ) {
    	                    	
    	                    	Double neighbourCost = getCost(writer, newSolution);
    	                    	
    	                    	output = "\tTesting parameters: " + String.format("\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f", newSolution.toArray()) + " => " + neighbourCost + " ==> "+ globalMaxScore;
    	                    	System.out.println(output);
    	                    	writer.write(output);
    	                    	
    	                        if ( neighbourCost > highestFScore ) {
    	
    	                        	highestFScore = neighbourCost;
    	                            highestSolution = newSolution;
    	                            
//    	                            currentSolution = highestSolution;
//    	                            break;
    	                        }
    	                    }
    	                        
    	                    if ( highestFScore <= currentFScore ) break;
    	                    else {
    	                    	
    	                    	currentSolution = highestSolution;
    	            			currentFScore = highestFScore;
    	                        step++;
    	                    }
    	                }
    	                
    	                if ( currentFScore > globalMaxScore ) {
    	                	
    	                	globalMaxScore = currentFScore;
    	                	globalMaxSolution = currentSolution;
    	                }
    	                
    	                writer.close();
    	            }
//        		}
//        	}
//        }
    	
    	System.out.println("Maximum f1: " + globalMaxScore);
    	System.out.println("With: " + globalMaxSolution);
    }
    
    private static List<List<Double>> getNeighbours(List<Double> solution) {
    	
    	double upperBound = 1D;
    	double lowerBound = 0D;
    	double jumpSize	  = 0.1D;
    	
    	List<List<Double>> neighbours = new ArrayList<>();
    	
//    	solution.remove(4);
    	for (int i = 0; i < solution.size(); i++){
    		
//    		if ( i < 4 ) {
    		
    			List<Double> newNeighbour = new ArrayList<>(solution);
                if ( newNeighbour.get(i) < upperBound ) {
                	
                	double newParamterValue = newNeighbour.get(i) + stepSize ;
                	for ( double j = newNeighbour.get(i) ; j <= newParamterValue; j += jumpSize) {
                		
                		newNeighbour = new ArrayList<>(solution);
                		newNeighbour.set(i, j > lowerBound ? j : 0);
                    	neighbours.add(newNeighbour);
                	}
                }
                newNeighbour = new ArrayList<>(solution);
                if ( newNeighbour.get(i) > lowerBound ) {
                	
                	double newParamterValue = newNeighbour.get(i) - stepSize;
                	for ( double j = newNeighbour.get(i) ; j >= newParamterValue; j -= jumpSize) {
                		
                		newNeighbour = new ArrayList<>(solution);
                		newNeighbour.set(i, j > lowerBound ? j : 0);
                    	neighbours.add(newNeighbour);
                	}
                }
//    		}
//    		else {
//    			
//    			// we test all thresholds in stepsize
//    			for ( double j = 0 ; j <= 1 ; j += 0.1) {
//    				
//    				List<Double> newNeighbour = new ArrayList<>(solution);
//    				newNeighbour.set(i, j);
//                	neighbours.add(newNeighbour);
//    			}
    			
//    			List<Double> newNeighbour = new ArrayList<>(solution);
//                if ( newNeighbour.get(i) < upperBound ) {
//                	
//                	double newParamterValue = newNeighbour.get(i) + 0.1 ;
//                	newNeighbour.set(i, newParamterValue < upperBound ? newParamterValue : 1);
//                	neighbours.add(newNeighbour);
//                }
//                newNeighbour = new ArrayList<>(solution);
//                if ( newNeighbour.get(i) > lowerBound ) {
//                	
//                	double newParamterValue = newNeighbour.get(i) - 0.1;
//                	newNeighbour.set(i, newParamterValue > lowerBound ? newParamterValue : 0);
//                	neighbours.add(newNeighbour);
//                }    			
//    		}
    	}
    	
//    	List<List<Double>> newPars = new ArrayList<List<Double>>();
//    	
//    	for ( List<Double> neighbor : neighbours ) {
//    		for ( double i = 0D; i <= 1 ; i += 0.1) {
//    			
//    			List<Double> x = new ArrayList<Double>(neighbor);
//    			x.add(i);
//    			newPars.add(x);
//    		}
//    	}
    	
    	return neighbours;
    }
    
    private static double getCost(BufferedFileWriter writer, List<Double> paramters) throws UnsupportedEncodingException {
    	
		RdfLiveNews.CONFIG.setStringSetting("refiner", "contextGlobal", paramters.get(0) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "contextLocal", paramters.get(1) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "apriori", paramters.get(2) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "stringsim", paramters.get(3) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "urlScoreThreshold", paramters.get(4) + "");
    	
//    	long startIteration = System.currentTimeMillis();
		int subjectCounter = 0;
		int objectCounter = 0;
		int correctSubjects = 0;
    	int correctObjects = 0;
    	
    	int dbpediaUriButRlnFound = 0;
    	int rnlUriButDbpediaFound = 0;
    	int nonUriFound = 0;
    	int missingRlnUri = 0;
    	int missingDbpediaUri = 0;
    	int rlnUris = 0;
		int dbpediaUris = 0;
		
		int i = 1;
		int evaluations = 0;
		int possibleSubjectHits = 0;
		int possibleObjectHits = 0;
		
		int correctDbpediaObjects = 0;
		int correctDbpediaSubjects = 0;
    	
    	for ( Map.Entry<String, ObjectPropertyTriple> goldStandardEntry : GOLD_STANDARD_TRIPLES.entrySet()) {
    		
//    		System.out.println(i++ + "/" + GOLD_STANDARD_TRIPLES.size());
    		i++;
    		
    		ObjectPropertyTriple goldTriple   = goldStandardEntry.getValue();
    		
    		String sUri = goldTriple.getSubjectUri();
    		String oUri = goldTriple.getObject();
    		
    		if ( sUri.startsWith("http://dbpedia") ) dbpediaUris++;
    		if ( oUri.startsWith("http://dbpedia") ) dbpediaUris++;
    		if ( sUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) rlnUris++;
    		if ( oUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) rlnUris++;
    		
    		Integer sentenceId = goldTriple.getSentenceId().iterator().next();
    		if ( !entitiesCache.containsKey(sentenceId) ) entitiesCache.put(sentenceId, IndexManager.getInstance().getEntitiesFromArticle(sentenceId));
    		List<String> entities = entitiesCache.get(sentenceId);
    		
			String sLabel = goldTriple.getSubjectLabel();
			String oLabel = goldTriple.getObjectLabel();
			
			long start = System.currentTimeMillis();
			String sRefinedLabel = labelRefiner.refineLabel(sLabel, sentenceId);
//			System.out.println("sLabel: " + (System.currentTimeMillis() - start));
			
			start = System.currentTimeMillis();
			String oRefinedLabel = labelRefiner.refineLabel(oLabel, sentenceId);
//			System.out.println("oLabel: " + (System.currentTimeMillis() - start));

			start = System.currentTimeMillis();
			String foundSUri = disambiguation.getUri(sLabel, sRefinedLabel, oRefinedLabel, entities, true);
//			System.out.println("sUri: " + (System.currentTimeMillis() - start) + " " + sUri);
			
			start = System.currentTimeMillis();
    		String foundOUri = disambiguation.getUri(oLabel, oRefinedLabel, sRefinedLabel, entities, false);
//    		System.out.println("oUri: " + (System.currentTimeMillis() - start) + " " + oUri);
    		
    		if ( disambiguation.possibleSubjectUris.contains(sUri) && !sUri.equals(Constants.NON_GOOD_URL_FOUND) ) possibleSubjectHits++;
//    		else if (!sUri.equals(Constants.NON_GOOD_URL_FOUND)) System.out.println(sLabel + " -- " + sRefinedLabel + " == " + sUri + " == " + foundSUri);
    		if ( disambiguation.possibleObjectUris.contains(oUri) && !oUri.equals(Constants.NON_GOOD_URL_FOUND) ) possibleObjectHits++;
//    		else if (!oUri.equals(Constants.NON_GOOD_URL_FOUND)) System.out.println(oLabel + " -- " + oRefinedLabel + " == " + oUri + " == " + foundOUri);
    		
			if ( !sUri.equals(Constants.NON_GOOD_URL_FOUND) ) evaluations++;
    		if ( !oUri.equals(Constants.NON_GOOD_URL_FOUND) ) evaluations++;
    		
    		if ( sUri.equals(foundSUri) ) correctSubjects++; 
    		else if (foundSUri.equals(Constants.NON_GOOD_URL_FOUND) && sUri.startsWith("http://db") ) subjectCounter++;
    		
    		if ( oUri.equals(foundOUri) ) correctObjects++; 
    		else if (foundOUri.equals(Constants.NON_GOOD_URL_FOUND) && oUri.startsWith("http://db") ) objectCounter++;
    	}
    	
//    	System.out.println("nonUriFound: " +nonUriFound);
//    	System.out.println("rnlUriButDbpediaFound: " + rnlUriButDbpediaFound);
//    	System.out.println("dbpediaUriButRlnFound: " + dbpediaUriButRlnFound);
//    	System.out.println("missingDbpediaUri: " + missingDbpediaUri);
//    	System.out.println("missingRlnUri: " + missingRlnUri);
//    	System.out.println("rlnUris: " + rlnUris);
//    	System.out.println("dbpediaUris: " + dbpediaUris);
    	
//    	System.out.println(String.format("CS: %s/%s CO: %s/%s NA: %s", correctSubjects, subjectCounter, correctObjects, objectCounter, nonUriFound));
    	
//    	System.out.println("correctDbpediaSubjects: " + correctDbpediaSubjects + "/" + dbpediaUris);
//    	System.out.println("correctDbpediaObjects: " + correctDbpediaObjects + "/" + dbpediaUris);
    	
//    	System.out.println("PSH: " +  possibleSubjectHits + " POH: " + possibleObjectHits + " TOTAL: " + evaluations + " RATIO: " + ( (possibleSubjectHits+possibleObjectHits) / (double)evaluations));
    	
    	// how many of the uri's we have found are correct
    	precision	= (double) (correctSubjects + correctObjects) / (GOLD_STANDARD_TRIPLES.size() * 2);
    	recall		= (double) (correctSubjects + correctObjects) / (GOLD_STANDARD_TRIPLES.size() * 2);
    	fScore		= (2 * precision * recall ) / (precision + recall);
    	
//    	evaluationResults.put("time", (System.currentTimeMillis() - startIteration ) + "");
//    	evaluationResults.put("apriori", round(RdfLiveNews.CONFIG.getStringSetting("refiner", "apriori")));
//    	evaluationResults.put("contextLocal", round(RdfLiveNews.CONFIG.getStringSetting("refiner", "contextLocal")));
//    	evaluationResults.put("contextGlobal", round(RdfLiveNews.CONFIG.getStringSetting("refiner", "contextLocal")));
//    	evaluationResults.put("stringsim", round(RdfLiveNews.CONFIG.getStringSetting("refiner", "stringsim")));
//    	evaluationResults.put("urlScoreThreshold", round(RdfLiveNews.CONFIG.getStringSetting("refiner", "urlScoreThreshold")));
//    	evaluationResults.put("refineLabel", RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel"));
//    	evaluationResults.put("foundUris", correctSubjects + correctObjects + "");
//    	evaluationResults.put("totalUris", subjectCounter + objectCounter + "");
//    	evaluationResults.put("precision", round(precision));
//    	evaluationResults.put("recall", round(recall));
//    	evaluationResults.put("fscore", round(fScore));
    	
//    	writeArffHeader(writer);
//    	writeArffLine(writer);
    	
//    	System.out.println("Precision: " + precision);
//    	System.out.println("Recall: " + recall);
    	
    	writer.write(round(fScore) + "\t" + round(precision) + "\t"+  round(recall) + "\t" + StringUtils.join(paramters, "\t"));
    	writer.flush();
//    	return fScore;
    	return precision;
    }
    
	public static String round(double d) {
    	
    	return df.format(Math.floor(d * 100000) / 100000.0);
    }
	
	public static String round(String d) {
    	
    	return df.format(Math.floor(Double.valueOf(d) * 100000) / 100000.0);
    }
	
//	
//	if ( !foundSUri.equals(Constants.NON_GOOD_URL_FOUND) ) {
//		
//		if ( foundSUri.startsWith("http://dbpedia") && sUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
//			
//			rnlUriButDbpediaFound++;
//			System.out.println("" + foundSUri + "\t\t" + sUri);
//		}
//		if ( foundSUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) && sUri.startsWith("http://dbpedia") ) {
//			
//			System.out.println("" + foundSUri + "\t\t" + sUri);
//			dbpediaUriButRlnFound++;
//		}
//		
//		subjectCounter++;
//		if ( sUri.equals(foundSUri) ) {
//			
//			correctSubjects++;
//			if ( foundSUri.startsWith("http://dbpedia") ) correctDbpediaSubjects++;
//			if ( foundOUri.startsWith("http://dbpedia") ) correctDbpediaObjects++;
//		}
//		else {
//			
//			System.out.println("Gold: " + sUri + " - " + sLabel);
//			System.out.println("Found: " + foundSUri + " - " + sRefinedLabel);
//			System.out.println(IndexManager.getInstance().getStringValueFromDocument(sentenceId, Constants.LUCENE_FIELD_TEXT));
//			System.out.println();
//		}
//	}
//	else {
//		
//		nonUriFound++;
//		if ( sUri.startsWith("http://dbpedia") ) missingDbpediaUri++;
//		if ( sUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) missingRlnUri++;
//	}
//	
//	if ( !foundOUri.equals(Constants.NON_GOOD_URL_FOUND) ) {
//		
//		if ( foundOUri.startsWith("http://dbpedia") && oUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
//			
//			System.out.println("" + foundOUri + "\t\t" + oUri);
//			rnlUriButDbpediaFound++;
//		}
//		if ( foundOUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) && oUri.startsWith("http://dbpedia") ) {
//			
//			dbpediaUriButRlnFound++;
//			System.out.println("" + foundOUri + "\t\t" + oUri);
//		}
//		
//		
//		objectCounter++;
//		if (oUri.equals(foundOUri)) correctObjects++;
//		else {
//			
//			List<String> done = new ArrayList<>(Arrays.asList("http://dbpedia.org/resource/National_Taiwan_University_Hospital", "http://dbpedia.org/resource/Félix_Hernández"));
//			
//			if ( !done.contains(sUri) ) {
//
//				System.out.println("Gold: " + oUri + " - " + oLabel);
//				System.out.println("Found: " + foundOUri + " - " + oRefinedLabel);
//				System.out.println(IndexManager.getInstance().getStringValueFromDocument(sentenceId, Constants.LUCENE_FIELD_TEXT));
//				System.out.println();
//			}
//		}
//	}
//	else {
//		
//		nonUriFound++;
//		if ( sUri.startsWith("http://dbpedia") ) missingDbpediaUri++;
//		if ( sUri.startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) missingRlnUri++;
//	}
//}
	
	
}
