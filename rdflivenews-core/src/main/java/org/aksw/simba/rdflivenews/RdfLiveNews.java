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
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.ClusterLabeler;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.Extraction;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.nlp.NaturalLanguageTagger;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.BorderFlowPatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.ClusterMerger;
import org.aksw.simba.rdflivenews.pattern.clustering.merging.DefaultClusterMerger;
import org.aksw.simba.rdflivenews.pattern.comparator.PatternSupportSetComparator;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.linking.Linker;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefinementManager;
import org.aksw.simba.rdflivenews.pattern.search.concurrency.PatternSearchThreadManager;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.impl.SimilarityGeneratorManager;
import org.aksw.simba.rdflivenews.rdf.RdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.NIFRdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.ObjectPropertyTriple;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.aksw.simba.rdflivenews.statistics.Statistics;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.lucene.LuceneManager;
import com.github.gerbsen.time.TimeUtil;
import de.uni_leipzig.simba.data.Mapping;
import org.aksw.simba.rdflivenews.pattern.linking.impl.LimesLinker;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RdfLiveNews {

    public static Config CONFIG;
    public static String DATA_DIRECTORY;
    public static int ITERATION = 0;
    public static List<Pattern> patterns                  = new ArrayList<Pattern>();
    public static Set<Cluster<Pattern>> clusters;
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        init();
        run();
    }
    
    private static void run() {

//        System.out.print("Resetting documents to non duplicate ... ");
//        we only need to do this, if the deduplication is running again
//        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        Statistics stats = new Statistics();
        
        // we need this to be an instance variable because we need to save the similarities which we computed for each iteration
        SimilarityGeneratorManager similarityGenerator = new SimilarityGeneratorManager(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity"));
        Set<Similarity> similarities = Collections.synchronizedSet(new HashSet<Similarity>());
     
        // we can only find patterns if we have NER or POS tags annotated
        NaturalLanguageTagger tagger = (NaturalLanguageTagger) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "tagging"));
        
        for ( ; ITERATION < 1/* TODO change this back, it takes to long for testing IndexManager.getInstance().getHighestTimeSliceId()*/ ; ITERATION++ ) {
//        for ( ; ITERATION <= IndexManager.getInstance().getHighestTimeSliceId() ; ITERATION++ ) {
            
            long iterationTime = System.currentTimeMillis();
            System.out.println("Starting Iteration #" + ITERATION + "!");
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 1. Deduplication
//            System.out.println("Starting deduplication!");
            long start = System.currentTimeMillis();
            
            // mark the duplicate sentences in the index, we dont want to use them to search patterns
//            Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
//            deduplication.runDeduplication(ITERATION, 40/*ITERATION + 1*/, RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window"));
//            Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(ITERATION);
            Set<Integer> currentNonDuplicateSentenceIds = IndexManager.getInstance().getNonDuplicateSentences();
            
//            Set<Integer> currentNonDuplicateSentenceIds = deduplication.runDeduplication(ITERATION, 40/*ITERATION + 1*/, RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window"));
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            System.out.println(String.format("Finished deduplication with %s sentences in %s!", currentNonDuplicateSentenceIds.size(), TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));

            // ##################################################
            // ##################################################
            // ##################################################
            // 1.5 Sentence NLP Annotation
            
            System.out.println("Starting NER & POS tagging of " + currentNonDuplicateSentenceIds.size() + " non duplicate sentences!");
            start = System.currentTimeMillis();
//            if you have not yet tagged all sentences in the index you need to uncomment this
//            if (ITERATION > 30) tagger.annotateSentencesInIndex(currentNonDuplicateSentenceIds);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);

            System.out.println(String.format("Finished NER & POS tagging of non duplicate sentences in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 2. Pattern Search and Filtering
            
            System.out.println(String.format("Starting pattern search with %s non duplicate sentences and %s threads!", currentNonDuplicateSentenceIds.size(), Runtime.getRuntime().availableProcessors()));
            start = System.currentTimeMillis();

            // search the patterns only in the sentences of the current iteration
            PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
            List<Pattern> patternsOfIteration = patternSearchManager.startPatternSearchCallables(new ArrayList<Integer>(currentNonDuplicateSentenceIds));
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);

            // filter the patterns and merge the old and the new patterns
            PatternFilter patternFilter = new DefaultPatternFilter();
            patternsOfIteration         = patternFilter.filter(patternSearchManager.mergeNewFoundPatterns(patternsOfIteration));
            patterns                    = patternSearchManager.mergeNewFoundAndOldPattern(patterns, patternsOfIteration);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);

            System.out.println(String.format("Finished pattern search with %s patterns in current iteration and %s total patterns in %s!", patternsOfIteration.size(), patterns.size(),  TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 3. Pattern Scoring
            
//            System.out.println("Starting pattern scoring!");
//            start = System.currentTimeMillis();
//            
//            // scores the pattern according to certain features
//            PatternScorer patternScorer = new OccurrencePatternScorer();
//            patternScorer.scorePatterns(patterns);
            
            Collections.sort(patterns, new PatternSupportSetComparator());
            List<Pattern> top1PercentPattern = patterns.size() > 100000 ? patterns.subList(0, 1000) : patterns.subList(0, patterns.size() / 100);
            
            System.out.println(String.format("Finished pattern scoring in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 4. Pattern Refinement
            
            System.out.println(String.format("Starting pattern refinement with %s strategy!", RdfLiveNews.CONFIG.getStringSetting("refiner", "typing")));
            start = System.currentTimeMillis();
            
            // refines the domain and range of the patterns
            PatternRefinementManager refinementManager = new PatternRefinementManager();
            refinementManager.startPatternRefinement(top1PercentPattern);
            patternSearchManager.logPatterns(patterns);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            System.out.println(String.format("Finished pattern refinement in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 5. Generate similarities
            System.out.println(String.format("Starting to generate similarities between patterns with %s!", RdfLiveNews.CONFIG.getStringSetting("classes", "similarity")));
            start = System.currentTimeMillis();
            
            similarityGenerator.startSimilarityGeneratorThreads(top1PercentPattern, similarities);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            System.out.println(String.format("Finished generate similarities in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 6. Pattern Clustering according to similarities
            System.out.println("Starting to cluster patterns!");
            start = System.currentTimeMillis();
            
            // tries to group similar patterns into the same cluster
            PatternClustering patternClustering = new BorderFlowPatternClustering();
            clusters = patternClustering.clusterPatterns(similarities, RdfLiveNews.CONFIG.getDoubleSetting("clustering", "similarityThreshold"));
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
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
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            System.out.println(String.format("Finished labeling clusters in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 6.2 we can merge the clusters
            ClusterMerger clusterMerger = new DefaultClusterMerger();
            clusterMerger.mergeCluster(clusters);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 7. RDF Extraction
            System.out.println("Starting to write out RDF!");
            start = System.currentTimeMillis();
            
            // use the patterns to extract rdf from news text
            RdfExtraction rdfExtractor = new NIFRdfExtraction();
//            RdfExtraction rdfExtractor = new SimpleRdfExtraction();
            List<Triple> triples = rdfExtractor.extractRdf(clusters);
            rdfExtractor.uploadRdf();
//            writeEvalData(triples);
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            System.out.println(String.format("Wrote rdf data in %s!", TimeUtil.convertMilliSeconds(System.currentTimeMillis() - start)));
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 8. Mapping to DBpedia
            start = System.currentTimeMillis();
            
//            DbpediaMapper mapper = new DefaultDbpediaMapper();
//            mapper.map(clusters);
            
            Linker linker = new LimesLinker();
            Mapping m = linker.link(clusters, 0.2);
            System.out.println(m);
            
            
            Statistics.durationPerIteration.get(ITERATION).add(System.currentTimeMillis() - start);
            
            // ##################################################
            // ##################################################
            // ##################################################
            // 9. Create statistics like pos tag distribution
            stats.createStatistics(patterns);
            
            System.out.println("Finished iteration #" + ITERATION + " in " + TimeUtil.convertMilliSeconds(System.currentTimeMillis() - iterationTime) + ".");
        }
    }

    public static void init() {

    	// load the config, we dont need to configure logging because the log4j config is on the classpath
        try {
        	
			RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
			RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
	    	
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
	        if ( !new File(RdfLiveNews.DATA_DIRECTORY + "evaluation").exists()) 
	            new File(RdfLiveNews.DATA_DIRECTORY + "evaluation").mkdir();
	            new File(RdfLiveNews.DATA_DIRECTORY + "rdf").mkdir();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void writeEvalData(List<Triple> triples) {

    	for ( Integer clusterMinSize : Arrays.asList(1, 2, 3, 4, 5) ) {

    		Collections.shuffle(triples);
            int maxWrittenTriples = 200;
            int writtenTriples = 0;
            BufferedFileWriter writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/rlneval_triple_" + clusterMinSize + "_c_min_size_" + clusterMinSize+".tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
            for ( int tripleIndex = 0 ; tripleIndex < triples.size() && writtenTriples < maxWrittenTriples ; tripleIndex++) {

            	ObjectPropertyTriple triple = (ObjectPropertyTriple) triples.get(tripleIndex);
            	if ( triple.cluster.size() >= clusterMinSize ) {

            		List<String> line = new ArrayList<String>();
                	line.add(triple.getSubjectUri());
                	line.add(triple.getSubjectLabel());
                	line.add(triple.getPropertyType());
                	line.add(triple.getObject());
                	line.add(triple.getObjectLabel());
                	List<Set<Extraction>> list = Arrays.asList(IndexManager.getInstance().getTextArticleDateAndArticleUrl(triple.getSentenceId()));
                	Extraction x1 = list.get(0).iterator().next();
                	line.add(x1.text);

                	writer.write(StringUtils.join(line, "\t"));
                	writtenTriples++;
            	}
            }

            writer.close();
    	}
	}
    
private static void test() {
        
        IndexManager.getInstance();
        IndexSearcher searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        
        Set<String> urls = new HashSet<>();
        
        for (int i = 0; i < 1000 ; i++) {
        
            Document doc = LuceneManager.getDocumentByNumber(searcher.getIndexReader(), i);
            String url = doc.get(Constants.LUCENE_FIELD_URL);
            
            if ( !urls.contains(url) ) {
                
                urls.add(url);
                
                System.out.println(url);
                
                for ( String s : IndexManager.getInstance().getAllSentencesFromArticle(url)) {
                    
                    System.out.println(s);
                }
            }
        }
    }
}
