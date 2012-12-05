/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.ClusterLabeler;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.BorderFlowPatternClustering;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer;
import org.aksw.simba.rdflivenews.pattern.scoring.impl.OccurrencePatternScorer;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.SimilarityGenerator;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.impl.DefaultSimilarityGenerator;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramAndWordnetSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.WordnetSimilarityMetric;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.DefaultRdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.SimpleRdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.DatatypePropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.time.TimeUtil;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class TripleExtractionEvaluation {

    /**
     * normal triples are basically triples with owl:ObjectProperties
     */
    private static Map<String,Triple> GOLD_STANDARD_TRIPLES = new HashMap<String,Triple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,Triple> GOLD_STANDARD_SAY_TRIPLES = new HashMap<String,Triple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,Triple> EXTRACTED_TRIPLES = new HashMap<String,Triple>();
    
    public static void main(String[] args) throws IOException {
     
        loadGoldStandard();
        loadExtraction();
        runEvaluation();
    }


    private static void runEvaluation() {

        int foundTriples = 0;
        
        int correctSubjectUris = 0, correctObjectUris = 0, correctSubjectAndObjectUris = 0;
        
        for ( Map.Entry<String, Triple> goldStandardEntry : GOLD_STANDARD_TRIPLES.entrySet() ) {
            
            Triple goldTriple   = goldStandardEntry.getValue();
            Triple newTriple    = EXTRACTED_TRIPLES.get(goldStandardEntry.getKey());
            
            if ( newTriple != null ) {
                
                foundTriples++;
                if ( newTriple.getSubjectUri().equals(goldTriple.getSubjectUri())) correctSubjectUris++;
                if ( newTriple.getObject().equals(goldTriple.getObject())) correctObjectUris++;
                if ( newTriple.getSubjectUri().equals(goldTriple.getSubjectUri()) &&
                        newTriple.getObject().equals(goldTriple.getObject())) correctSubjectAndObjectUris++;
            }
            else {
                
                System.out.println(goldTriple.getKey());
            }
        }
        
        float subjectPrecision          = correctSubjectUris / (float) foundTriples;
        float objectPrecision           = correctObjectUris / (float) foundTriples;
        float subjectAndObjectPrecision = correctSubjectAndObjectUris / (float) foundTriples;
        
        float subjectRecall             = correctSubjectUris / (float) GOLD_STANDARD_TRIPLES.size();
        float objectRecall              = correctObjectUris / (float) GOLD_STANDARD_TRIPLES.size();
        float subjectAndObjectRecall    = correctSubjectAndObjectUris / (float) GOLD_STANDARD_TRIPLES.size();
        
        System.out.println("Subject-Precision: " + subjectPrecision);
        System.out.println("Object-Precision: " + objectPrecision);
        System.out.println("Subject-Object-Precision: " + subjectAndObjectPrecision);
        
        System.out.println("Subject-Recall: " + subjectRecall);
        System.out.println("Object-Recall: " + objectRecall);
        System.out.println("Subject-Object-Recall: " + subjectAndObjectRecall);
        
        double subjectUriFMeasure           = (2D * subjectPrecision * subjectRecall) / (subjectPrecision + subjectRecall);  
        double objectUriFMeasure            = (2D * objectPrecision * objectRecall) / (objectPrecision + objectRecall);
        double subjectAndObjectUriFMeasure  = (2D * subjectAndObjectPrecision * subjectAndObjectRecall) / (subjectAndObjectPrecision + subjectAndObjectRecall);
        
        System.out.println("Subject-F-Measure: " + subjectUriFMeasure);
        System.out.println("Object-F-Measure: " + objectUriFMeasure);
        System.out.println("Subject-Object-F-Measure: " + subjectAndObjectUriFMeasure);
    }


    private static void loadGoldStandard() throws IOException {

        for (String line : FileUtils.readLines(new File("/Users/gerb/test/patterns_annotated.txt"))) {
            
            String[] lineParts = line.replace("______", "___ ___").split("___");
            if (lineParts[0].equals("NORMAL")) {
                
                ObjectPropertyTriple triple = new ObjectPropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[4], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
//                if ( GOLD_STANDARD_TRIPLES.containsKey(triple.getKey())) System.out.println(triple.getKey());
                GOLD_STANDARD_TRIPLES.put(triple.getKey(), triple);
            }
            else if (lineParts[0].equals("SAY")) {
                
                DatatypePropertyTriple triple = new DatatypePropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[5], lineParts[7]);
                GOLD_STANDARD_SAY_TRIPLES.put(triple.getKey(), triple);
            }
            else throw new RuntimeException("WOWOWW: " + line);
        }
    }

    private static void loadExtraction() throws InvalidFileFormatException, IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
        // we need this to be an instance variable because we need to save the similarities which we computed for each iteration
        SimilarityGenerator similarityGenerator = new DefaultSimilarityGenerator(
                (SimilarityMetric) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity")));
        
//        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        
//        Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
//        deduplication.runDeduplication(0, 37, 37);
        
        Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentences();
        System.out.print("Starting pattern search in "+currentNonDuplicateSentenceIds.size()+" sentences ...  ");
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds), RdfLiveNews.CONFIG.getIntegerSetting("search", "number-of-threads"));
        System.out.println("DONE");

        // filter the patterns and merge the old and the new patterns
        PatternFilter patternFilter = new DefaultPatternFilter();
        patternsOfIteration         = patternFilter.filter(patternSearchManager.mergeNewFoundPatterns(patternsOfIteration));
        
        // refines the domain and range of the patterns 
        PatternRefiner patternRefiner = (PatternRefiner) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "refiner"));
        patternRefiner.refinePatterns(patternsOfIteration);
        
        // scores the pattern according to certain features
        PatternScorer patternScorer = new OccurrencePatternScorer();
        patternScorer.scorePatterns(patternsOfIteration);
        
        Set<Similarity> similarities = similarityGenerator.calculateSimilarities(patternsOfIteration);
        
        // tries to group similar patterns into the same cluster
        PatternClustering patternClustering = new BorderFlowPatternClustering();
        Set<Cluster<Pattern>> clusters = patternClustering.clusterPatterns(similarities, RdfLiveNews.CONFIG.getDoubleSetting("clustering", "similarityThreshold"));
        
        // we need to name the property a single cluster stands for
        ClusterLabeler clusterLabeler = new DefaultClusterLabeling();
        clusterLabeler.labelCluster(clusters);
        
        // use the patterns to extract rdf from news text
        RdfExtraction rdfExtractor = new SimpleRdfExtraction();
        for ( Triple triple : rdfExtractor.extractRdf(clusters)) EXTRACTED_TRIPLES.put(triple.getKey(), triple);
    }
}
