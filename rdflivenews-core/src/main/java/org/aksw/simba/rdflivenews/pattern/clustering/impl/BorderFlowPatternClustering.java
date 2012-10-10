/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.clustering.impl;

import de.uni_leipzig.gk.cluster.BorderFlowHard;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;

/**
 *
 * @author ngonga
 */
public class BorderFlowPatternClustering implements PatternClustering {

    public Set<Cluster<Pattern>> clusterPatterns(Set<Similarity> similarities, Double similarityThreshold) {

        Map<Set<String>, Set<String>> results = new HashMap<Set<String>, Set<String>>();
        Map<Integer, Pattern> patternIndex = new HashMap<>();
        Map<Pattern, Integer> reversePatternIndex = new HashMap<>();
        Set<Pattern> allPatterns = new HashSet<>();
        //construct index for patterns
        int count = 1;
        //cannot use NLR as index because they might contain "\t"
        for (Similarity sim : similarities) {
            allPatterns.add(sim.getPattern1());
            allPatterns.add(sim.getPattern2());
        }

        //create index and reverse index
        for (Pattern p : allPatterns) {
            patternIndex.put(count, p);
            reversePatternIndex.put(p, count);
            count++;
        }

        //run clustering
        try {

            //write data to file
            File f = File.createTempFile("www", "ww");
            String name = f.getAbsolutePath();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(name)));
            for (Similarity sim : similarities) {
                if (sim.getSimilarity() >= similarityThreshold) {
                    writer.println(reversePatternIndex.get(sim.getPattern1()) + "\t"
                            + reversePatternIndex.get(sim.getPattern2()) + "\t"
                            + sim.getSimilarity());
                }
            }
            writer.close();

            //call borderflow
            BorderFlowHard bf = new BorderFlowHard(name);
            bf.hardPartitioning = true;
            Map<Set<String>, Set<String>> clusters = bf.cluster(-1, true, true, true);

            //write output to clusters
            Set<Cluster<Pattern>> patternClusters = new HashSet<Cluster<Pattern>>();
            for (Set<String> seed : clusters.keySet()) {
                Set<String> c = clusters.get(seed);
                Cluster<Pattern> patternCluster = new Cluster<>();
                for (String patternId : c) {
                    patternCluster.add(patternIndex.get(Integer.parseInt(patternId)));
                }
                patternClusters.add(patternCluster);
            }

            return patternClusters;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<Cluster<Pattern>>();
    }

    public static void main(String args[]) {
        Set<Similarity> test = new HashSet<>();
        List<Pattern> p = new ArrayList<>();
        int size = 8;
        for (int i = 0; i < size; i++) {
            Pattern pat = new DefaultPattern();
            pat.setNaturalLanguageRepresentation(i * i + "");
            p.add(pat);
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double sim = Double.parseDouble(p.get(i).getNaturalLanguageRepresentation())
                        - Double.parseDouble(p.get(j).getNaturalLanguageRepresentation());
                sim = 1/(1+sim);
                test.add(new Similarity(p.get(i), p.get(j), sim)); 
            }
        }
        
        PatternClustering pc = new BorderFlowPatternClustering();
        Set<Cluster<Pattern>> clusters = pc.clusterPatterns(test, 0.5);
        for(Cluster<Pattern> cp: clusters)
        {
            for(Pattern pattern: cp)
            {
                System.out.print(pattern.getNaturalLanguageRepresentation()+"\t");
            }
            System.out.print("\n");
        }
    }
}
