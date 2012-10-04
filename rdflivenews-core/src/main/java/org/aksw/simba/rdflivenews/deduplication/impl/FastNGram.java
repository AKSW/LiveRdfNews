/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.index.Index;
import org.aksw.simba.rdflivenews.deduplication.similarity.Similarity;
import org.aksw.simba.rdflivenews.deduplication.tokenization.Tokenizer;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.WordTokenizer;

/**
 *
 * @author ngonga
 */
public class FastNGram {
    
    /**
     * Runs the deduplication
     * @param source Source strings
     * @param target Target strings
     * @param q Parameter for the tokenizer
     * @param threshold SimilarityMetric threshold
     * @return Map source -> target -> similarity
     */
    public static Map<String, Map<String, Double>> compute(Set<String> source, Set<String> target, Tokenizer tokenizer, double threshold) {
        
        if(source.isEmpty() || target.isEmpty()) return new HashMap<String, Map<String, Double>>();
        
        Index index = new Index(tokenizer, threshold);
        double kappa = (1 + threshold) / threshold;
        Similarity sim = new Similarity(tokenizer);
        
        Map<String, Set<String>> targetTokens = new HashMap<String, Set<String>>();
        Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
        //index target
        for (String t : target) {
            targetTokens.put(t, index.addString(t));
        }

        Set<String> candidates1;
        Set<String> candidates2;
        //run similarity computation
        Map<String, Double> buffer;
        for (String s : source) {
            Set<Integer> allSizes = index.getAllSizes();
            Set<String> sourceTokens = tokenizer.tokenize(s);
            double sourceSize = (double) sourceTokens.size();
            candidates1 = new HashSet<String>();
            candidates2 = new HashSet<String>();
            buffer = new HashMap<String, Double>();
            for (int size = (int)Math.ceil(sourceSize*threshold); size<= (int)Math.floor(sourceSize/threshold); size++)
            {
                if(allSizes.contains(size))
                {
                    Map<String, Set<String>> stringsOfSize = index.getStrings(size);
                    Map<String, Integer> countMap = new HashMap<String, Integer>();
                    for (String token : sourceTokens) {
                        if (stringsOfSize.containsKey(token)) {
                            //take each string and add it to the count map
                            Set<String> candidates = stringsOfSize.get(token);
                            for (String candidate : candidates) {
                                if (!countMap.containsKey(candidate)) {
                                    countMap.put(candidate, 0);
                                }
                                countMap.put(candidate, countMap.get(candidate) + 1);
                            }
                        }
                    }
                    // now apply filtering |X \cap Y| \geq \kappa(|X| + |Y|)
                    for (String candidate : countMap.keySet()) {
                        double count = (double) countMap.get(candidate);
                        if (kappa*count >= (sourceSize + size)) {
                            double similarity = sim.getSimilarity(targetTokens.get(candidate), sourceTokens);
                            if (similarity >= threshold) {
                                buffer.put(candidate, similarity);
                            }
                        }
                    }
                }
            }

            if (!buffer.isEmpty()) {
                result.put(s, buffer);
            }
        }
        return result;
    }
}
