/**
 * 
 */
package org.aksw.simba.rdflivenews;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.ClusterLabeler;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.deduplication.impl.DummyDeduplication;
import org.aksw.simba.rdflivenews.deduplication.impl.FastDeduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.nlp.NaturalLanguageTagger;
import org.aksw.simba.rdflivenews.nlp.impl.NamedEntityAndPartOfSpeechNaturalLanguageTagger;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.DefaultPatternClustering;
import org.aksw.simba.rdflivenews.pattern.mapping.DbpediaMapper;
import org.aksw.simba.rdflivenews.pattern.mapping.impl.DefaultDbpediaMapper;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.impl.DefaultPatternRefiner;
import org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer;
import org.aksw.simba.rdflivenews.pattern.scoring.impl.WekaPatternScorer;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.DefaultRdfExtraction;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.time.TimeUtil;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RdfLiveNews {

    public static Config CONFIG;

    public static void main(String[] args) throws InvalidFileFormatException, IOException {
        
        // load the config, we dont need to configure logging because the log4j config is on the classpath
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        
        List<Pattern> patterns               = new ArrayList<Pattern>();
        Set<Integer> nonDuplicateSentenceIds = new HashSet<Integer>();
        
        for ( int iteration = 0 ; iteration <= 5/* TODO change this back, it takes to long for testing IndexManager.getInstance().getHighestTimeSliceId()*/ ; iteration++ ) {
            
            System.out.println("Starting Iteration #" + iteration + "!");
            
            // ##################################################
            // 1. Deduplication
            System.out.println("Starting deduplication!");
            long start = System.currentTimeMillis();
            
            // mark the duplicate sentences in the index, we dont want to use them to search patterns
            Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
            deduplication.runDeduplication(iteration, iteration + 1, RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window"));
            Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(iteration);
            nonDuplicateSentenceIds.addAll(currentNonDuplicateSentenceIds);
            
            System.out.println(String.format("Finished deduplication with %s sentences in %s!", currentNonDuplicateSentenceIds.size(), TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));

            // ##################################################
            // 1.5 Sentence NLP Annotation
            
            System.out.println("Starting NER & POS tagging of " +currentNonDuplicateSentenceIds.size() + " non duplicate sentences!");
            start = System.currentTimeMillis();

            // we can only find patterns if we have NER or POS tags annotated
            NaturalLanguageTagger tagger = (NaturalLanguageTagger) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "tagging"));
            tagger.annotateSentencesInIndex(currentNonDuplicateSentenceIds);

            System.out.println(String.format("Finished NER & POS tagging of non duplicate sentences in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // 2. Pattern Search
            
            PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
            // search the patterns only in the sentences of the current iteration
            List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds), RdfLiveNews.CONFIG.getIntegerSetting("search", "number-of-threads"));
            // merge those patterns with themselves
            patternsOfIteration = patternSearchManager.mergeNewFoundPatterns(patternsOfIteration);
            // merge the old and the new patterns
            patterns = patternSearchManager.mergeNewFoundAndOldPattern(patterns, patternsOfIteration);
            
            // ##################################################
            // 3. Pattern Refinement
            
            // refines the domain and range of the patterns 
            PatternRefiner patternRefiner = new DefaultPatternRefiner();
            patternRefiner.refinePatterns(patterns);
            
            // ##################################################
            // 4. Pattern Scoring
            
            // scores the pattern according to certain features
            PatternScorer patternScorer = new WekaPatternScorer();
            patternScorer.scorePatterns(patterns);
            
            // ##################################################
            // 5. Pattern Clustering
            
            // tries to group similar patterns into the same cluster
            PatternClustering patternClustering = new DefaultPatternClustering();
            Set<Cluster<Pattern>> cluster = patternClustering.clusterPatterns(patterns);
            
            // ##################################################
            // 6. Cluster Labeling
            
            // we need to name the property a single cluster stands for
            ClusterLabeler clusterLabeler = new DefaultClusterLabeling();
            clusterLabeler.labelCluster(cluster);
            
            // ##################################################
            // 7. RDF Extraction
            
            // use the patterns to extract rdf from news text
            RdfExtraction rdfExtractor = new DefaultRdfExtraction();
            rdfExtractor.extractRdf(cluster);
            
            // ##################################################
            // 8. Mapping to DBpedia
            DbpediaMapper mapper = new DefaultDbpediaMapper();
            mapper.map(cluster);
        }
    }
}
