/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.linking.impl;

import java.util.*;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.deduplication.impl.FastNGram;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.NGramTokenizer;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.linking.Linker;

/**
 *
 * @author ngonga
 */
public class SimpleLinker implements Linker {

    @Override
    public Map<Cluster, Set<String>> link(List<Cluster> clusters, Set<String> propertyLabels, double threshold) {
        Map<String, Set<Cluster>> map = new HashMap<String, Set<Cluster>>();
        for(Cluster c: clusters)
        {
            if(!map.containsKey(c.getName()))
            {
                map.put(c.getName(), new HashSet<Cluster>());
            }
            map.get(c.getName()).add(c);
        }
        
        Map<String, Map<String, Double>> dedupResult = FastNGram.compute(map.keySet(), propertyLabels, new NGramTokenizer(3), threshold);
        Map<Cluster, Set<String>> result = new HashMap<Cluster, Set<String>>();
        
        for(String clusterLabel: dedupResult.keySet())
        {
            for(Cluster c: map.get(clusterLabel))
            result.put(c, result.get(clusterLabel));
        }
        return result;
    }
    
}
