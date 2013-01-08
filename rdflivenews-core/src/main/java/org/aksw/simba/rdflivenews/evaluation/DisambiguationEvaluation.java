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
    private static Disambiguation disambiguation 	= null;
    private static Map<Integer,List<String>> entitiesCache = new HashMap<>();
    private static Map<String,String> evaluationResults = new LinkedHashMap<>();
	private static boolean headerWritten = false;
	private static Double stepSize = 0.1D;;
    
    public static void main(String[] args) throws IOException {
    	
    	nf.setMinimumFractionDigits(4);    	
        RdfLiveNews.init();
        
    	labelRefiner = new EntityLabelRefiner();
    	disambiguation = new FeatureBasedDisambiguation();
        
        List<DisambiguationEvaluationResult> results = new ArrayList<>();
        
        loadGoldStandard();
//        runEvaluation();
        debugEvaluation();

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
    	System.out.println("F1: " + getCost(writer, Arrays.asList(0.1, 0.2,	0.5, 0.2, 0.2)));
    	DEBUG_WRITER.close();
	}

	private static void loadGoldStandard() throws IOException {

        for (String line : FileUtils.readLines(new File(RdfLiveNews.DATA_DIRECTORY + "goldstandard/patterns_annotated.txt"))) {
            
            String[] lineParts = line.replace("______", "___ ___").split("___");
            if (lineParts[0].equals("NORMAL")) {
                
                ObjectPropertyTriple triple = new ObjectPropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[4], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
//                if ( GOLD_STANDARD_TRIPLES.containsKey(triple.getKey())) System.out.println(triple.getKey());
                GOLD_STANDARD_TRIPLES.put(triple.getKey(), triple);
            }
            else if (lineParts[0].equals("SAY")) {
                
                DatatypePropertyTriple triple = new DatatypePropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
                GOLD_STANDARD_SAY_TRIPLES.put(triple.getKey(), triple);
            }
            else throw new RuntimeException("WOWOWW: " + line);
        }
    }
	
	private static void runEvaluationGridSearch() throws UnsupportedEncodingException {
		
		Double globalMaxScore = 0D;
        List<Double> globalMaxSolution = null;
        RdfLiveNews.CONFIG.setStringSetting("refiner", "refineLabel", "ALL");
        DisambiguationEvaluation.stepSize = 0.1;
//        BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/tmp/"+stepSize+"-ALL.arff", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter writer1 = new BufferedFileWriter("/Users/gerb/tmp/scores.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        
		for ( double contextGlobal = 0D; contextGlobal <= 1 ; contextGlobal += DisambiguationEvaluation.stepSize) 
			for ( double contextLocal = 0D; contextLocal <= 1 ; contextLocal += DisambiguationEvaluation.stepSize)
				for ( double apriori = 0D; apriori <= 1 ; apriori += DisambiguationEvaluation.stepSize)
					for ( double stringsim = 0D; stringsim <= 1 ; stringsim += DisambiguationEvaluation.stepSize) 
	    				for ( double threshold = 0D; threshold <= 1 ; threshold += DisambiguationEvaluation.stepSize) {
	    					
	    					List<Double> paramters = Arrays.asList(contextGlobal, contextLocal, apriori, stringsim, threshold);
	    					
//	    					long s = System.currentTimeMillis();
	    					double score = getCost(writer1, paramters);
//	    					System.out.println(System.currentTimeMillis() - s);
	    					
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
    	
        for ( Double stepSize : Arrays.asList(/*0.01, 0.02, */0.05/*, 0.1*/) ) { DisambiguationEvaluation.stepSize = stepSize;
        	for ( String refinementType : Arrays.asList("PERSON"/*, "NONE", "ALL"*/)) { RdfLiveNews.CONFIG.setStringSetting("refiner", "refineLabel", refinementType);
            	for ( int randomIteration = 0; randomIteration < 100 ; randomIteration++) {
//            		for ( Boolean forceTyping : Arrays.asList(true, false)  ) {
    	
    	    			BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/tmp/"+stepSize+"-"+refinementType+"-"+randomIteration+".arff", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
    	    	
    	            	List<Double> initialSolution = new ArrayList<>(MathUtil.getFixedSetOfFixedNumbers(4, Double.class, 0, 1));
    	            	List<Double> currentSolution = initialSolution;
    	            	
    	            	Double currentFScore = getCost(writer, initialSolution);
    	            	int step = 1;
    	                while ( true ) {
    	                	
    	                	// try to replace each single component w/ its neighbors
    	                    Double highestFScore = currentFScore;
    	                    List<Double> highestSolution = currentSolution;
    	                    
    	                    System.out.println(String.format("Hill-climbing f-Score at step %s in iter: %s: %s", step, randomIteration, highestFScore));
    	                    List<List<Double>> neighbours = getNeighbours(currentSolution);
    	                    
    	                    for (List<Double> newSolution : neighbours ) {
    	                    	
    	                    	Double neighbourCost = getCost(writer, newSolution);
    	                        if ( neighbourCost > highestFScore ) {
    	
    	                        	highestFScore = neighbourCost;
    	                            highestSolution = newSolution;
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
        	}
        }
    	
    	System.out.println("Maximum f1: " + globalMaxScore);
    	System.out.println("With: " + globalMaxSolution);
    }
    
    private static List<List<Double>> getNeighbours(List<Double> solution) {
    	
    	double upperBound = 1D;
    	double lowerBound = 0D;
    	
    	List<List<Double>> neighbours = new ArrayList<>();
    	
    	for (int i = 0; i < solution.size(); i++){

    		List<Double> newNeighbour = new ArrayList<>(solution);
            if ( newNeighbour.get(i) < upperBound ) {
            	
            	double newParamterValue = newNeighbour.get(i) + stepSize ;
            	newNeighbour.set(i, newParamterValue < upperBound ? newParamterValue : 1);
            	neighbours.add(newNeighbour);
            }
            newNeighbour = new ArrayList<>(solution);
            if ( newNeighbour.get(i) > lowerBound ) {
            	
            	double newParamterValue = newNeighbour.get(i) - stepSize;
            	newNeighbour.set(i, newParamterValue > lowerBound ? newParamterValue : 0);
            	neighbours.add(newNeighbour);
            }
    	}
    	return neighbours;
    }
    
    private static double getCost(BufferedFileWriter writer, List<Double> paramters) throws UnsupportedEncodingException {
    	
		RdfLiveNews.CONFIG.setStringSetting("refiner", "contextGlobal", paramters.get(0) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "contextLocal", paramters.get(1) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "apriori", paramters.get(2) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "stringsim", paramters.get(3) + "");
		RdfLiveNews.CONFIG.setStringSetting("refiner", "urlScoreThreshold", paramters.get(4) + "");
    	
//    	long startIteration = System.currentTimeMillis();
    	double precision = 0D;
    	double recall = 0D;
    	double fScore = 0D;
    	
		int subjectCounter = 0;
		int objectCounter = 0;
		int correctSubjects = 0;
    	int correctObjects = 0;
    	
    	for ( Map.Entry<String, ObjectPropertyTriple> goldStandardEntry : GOLD_STANDARD_TRIPLES.entrySet()) {
    		
    		ObjectPropertyTriple goldTriple   = goldStandardEntry.getValue();
    		
    		String sUri = goldTriple.getSubjectUri();
    		String oUri = goldTriple.getObject();
    		
    		Integer sentenceId = goldTriple.getSentenceId().iterator().next();
    		if ( !entitiesCache.containsKey(sentenceId) ) entitiesCache.put(sentenceId, IndexManager.getInstance().getEntitiesFromArticle(sentenceId));
    		List<String> entities = entitiesCache.get(sentenceId);
    		
			String sLabel = goldTriple.getSubjectLabel();
			String oLabel = goldTriple.getObjectLabel();
			String sRefinedLabel = labelRefiner.refineLabel(sLabel, sentenceId);
			String oRefinedLabel = labelRefiner.refineLabel(oLabel, sentenceId);
			String foundSUri = disambiguation.getUri(sRefinedLabel, oRefinedLabel, entities);
    		String foundOUri = disambiguation.getUri(oRefinedLabel, sRefinedLabel, entities);
    		
			if ( !foundSUri.equals(Constants.NON_GOOD_URL_FOUND) ) {
				
				subjectCounter++;
				if ( sUri.equals(foundSUri) ) correctSubjects++;
			}
			
    		if ( !foundOUri.equals(Constants.NON_GOOD_URL_FOUND) ) {
    			
    			objectCounter++;
    			if (oUri.equals(foundOUri)) correctObjects++;
    			else {
					
//					List<String> done = new ArrayList<>(Arrays.asList("http://dbpedia.org/resource/National_Taiwan_University_Hospital", "http://dbpedia.org/resource/Félix_Hernández"));
					
//					if ( !done.contains(sUri) ) {

//						System.out.println("Gold: " + oUri + " - " + oLabel);
//						System.out.println("Found: " + foundOUri + " - " + oRefinedLabel);
//						System.out.println(IndexManager.getInstance().getStringValueFromDocument(sentenceId, Constants.LUCENE_FIELD_TEXT));
//						System.out.println();
//					}
				}
    		}
    	}
    	
    	// how many of the uri's we have found are correct
    	precision	= (double) (correctSubjects + correctObjects) / (subjectCounter + objectCounter);
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
    	
//    	writer.write(round(fScore) + "\t" + round(precision) + "\t"+  round(recall) + "\t" + StringUtils.join(paramters, "\t"));
//    	writer.flush();
    	return fScore;
    }
    
    private static void writeArffLine(BufferedFileWriter writer) {
    	
    	writer.write(StringUtils.join(evaluationResults.values(), ","));
    }
    
    private static void writeArffHeader(BufferedFileWriter writer) {
    	
    	if ( !headerWritten  ) {
    		
    		writer.write("@relation AprioriAndContextAndStringAndLabelRefinementParameter");
    		writer.write("");
    		
    		for ( String key : evaluationResults.keySet() ) {
    			
    			if ( !key.equals("refineLabel") ) 
    				writer.write("@attribute " + key + " numeric");
    			else
    				writer.write("@attribute " + key + " {ALL,NONE,PERSON}");
    		}
    		
    		writer.write("");
    		writer.write("@data");
    		
    		headerWritten = true;
    	}
    	writer.flush();
	}

	public static String round(double d) {
    	
    	return df.format(Math.floor(d * 100000) / 100000.0);
    }
	
	public static String round(String d) {
    	
    	return df.format(Math.floor(Double.valueOf(d) * 100000) / 100000.0);
    }
    
    
    public static void findGlobalMaximaForAprioriScore(){
    
    	LuceneDbpediaManager m = new LuceneDbpediaManager();
    	try {
			System.out.println(m.findMaximumAprioriScore());
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
