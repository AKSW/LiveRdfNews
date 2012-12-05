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

import org.aksw.simba.rdflivenews.cluster.Cluster;
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

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class ClusteringEvaluation {

    private static Map<String,Set<String>> classToPattern = new LinkedHashMap<String,Set<String>>();
    private static Map<String,Float> maxSensitivityForPatternInCluster = new LinkedHashMap<String,Float>();
    private static Map<Integer,Float> maxPPVForPatternInCluster = new LinkedHashMap<>();
    private static ArrayList<String> patternKeys;
    private static PatternClustering clustering = new BorderFlowPatternClustering();
    
    public static void main(String[] args) throws IOException {
        
        List<EvaluationResult> results = new ArrayList<>();
        
        for ( Double threshold : Arrays.asList(/*0D, 0.1, 0.2, */0.3, 0.4, 0.5/*, 0.6, 0.7, 0.8, 0.9, 1D*/) ) {
            
            for ( SimilarityMetric metric : Arrays.asList(/*new QGramSimilarityMetric(), new WordnetSimilarityMetric(), */ new QGramAndWordnetSimilarityMetric() )) {

                if ( metric instanceof WordnetSimilarityMetric ) {
                    
                    for ( WordnetSimilarity sim : Wordnet.WordnetSimilarity.values()) {
                        
                        EvaluationResult result = new EvaluationResult();
                        result.addConfigOption("ClusteringSimilarityThreshold", threshold);
                        result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
                        result.addConfigOption("WordnetSimilarity", sim.toString());
                        results.add(result);
                        ((WordnetSimilarityMetric) metric).setWordnetSimilarity(sim);
                        
                        startEval(result, metric, threshold);
                    }
                }
                else if ( metric instanceof QGramAndWordnetSimilarityMetric ) {
                    
                    for ( double wordnetParameter : Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1D)) {
                        for ( double qgramParameter : Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1D)) {
                        
                            EvaluationResult result = new EvaluationResult();
                            result.addConfigOption("ClusteringSimilarityThreshold", threshold + "");
                            result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
                            result.addConfigOption("WordnetParameter", wordnetParameter);
                            result.addConfigOption("QGramParameter", qgramParameter);
                            results.add(result);
                            
                            ((QGramAndWordnetSimilarityMetric) metric).setWordnetParamter(wordnetParameter);
                            ((QGramAndWordnetSimilarityMetric) metric).setQgramParamter(qgramParameter);
                            
                            for ( WordnetSimilarity sim : Wordnet.WordnetSimilarity.values()) {
                                
                                result.addConfigOption("WordnetSimilarity", sim.toString());
                                ((QGramAndWordnetSimilarityMetric) metric).setWordnetSimilarity(sim);
                                
                                startEval(result, metric, threshold);
                            }
                            
//                            startEval(results, config, metric, threshold);
                        }
                    }
                }
                else {
                    
                    for (Integer i : Arrays.asList(-1000, -100, -10, -1, 0, 1, 10, 100, 1000)) {
                        
                        EvaluationResult result = new EvaluationResult();
                        result.addConfigOption("ClusteringSimilarityThreshold", threshold + "");
                        result.addConfigOption("SimilarityMetric", metric.getClass().getSimpleName());
                        result.addConfigOption("BorderFlowDelta", i);
                        results.add(result);
                        
                        ((BorderFlowPatternClustering)clustering).setDelta(i);
                        
                        startEval(result, metric, threshold);
                    }
                }
            }
        }
        Collections.sort(results);
        
        for ( EvaluationResult result : results ) System.out.println(result + "\n");
    }

    private static void startEval(EvaluationResult result, SimilarityMetric metric, Double threshold) throws IOException {

        // initialize class labels
        initializePatternClassLabels();

        // create similarities
        Set<Similarity> similarities = createSimilarities(metric);

        // we need to do the clustering
        List<Cluster<Pattern>> clusters = new ArrayList<>(createClusters(similarities, threshold));

        // we need to set the class labels for each pattern
        setClassLabelForPatterns(clusters);

        // run the evaluation
        runEvaluation(result, clusters);
    }

    private static void runEvaluation(EvaluationResult result, List<Cluster<Pattern>> clusters) {
        
        float sensitivity = calculateSensitivity(clusters);
        float positivePredictedValue = calculatePPV(clusters);
        double accuracy = Math.sqrt(sensitivity * positivePredictedValue);
        
        result.setSensitivity(sensitivity);
        result.setPositivePredictedValue(positivePredictedValue);
        result.setAccuracy(accuracy);
        
        System.out.println(result + "\n");
    }
    
    /**
     * The positive predictive value is the proportion of members of cluster j 
     * which belong to complex i, relative to the total number of members of 
     * this cluster assigned to all complexes.
     * 
     * @param clusters
     * @return
     */
    private static float calculatePPV(List<Cluster<Pattern>> clusters) {

        float[][] ppvMatrix = new float[classToPattern.size()][clusters.size()];

        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {

            Cluster<Pattern> cluster = clusters.get(clusterIndex);
            float ppvMax = 0f;
            
            for (int mappingIndex = 0; mappingIndex < classToPattern.size(); mappingIndex++) {

                int patternsInCluster = 0;
                for (Pattern pattern : cluster) 
                    if ( pattern.getClazz().equals(patternKeys.get(mappingIndex))) patternsInCluster++;

                ppvMatrix[mappingIndex][clusterIndex] = (float) patternsInCluster / clusters.get(clusterIndex).size();
                ppvMax = Math.max(ppvMax, ppvMatrix[mappingIndex][clusterIndex]);
            }
            maxPPVForPatternInCluster.put(clusterIndex, ppvMax);
        }
        
        float numberOfPatternsForMappingI = 0;
        float weightedNumberOfPatternsForMappingI = 0;
        
        for (int clusterIndex = 0; clusterIndex < clusters.size(); clusterIndex++) {
            
            weightedNumberOfPatternsForMappingI += clusters.get(clusterIndex).size() * maxPPVForPatternInCluster.get(clusterIndex);
            numberOfPatternsForMappingI += clusters.get(clusterIndex).size();
        }
        
        return weightedNumberOfPatternsForMappingI / numberOfPatternsForMappingI;
    }

    private static float calculateSensitivity(List<Cluster<Pattern>> clusters) {

        float[][] sensitivityMatrix = new float[classToPattern.size()][clusters.size()];
        
        // sensitivity is the fraction of patterns of mapping i which are found in cluster j
        for ( int mappingIndex = 0; mappingIndex < classToPattern.size(); mappingIndex++) {

            String clazz = patternKeys.get(mappingIndex);
            float sensitivityMax = 0f;
            
            for ( int clusterIndex = 0; clusterIndex < clusters.size() ; clusterIndex++ ) {
                
                int patternsInCluster = 0;
                
                for ( Pattern pattern : clusters.get(clusterIndex) ) 
                    if ( pattern.getClazz().equals(clazz) ) patternsInCluster++;
                
                sensitivityMatrix[mappingIndex][clusterIndex] = (float)patternsInCluster / classToPattern.get(clazz).size();
                sensitivityMax = Math.max(sensitivityMax, sensitivityMatrix[mappingIndex][clusterIndex]);
            }
            
            maxSensitivityForPatternInCluster.put(clazz, sensitivityMax);
        }
        
        float numberOfPatternsForMappingI = 0;
        float weightedNumberOfPatternsForMappingI = 0;
        
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

        for ( Cluster<Pattern> cluster : clusters ) {
            for ( Pattern p : cluster ) {
                for ( Map.Entry<String, Set<String>> entry : classToPattern.entrySet()) {
                    if ( entry.getValue().contains(p.getNaturalLanguageRepresentation()) ) {
                        p.setClazz(entry.getKey());
                    }
                }
            }
        }
    }

    private static Set<Cluster<Pattern>> createClusters(Set<Similarity> similarities, Double threshold) {

        Set<Cluster<Pattern>> clusters = new HashSet<Cluster<Pattern>>(); 
        for ( Cluster<Pattern> cluster : clustering.clusterPatterns(similarities, threshold)) {
            
            if ( cluster.size() > 0 ) clusters.add(cluster);
        }
        return clusters;
    }

    private static Set<Similarity> createSimilarities(SimilarityMetric metric) throws IOException {

        List<String> lines = FileUtils.readLines(new File("/Users/gerb/test/patterns1percent5occ.pattern"));

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

        classToPattern.put("says", new HashSet<>(Arrays.asList(", '' said", ", '' says", "asked", "said", "said ,", "said in", "said of", "said on", "said that", "says", "says that", "announced", ", told", ", said", ", according to", "calls", "tells", "told", "called", "reported", "noted that")));
        classToPattern.put("spokesperson", new HashSet<>(Arrays.asList("spokeswoman", ", spokeswoman for", ", a spokesman for", "spokesman", ", a spokeswoman for", "spokesperson",", spokesman for")));
        classToPattern.put("director", new HashSet<>(Arrays.asList(", the director of", "director", ", director of", "executive director", ", executive director of"))); 
        classToPattern.put("player", new HashSet<>(Arrays.asList("quarterback", "receiver", "pitcher", "first baseman", "third baseman", "player"))); 
        classToPattern.put("chief", new HashSet<>(Arrays.asList("captain", ", head of", "chief executive", "chief"))); 
        classToPattern.put("attorney", new HashSet<>(Arrays.asList("'s attorney ,", "attorney", "'s lawyer ,", "lawyer"))); 
        classToPattern.put("president", new HashSet<>(Arrays.asList("president", ", the president of", ", president of"))); 
        classToPattern.put("coach", new HashSet<>(Arrays.asList("head coach", "football coach", "coach"))); 
        classToPattern.put("goto", new HashSet<>(Arrays.asList("returned to", "went to", "comes to"))); 
        classToPattern.put("daughter", new HashSet<>(Arrays.asList("'s daughter ,", "; daughters", ", daughter"))); 
        classToPattern.put("placeOf", new HashSet<>(Arrays.asList("suburb of", "town of", "city of"))); 
        classToPattern.put("leader", new HashSet<>(Arrays.asList("leader", "led"))); 
        classToPattern.put("basedIn", new HashSet<>(Arrays.asList("headquarters in", ", based in"))); 
        classToPattern.put("memberOf", new HashSet<>(Arrays.asList("member", ", a member of"))); 
        classToPattern.put("livesIn", new HashSet<>(Arrays.asList(", who lives in", "lives in"))); 
        classToPattern.put("chairman", new HashSet<>(Arrays.asList(", chairman of", "chairman"))); 
        classToPattern.put("candidate", new HashSet<>(Arrays.asList("presidential candidate", "candidate"))); 
        classToPattern.put("graduatedFrom", new HashSet<>(Arrays.asList("graduated from"))); 
        classToPattern.put("wasTaken", new HashSet<>(Arrays.asList("took", "was taken to"))); 
        classToPattern.put("founder", new HashSet<>(Arrays.asList("co-founder", "founder"))); 
        classToPattern.put("owner", new HashSet<>(Arrays.asList(", owner of", "owner")));
        classToPattern.put("champion", new HashSet<>(Arrays.asList("champions", "champion")));
        classToPattern.put("aka", new HashSet<>(Arrays.asList(", known as", ", also known as"))); 
        classToPattern.put("manager", new HashSet<>(Arrays.asList("campaign manager", "manager"))); 
        classToPattern.put("writer", new HashSet<>(Arrays.asList("writer", "writers"))); 
        classToPattern.put("selected", new HashSet<>(Arrays.asList("named", "'s selection of")));
        classToPattern.put("win", new HashSet<>(Arrays.asList("beat", "defeated")));
        classToPattern.put(", general partner ,", new HashSet<>(Arrays.asList(", general partner ,")));
        classToPattern.put(", hold for", new HashSet<>(Arrays.asList(", hold for")));
        classToPattern.put(", including", new HashSet<>(Arrays.asList(", including")));
        classToPattern.put(", part of", new HashSet<>(Arrays.asList(", part of")));
        classToPattern.put("; brothers", new HashSet<>(Arrays.asList("; brothers")));
        classToPattern.put("'s mother ,", new HashSet<>(Arrays.asList("'s mother ,")));
        classToPattern.put("'s son ,", new HashSet<>(Arrays.asList("'s son ,")));
        classToPattern.put("'s wife ,", new HashSet<>(Arrays.asList("'s wife ,")));
        classToPattern.put("analyst", new HashSet<>(Arrays.asList("analyst")));
        classToPattern.put("arrived in", new HashSet<>(Arrays.asList("arrived in")));
        classToPattern.put("being in", new HashSet<>(Arrays.asList("being in")));
        classToPattern.put("congressman", new HashSet<>(Arrays.asList("congressman")));
        classToPattern.put("criticized", new HashSet<>(Arrays.asList("criticized")));
        classToPattern.put("customers in", new HashSet<>(Arrays.asList("customers in")));
        classToPattern.put("dated", new HashSet<>(Arrays.asList("dated")));
        classToPattern.put("died in", new HashSet<>(Arrays.asList("died in")));
        classToPattern.put("editor", new HashSet<>(Arrays.asList("editor")));
        classToPattern.put("found", new HashSet<>(Arrays.asList("found")));
        classToPattern.put("gave", new HashSet<>(Arrays.asList("gave")));
        classToPattern.put("gold medalist", new HashSet<>(Arrays.asList("gold medalist")));
        classToPattern.put("in downtown", new HashSet<>(Arrays.asList("in downtown")));
        classToPattern.put("is a former", new HashSet<>(Arrays.asList("is a former")));
        classToPattern.put("joined", new HashSet<>(Arrays.asList("joined")));
        classToPattern.put("last year ,", new HashSet<>(Arrays.asList("last year ,")));
        classToPattern.put("left", new HashSet<>(Arrays.asList("left")));
        classToPattern.put("pitched", new HashSet<>(Arrays.asList("pitched")));
        classToPattern.put("police", new HashSet<>(Arrays.asList("police")));
        classToPattern.put("praised", new HashSet<>(Arrays.asList("praised")));
        classToPattern.put("put", new HashSet<>(Arrays.asList("put")));
        classToPattern.put("reporter", new HashSet<>(Arrays.asList("reporter")));
        classToPattern.put("scored on", new HashSet<>(Arrays.asList("scored on")));
        classToPattern.put("star", new HashSet<>(Arrays.asList("star")));
        classToPattern.put("state", new HashSet<>(Arrays.asList("state")));
        classToPattern.put("teammate", new HashSet<>(Arrays.asList("teammate")));
        classToPattern.put("was born", new HashSet<>(Arrays.asList("was born")));
        classToPattern.put("winner", new HashSet<>(Arrays.asList("winner")));
        classToPattern.put("withdrew from", new HashSet<>(Arrays.asList("withdrew from")));
        classToPattern.put("won", new HashSet<>(Arrays.asList("won")));
        
        patternKeys = new ArrayList<String>(classToPattern.keySet());
    }
}
