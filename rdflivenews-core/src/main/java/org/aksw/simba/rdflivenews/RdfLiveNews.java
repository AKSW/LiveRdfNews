/**
 * 
 */
package org.aksw.simba.rdflivenews;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.ClusterLabeler;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.nlp.NaturalLanguageTagger;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.BorderFlowPatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.DefaultPatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.ClusterMerger;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.DefaultClusterMerger;
import org.aksw.simba.rdflivenews.pattern.comparator.PatternOccurrenceComparator;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.mapping.DbpediaMapper;
import org.aksw.simba.rdflivenews.pattern.mapping.impl.DefaultDbpediaMapper;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.impl.DefaultPatternRefiner;
import org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer;
import org.aksw.simba.rdflivenews.pattern.scoring.impl.OccurrencePatternScorer;
import org.aksw.simba.rdflivenews.pattern.scoring.impl.WekaPatternScorer;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.SimilarityGenerator;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.impl.DefaultSimilarityGenerator;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.DefaultRdfExtraction;
import org.aksw.simba.rdflivenews.statistics.Statistics;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.time.TimeUtil;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RdfLiveNews {

    public static Config CONFIG;
    public static String DATA_DIRECTORY;
    public static int ITERATION = 0;

    public static void main(String[] args) throws InvalidFileFormatException, IOException {
        
        // load the config, we dont need to configure logging because the log4j config is on the classpath
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
        initializeDataDirectory();
        
//        for ( String dataDir : Arrays.asList(/*"index/1percent", "index/10percent") ) {
//            for ( Integer occ : Arrays.asList(1,2,3,4,5,6,7,8,9,10)) {
                
//                RdfLiveNews.CONFIG.setStringSetting("general", "index", dataDir);
//                RdfLiveNews.CONFIG.setStringSetting("scoring", "occurrenceThreshold", occ + "");
                
                run();
//            }
//        }
    }

    private static void run() {

        System.out.print("Resetting documents to non duplicate ... ");
        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        
        List<Pattern> patterns                  = new ArrayList<Pattern>();
        Set<Integer> nonDuplicateSentenceIds    = new HashSet<Integer>();
        
        // we need this to be an instance variable because we need to save the similarities which we computed for each iteration
        SimilarityGenerator similarityGenerator = new DefaultSimilarityGenerator(
                (SimilarityMetric) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity")));
        
//        for ( ; ITERATION < 1/* TODO change this back, it takes to long for testing IndexManager.getInstance().getHighestTimeSliceId()*/ ; ITERATION++ ) {
        for ( ; ITERATION < IndexManager.getInstance().getHighestTimeSliceId() ; ITERATION++ ) {
            
            System.out.println("Starting Iteration #" + ITERATION + "!");
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 1. Deduplication
            System.out.println("Starting deduplication!");
            long start = System.currentTimeMillis();
            
            // mark the duplicate sentences in the index, we dont want to use them to search patterns
            Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
//            deduplication.runDeduplication(iteration, iteration + 1, RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window"));
            Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(ITERATION);
//            Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentences();
            nonDuplicateSentenceIds.addAll(currentNonDuplicateSentenceIds);
            
            System.out.println(String.format("Finished deduplication with %s sentences in %s!", currentNonDuplicateSentenceIds.size(), TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));

            // ##################################################
            // ##################################################
            // ##################################################
            // 1.5 Sentence NLP Annotation
            
            System.out.println("Starting NER & POS tagging of " + currentNonDuplicateSentenceIds.size() + " non duplicate sentences!");
            start = System.currentTimeMillis();

            // we can only find patterns if we have NER or POS tags annotated
            NaturalLanguageTagger tagger = (NaturalLanguageTagger) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "tagging"));
//            if you have not yet tagged all sentences in the index you need to uncomment this
//            tagger.annotateSentencesInIndex(currentNonDuplicateSentenceIds);

            System.out.println(String.format("Finished NER & POS tagging of non duplicate sentences in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 2. Pattern Search and Filtering
            
            System.out.println(String.format("Starting pattern search with %s non duplicate sentences and %s threads!", currentNonDuplicateSentenceIds.size(), RdfLiveNews.CONFIG.getStringSetting("search", "number-of-threads")));
            start = System.currentTimeMillis();

            // search the patterns only in the sentences of the current iteration
            PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
            List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds), RdfLiveNews.CONFIG.getIntegerSetting("search", "number-of-threads"));

            // filter the patterns and merge the old and the new patterns
            PatternFilter patternFilter = new DefaultPatternFilter();
            patternsOfIteration         = patternFilter.filter(patternSearchManager.mergeNewFoundPatterns(patternsOfIteration));
            patterns                    = patternSearchManager.mergeNewFoundAndOldPattern(patterns, patternsOfIteration);

            System.out.println(String.format("Finished pattern search with %s patterns in current iteration and %s total patterns in %s!", patternsOfIteration.size(), patterns.size(),  TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 3. Pattern Refinement
            
            System.out.println(String.format("Starting pattern refinement with %s strategy!", RdfLiveNews.CONFIG.getStringSetting("refinement", "typing")));
            start = System.currentTimeMillis();
            
            // refines the domain and range of the patterns 
            PatternRefiner patternRefiner = new DefaultPatternRefiner();
            patternRefiner.refinePatterns(patterns);
            
            System.out.println(String.format("Finished pattern refinement in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 4. Pattern Scoring
            
            System.out.println("Starting pattern scoring!");
            start = System.currentTimeMillis();
            
            // scores the pattern according to certain features
            PatternScorer patternScorer = new OccurrencePatternScorer();
            patternScorer.scorePatterns(patterns);
            patternSearchManager.logPatterns(patterns);
            
            System.out.println(String.format("Finished pattern scoring in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 5. Generate similarities
            System.out.println(String.format("Starting to generate similarities between patterns with %s!", RdfLiveNews.CONFIG.getStringSetting("classes", "similarity")));
            start = System.currentTimeMillis();
            
            Set<Similarity> similarities = similarityGenerator.calculateSimilarities(patterns);
            
            System.out.println(String.format("Finished generate similarities in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 6. Pattern Clustering according to similarities
            System.out.println("Starting to cluster patterns!");
            start = System.currentTimeMillis();
            
            // tries to group similar patterns into the same cluster
            PatternClustering patternClustering = new BorderFlowPatternClustering();
            Set<Cluster<Pattern>> clusters = patternClustering.clusterPatterns(similarities, RdfLiveNews.CONFIG.getDoubleSetting("clustering", "similarityThreshold"));
            
            System.out.println(String.format("Finished clustering with %s clusters in %s!", clusters.size(), TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 6.1 Cluster Labeling
            System.out.println("Starting to label clusters!");
            start = System.currentTimeMillis();
            
            // we need to name the property a single cluster stands for
            ClusterLabeler clusterLabeler = new DefaultClusterLabeling();
            clusterLabeler.labelCluster(clusters);
            
            System.out.println(String.format("Finished labeling clusters in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 6.2 we can merge the clusters
             ClusterMerger clusterMerger = new DefaultClusterMerger();
             clusterMerger.mergeCluster(clusters);
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 7. RDF Extraction
            System.out.println("Starting to write out RDF!");
            start = System.currentTimeMillis();
            
            // use the patterns to extract rdf from news text
            RdfExtraction rdfExtractor = new DefaultRdfExtraction();
            rdfExtractor.extractRdf(clusters);
            
            System.out.println(String.format("Wrote rdf data in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 8. Mapping to DBpedia
            DbpediaMapper mapper = new DefaultDbpediaMapper();
            mapper.map(clusters);
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 9. Create statistics like pos tag distribution
            Statistics stats = new Statistics();
            stats.createStatistics(patterns);
        }
    }

    private static void initializeDataDirectory() {

        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "clusters").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "clusters").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "patterns").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "patterns").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "rdf").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "rdf").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "similarity").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "similarity").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "statistics").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "statistics").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "test").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "test").mkdir();
        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "index").exists()) 
            new File(RdfLiveNews.DATA_DIRECTORY + "index").mkdir();        
    }
}
