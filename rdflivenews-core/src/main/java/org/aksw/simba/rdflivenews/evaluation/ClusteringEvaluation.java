/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.evaluation.comparator.InterClusterSimilarityComparator;
import org.aksw.simba.rdflivenews.evaluation.comparator.InterIntraClusterSimilarityComparator;
import org.aksw.simba.rdflivenews.evaluation.comparator.IntraClusterSimilarityComparator;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.clustering.impl.BorderFlowPatternClustering;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramAndWordnetSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.QGramSimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.WordnetSimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class ClusteringEvaluation {

    private static Map<String,Set<String>> classToPattern = new LinkedHashMap<String,Set<String>>();
    private static Map<String,Double> maxSensitivityForPatternInCluster = new LinkedHashMap<String,Double>();
    private static Map<Integer,Double> maxPPVForPatternInCluster = new LinkedHashMap<>();
    private static ArrayList<String> patternKeys;
    private static PatternClustering clustering = new BorderFlowPatternClustering();
    
    public static void main(String[] args) throws IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
        List<ClusterEvaluationResult> results = new ArrayList<>();
        int iter = 1;

        for ( SimilarityMetric metric : Arrays.asList(new QGramAndWordnetSimilarityMetric(), new QGramSimilarityMetric(), new WordnetSimilarityMetric())) {
        
        	for ( Double threshold : Arrays.asList(1D, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1) ) {
            
                if ( metric instanceof WordnetSimilarityMetric ) {
                    
                    for ( WordnetSimilarity sim : Wordnet.WordnetSimilarity.values()) {
                        
                        ClusterEvaluationResult result = new ClusterEvaluationResult();
                        result.addConfigOption("ClusteringSimilarityThreshold", threshold);
                        result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
                        result.addConfigOption("WordnetSimilarity", sim.toString());
                        results.add(result);
                        ((WordnetSimilarityMetric) metric).setWordnetSimilarity(sim);
                        
                        startEval(result, metric, threshold);
                    }
                }
                else if ( metric instanceof QGramAndWordnetSimilarityMetric ) {

                	for ( Boolean typeSimilarity : Arrays.asList(true, false) ) {
                		for ( WordnetSimilarity sim : Wordnet.WordnetSimilarity.values()) {
                	
                			for ( double wordnetParameter = 0.0 ; wordnetParameter <= 1D ; wordnetParameter += 0.05) {
                				for ( double qgramParameter = 0.0 ; qgramParameter <= 1D ; qgramParameter += 0.05) {
                            
	                                ClusterEvaluationResult result = new ClusterEvaluationResult();
	                                result.addConfigOption("ClusteringSimilarityThreshold", threshold + "");
	                                result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
	                                result.addConfigOption("Force Type Similarity", typeSimilarity.toString());
	                                result.addConfigOption("WordnetParameter", wordnetParameter);
	                                result.addConfigOption("QGramParameter", qgramParameter);
//	                                result.addConfigOption("WordnetParameter", 0);
//	                                result.addConfigOption("QGramParameter", 0);
	                                results.add(result);
	
	                                long start = System.currentTimeMillis();
	                                
	                                result.addConfigOption("WordnetSimilarity", sim.toString());
	                                ((QGramAndWordnetSimilarityMetric) metric).setWordnetSimilarity(sim);
	                                ((QGramAndWordnetSimilarityMetric) metric).qgramParamter = qgramParameter;
	                                ((QGramAndWordnetSimilarityMetric) metric).wordnetParamter = wordnetParameter;
	                                ((QGramAndWordnetSimilarityMetric) metric).setWordnetSimilarity(sim);
	                                RdfLiveNews.CONFIG.setStringSetting("similarity", "checkDomainAndRange", typeSimilarity.toString());
	                                
	                                startEval(result, metric, threshold);
	                                
	                                System.out.println(iter++ + " Time: " + (System.currentTimeMillis() - start));
	                            }
	                        }
                        }
                    }
                }
                else {
                    
                    ClusterEvaluationResult result = new ClusterEvaluationResult();
                    result.addConfigOption("ClusteringSimilarityThreshold", threshold + "");
                    result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
                    results.add(result);
                    
                    startEval(result, metric, threshold);
                }
            }
        }
//        Collections.sort(results);
//        for ( ClusterEvaluationResult result : results ) System.out.println(result + "\n");
        
        Collections.sort(results);
        BufferedFileWriter writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/clustering.evaluation", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (ClusterEvaluationResult sortedResult : results) writer.write(sortedResult.toString() + "\n");
        writer.close();
        
        Collections.sort(results, new IntraClusterSimilarityComparator());
        writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/clustering.evaluation.intra", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (ClusterEvaluationResult sortedResult : results) writer.write(sortedResult.toString() + "\n");
        writer.close();
        
        Collections.sort(results, new InterClusterSimilarityComparator());
        writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/clustering.evaluation.inter", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (ClusterEvaluationResult sortedResult : results) writer.write(sortedResult.toString() + "\n");
        writer.close();
        
        Collections.sort(results, new InterIntraClusterSimilarityComparator());
        writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "evaluation/clustering.evaluation.inter.intra", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (ClusterEvaluationResult sortedResult : results) writer.write(sortedResult.toString() + "\n");
        writer.close();
    }

    private static void startEval(ClusterEvaluationResult result, SimilarityMetric metric, Double threshold) throws IOException {

        // initialize class labels
        initializePatternClassLabels();

        // create similarities
        Set<Similarity> similarities = createSimilarities(metric);

        // we need to do the clustering
        List<Cluster<Pattern>> clusters = new ArrayList<>(createClusters(similarities, threshold));
        
        logClusters(clusters, metric);

        // we need to set the class labels for each pattern
        setClassLabelForPatterns(clusters);

        // run the evaluation
        runEvaluation(result, clusters, metric);
    }

    private static void logClusters(List<Cluster<Pattern>> clusters, SimilarityMetric metric ) {

        if ( RdfLiveNews.CONFIG.getBooleanSetting("clustering", "writeFile") ) {
            
            String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "clusters");
            fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
            fileName += "iter-#" + RdfLiveNews.ITERATION + "-";
            if ( metric instanceof WordnetSimilarityMetric ) fileName += ((WordnetSimilarityMetric)metric).getSimilarityMetric().toString() + "-";
            if ( metric instanceof QGramAndWordnetSimilarityMetric  ) fileName += ((QGramAndWordnetSimilarityMetric)metric).getSimilarityMetric().toString() + "-";
            fileName += metric.getClass().getSimpleName() + "-";
            fileName += RdfLiveNews.CONFIG.getStringSetting("similarity", "checkDomainAndRange") + "-";
            fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".clstr";
            
            BufferedFileWriter writer = new BufferedFileWriter(fileName, Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
            for ( Cluster<Pattern> cluster : clusters) {
             
                for ( Pattern pattern : cluster ) {

                    writer.write("\t" + pattern.getNaturalLanguageRepresentation());
                }
                writer.write("\n");
            }
            writer.close();
        }
    }

    private static void runEvaluation(ClusterEvaluationResult result, List<Cluster<Pattern>> clusters, SimilarityMetric metric) {
        
        double sensitivity = calculateSensitivity(clusters);
        double positivePredictedValue = calculatePPV(clusters);
        double accuracy = Math.sqrt(sensitivity * positivePredictedValue);
        double intraClusterSimilarity = calculateIntraClusterSimilarity(clusters, metric);
        double interClusterSimilarity = calculateInterClusterSimilarity(clusters, metric);
        
        result.setSensitivity(sensitivity);
        result.setPositivePredictedValue(positivePredictedValue);
        result.setAccuracy(accuracy);
        result.setIntraClusterSimilarity(intraClusterSimilarity);
        result.setInterClusterSimilarity(interClusterSimilarity);
        
        System.out.println(result + "\n");
    }
    
    private static double calculateInterClusterSimilarity(List<Cluster<Pattern>> clusters, SimilarityMetric metric) {

        double totalInterClusterSim = 0F;
        int clusterComparisons = 0;
        for ( Cluster<Pattern> cluster1 : clusters ) {
            for ( Cluster<Pattern> cluster2 : clusters) {
                if ( cluster1 != cluster2 ) {
         
                    clusterComparisons++;
                    
                    double interClusterSim   = 0D;
                    int comparisons     = 0;
                    
                    for ( Pattern p1 : cluster1 ) {
                        for ( Pattern p2 : cluster2) {
                                
                            interClusterSim += metric.calculateSimilarity(p1, p2);
                            comparisons++;
                        }
                    }
                    totalInterClusterSim += interClusterSim / comparisons;
                }
            }
        }
        return totalInterClusterSim / clusterComparisons;
    }

    private static double calculateIntraClusterSimilarity(List<Cluster<Pattern>> clusters, SimilarityMetric metric) {

        double totalSim = 0F;
        for ( Cluster<Pattern> cluster : clusters ) {
        	
        	double sim = getClusterSimilarity(cluster, metric);
            totalSim += sim;
        }
        return (double) (totalSim / clusters.size());
    }
    
    private static double getClusterSimilarity(Cluster<Pattern> cluster, SimilarityMetric metric) {
        
        if ( cluster.size() == 1 ) return 0D;
        else {

            double clusterSim   = 0D;
            int comparisons     = 0;
            for ( Pattern p1 : cluster ) {
                for ( Pattern p2 : cluster) {
                    if ( !p1.getNaturalLanguageRepresentation().equals(p2.getNaturalLanguageRepresentation()) ) {
                        
                    	double sim = metric.calculateSimilarity(p1, p2);
                        clusterSim += sim;
                        comparisons++;
                    }
                }
            }
            
            return clusterSim / comparisons;
        }
    }

    /**
     * The positive predictive value is the proportion of members of cluster j 
     * which belong to complex i, relative to the total number of members of 
     * this cluster assigned to all complexes.
     * 
     * @param clusters
     * @return
     */
    private static double calculatePPV(List<Cluster<Pattern>> clusters) {

        double[][] ppvMatrix = new double[classToPattern.size()][clusters.size()];

        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {

            Cluster<Pattern> cluster = clusters.get(clusterIndex);
            double ppvMax = 0;
            
            for (int mappingIndex = 0; mappingIndex < classToPattern.size(); mappingIndex++) {

                int patternsInCluster = 0;
                for (Pattern pattern : cluster) 
                    if ( pattern.getClazz().equals(patternKeys.get(mappingIndex))) patternsInCluster++;

                ppvMatrix[mappingIndex][clusterIndex] = (double) patternsInCluster / clusters.get(clusterIndex).size();
                ppvMax = Math.max(ppvMax, ppvMatrix[mappingIndex][clusterIndex]);
            }
            maxPPVForPatternInCluster.put(clusterIndex, ppvMax);
        }
        
        double numberOfPatternsForMappingI = 0;
        double weightedNumberOfPatternsForMappingI = 0;
        
        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {
            
            weightedNumberOfPatternsForMappingI += clusters.get(clusterIndex).size() * maxPPVForPatternInCluster.get(clusterIndex);
            numberOfPatternsForMappingI += clusters.get(clusterIndex).size();
        }
        
        return weightedNumberOfPatternsForMappingI / numberOfPatternsForMappingI;
    }

    private static double calculateSensitivity(List<Cluster<Pattern>> clusters) {

        double[][] sensitivityMatrix = new double[classToPattern.size()][clusters.size()];
        
        // sensitivity is the fraction of patterns of mapping i which are found in cluster j
        for ( int mappingIndex = 0; mappingIndex < classToPattern.size(); mappingIndex++) {

            String clazz = patternKeys.get(mappingIndex);
            double sensitivityMax = 0f;
            
            for ( int clusterIndex = 0; clusterIndex < clusters.size() ; clusterIndex++ ) {
                
                int patternsInCluster = 0;
                
                for ( Pattern pattern : clusters.get(clusterIndex) ) {
                	if ( pattern.getClazz().equals(clazz) ) patternsInCluster++;
                }
                
                sensitivityMatrix[mappingIndex][clusterIndex] = (double)patternsInCluster / classToPattern.get(clazz).size();
                sensitivityMax = Math.max(sensitivityMax, sensitivityMatrix[mappingIndex][clusterIndex]);
            }
            
            maxSensitivityForPatternInCluster.put(clazz, sensitivityMax);
        }
        
        double numberOfPatternsForMappingI = 0;
        double weightedNumberOfPatternsForMappingI = 0;
        
        for ( String clazz : patternKeys) {
            
            weightedNumberOfPatternsForMappingI += classToPattern.get(clazz).size() * maxSensitivityForPatternInCluster.get(clazz);
            numberOfPatternsForMappingI += classToPattern.get(clazz).size();
        }
        
        return weightedNumberOfPatternsForMappingI / numberOfPatternsForMappingI;
    }

    /**
     * 
     * @param clusters
     */
    private static void setClassLabelForPatterns(List<Cluster<Pattern>> clusters) {

        for ( Cluster<Pattern> cluster : clusters ) 
            for ( Pattern p : cluster ) 
                for ( Map.Entry<String, Set<String>> entry : classToPattern.entrySet())
                    if ( entry.getValue().contains(p.getNaturalLanguageRepresentation()) )
                        p.setClazz(entry.getKey());
    }

    private static Set<Cluster<Pattern>> createClusters(Set<Similarity> similarities, Double threshold) {

        Set<Cluster<Pattern>> clusters = new HashSet<Cluster<Pattern>>(); 
        for ( Cluster<Pattern> cluster : clustering.clusterPatterns(similarities, threshold)) {
            
            if ( cluster.size() > 0 ) clusters.add(cluster);
        }
        return clusters;
    }

    private static Set<Similarity> createSimilarities(SimilarityMetric metric) throws IOException {

        List<String> lines = FileUtils.readLines(new File(RdfLiveNews.DATA_DIRECTORY + "/evaluation/patterns1percent5occ.pattern"));

        Set<Similarity> sims = new HashSet<>();

        for (String line1 : lines) {

            String[] parts = line1.split("___");

            String nlr1 = parts[0];
            String pos1 = parts[1];

            for (String line2 : lines) {

                String[] parts2 = line2.split("___");

                String nlr2 = parts2[0];
                String pos2 = parts2[1];

                Pattern p1 = new DefaultPattern(nlr1, pos1);
                Pattern p2 = new DefaultPattern(nlr2, pos2);

                double similarity = metric.calculateSimilarity(p1, p2);

                sims.add(new Similarity(p1, p2, similarity));
            }
        }
        return sims;
    }

    private static void initializePatternClassLabels() {
    	
    	try {
    		
    		Set<String> cluster = new HashSet<String>();
			for ( String s : FileUtils.readLines(new File(RdfLiveNews.DATA_DIRECTORY + "/evaluation/gs_clusters.txt"))){
				
				if ( !s.isEmpty() ) cluster.add(s);
				else {
					classToPattern.put(cluster.iterator().next(), cluster);
					cluster = new HashSet<>();
				}
			}
			// add the last one
			classToPattern.put(cluster.iterator().next(), cluster);
		}
    	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        patternKeys = new ArrayList<String>(classToPattern.keySet());
    }
}
