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

import org.aksw.simba.rdflivenews.Constants;
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
import org.aksw.simba.rdflivenews.pattern.comparator.PatternSupportSetComparator;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefinementManager;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer;
import org.aksw.simba.rdflivenews.pattern.scoring.impl.OccurrencePatternScorer;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.SimilarityGenerator;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.impl.SimilarityGeneratorManager;
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

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.time.TimeUtil;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class DisambiguationEvaluation {

    /**
     * normal triples are basically triples with owl:ObjectProperties
     */
    private static Map<String,ObjectPropertyTriple> GOLD_STANDARD_TRIPLES = new HashMap<String,ObjectPropertyTriple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,Triple> GOLD_STANDARD_SAY_TRIPLES = new HashMap<String,Triple>();
    /**
     * say triples have strings as values
     */
    private static Map<String,ObjectPropertyTriple> EXTRACTED_TRIPLES = new HashMap<String,ObjectPropertyTriple>();
    
    public static void main(String[] args) throws IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
        List<DisambiguationEvaluationResult> results = new ArrayList<>();
        
        for ( Boolean forceTyping : Arrays.asList(/*true, */false)  ) {
            for ( String refinementType : Arrays.asList("PERSON"/*, "ALL", "NONE"*/)) {

                DisambiguationEvaluationResult result = new DisambiguationEvaluationResult();
                result.addConfigOption("Enforce Types of Cluster", forceTyping.toString());
                RdfLiveNews.CONFIG.setStringSetting("extraction", "enforceCorrectTypes", forceTyping.toString());
                result.addConfigOption("Entity Label Refinement", refinementType);
                RdfLiveNews.CONFIG.setStringSetting("refiner", "refineLabel", refinementType);
                
                loadGoldStandard();
                loadExtraction();
                runEvaluation(result);
                results.add(result);
                
                System.out.println(result);
            }
        }

        Collections.sort(results);
        BufferedFileWriter writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/disambiguation.evaluation", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (DisambiguationEvaluationResult sortedResult : results) writer.write(sortedResult.toString());
        writer.close();
        
        BufferedFileWriter normalTripleWriter = new BufferedFileWriter("/Users/gerb/test/normal_extracted.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for ( Map.Entry<String, ObjectPropertyTriple> entry: EXTRACTED_TRIPLES.entrySet()) {
            normalTripleWriter.write(entry.getKey());
        }
        normalTripleWriter.close();
    }


    private static void runEvaluation(DisambiguationEvaluationResult result) {

        int foundTriples = 0;
        
        int correctSubjectUris = 0, correctObjectUris = 0, correctSubjectAndObjectUris = 0;
        
        for ( Map.Entry<String, ObjectPropertyTriple> goldStandardEntry : GOLD_STANDARD_TRIPLES.entrySet() ) {
            
            ObjectPropertyTriple goldTriple   = goldStandardEntry.getValue();
            ObjectPropertyTriple newTriple    = EXTRACTED_TRIPLES.get(goldStandardEntry.getKey());
            
            if ( newTriple != null ) {
                
                foundTriples++;
                if ( newTriple.getSubjectUri().equals(goldTriple.getSubjectUri())) correctSubjectUris++;
                else { 
                    
                    System.out.println("Extraction-"+newTriple.getRefinedSubjectLabel()+": " + newTriple.getSubjectUriPrefixed() + " --GS:-- " + ((ObjectPropertyTriple) goldTriple).getSubjectUriPrefixed() + " -- " + goldTriple.getSentenceId());
                    System.out.println(IndexManager.getInstance().getStringValueFromDocument(newTriple.getSentenceId().iterator().next(), Constants.LUCENE_FIELD_TEXT));
                    System.out.println();
                }
                if ( newTriple.getObject().equals(goldTriple.getObject())) correctObjectUris++;
                else { 
//                    System.out.println("Extraction-"+newTriple.getRefinedObjectLabel()+": " + newTriple.getObjectUriPrefixed() + " --GS:-- " + goldTriple.getObjectUriPrefixed() + " -- " + goldTriple.getSentenceId());
//                    System.out.println(IndexManager.getInstance().getStringValueFromDocument(newTriple.getSentenceId().iterator().next(), Constants.LUCENE_FIELD_TEXT));
                }
                if ( newTriple.getSubjectUri().equals(goldTriple.getSubjectUri()) &&
                        newTriple.getObject().equals(goldTriple.getObject())) correctSubjectAndObjectUris++;
            }
            else {
                
                System.out.println(goldTriple.getKey());
            }
        }
        
        result.setSubjectPrecision(correctSubjectUris / (float) foundTriples);
        result.setObjectPrecision(correctObjectUris / (float) foundTriples);
        result.setSubjectAndObjectPrecision(correctSubjectAndObjectUris / (float) foundTriples);
        
        result.setSubjectRecall(correctSubjectUris / (float) GOLD_STANDARD_TRIPLES.size());
        result.setObjectRecall(correctObjectUris / (float) GOLD_STANDARD_TRIPLES.size());
        result.setSubjectAndObjectRecall(correctSubjectAndObjectUris / (float) GOLD_STANDARD_TRIPLES.size());
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
                
                DatatypePropertyTriple triple = new DatatypePropertyTriple(lineParts[1], lineParts[2], lineParts[3], lineParts[5], new HashSet<Integer>(Arrays.asList(Integer.valueOf(lineParts[7]))));
                GOLD_STANDARD_SAY_TRIPLES.put(triple.getKey(), triple);
            }
            else throw new RuntimeException("WOWOWW: " + line);
        }
    }

    private static void loadExtraction() throws InvalidFileFormatException, IOException {

        // we need this to be an instance variable because we need to save the similarities which we computed for each iteration
        SimilarityGeneratorManager similarityGenerator = new SimilarityGeneratorManager(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity"));
        
//        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
//        
//        Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
//        deduplication.runDeduplication(0, 37, 37);
        
        Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentences();
        System.out.print("Starting pattern search in "+currentNonDuplicateSentenceIds.size()+" sentences ...  ");
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds));
        System.out.println("DONE");

        // filter the patterns and merge the old and the new patterns
        PatternFilter patternFilter = new DefaultPatternFilter();
        patternsOfIteration         = patternFilter.filter(patternSearchManager.mergeNewFoundPatterns(patternsOfIteration));
        
        Collections.sort(patternsOfIteration, new PatternSupportSetComparator());
        patternSearchManager.logPatterns(patternsOfIteration);
        List<Pattern> top1PercentPattern = patternsOfIteration.size() > 100000 ? patternsOfIteration.subList(0, 1000) : patternsOfIteration.subList(0, patternsOfIteration.size() / 100);

        // refines the domain and range of the patterns
        PatternRefinementManager refinementManager = new PatternRefinementManager();
        refinementManager.startPatternRefinement(top1PercentPattern);
        
        Set<Similarity> similarities = similarityGenerator.startSimilarityGeneratorThreads(top1PercentPattern, Collections.synchronizedSet(new HashSet<Similarity>()));
        
        // tries to group similar patterns into the same cluster
        PatternClustering patternClustering = new BorderFlowPatternClustering();
        Set<Cluster<Pattern>> clusters = patternClustering.clusterPatterns(similarities, RdfLiveNews.CONFIG.getDoubleSetting("clustering", "similarityThreshold"));
        
        // we need to name the property a single cluster stands for
        ClusterLabeler clusterLabeler = new DefaultClusterLabeling();
        clusterLabeler.labelCluster(clusters);
        
        // use the patterns to extract rdf from news text
        RdfExtraction rdfExtractor = new SimpleRdfExtraction();
        for ( Triple triple : rdfExtractor.extractRdf(clusters)) {
            for ( Integer id : triple.getSentenceId()) {
                if ( triple instanceof ObjectPropertyTriple )
                    EXTRACTED_TRIPLES.put(triple.getSubjectLabel() + " " + triple.getPatternLabel() + " " + ((ObjectPropertyTriple) triple).getObjectLabel()+ " " + id, (ObjectPropertyTriple) triple);
            }
        }
    }
}
