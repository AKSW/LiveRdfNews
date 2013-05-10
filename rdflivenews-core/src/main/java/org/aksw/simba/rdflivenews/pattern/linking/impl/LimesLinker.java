/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.linking.impl;

import de.uni_leipzig.simba.cache.MemoryCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.ExecutionEngine;
import de.uni_leipzig.simba.execution.Instruction;
import java.util.*;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.linking.Linker;

/**
 *
 * @author ngonga
 */
public class LimesLinker implements Linker {

    @Override
    public Mapping link(Set<Cluster<org.aksw.simba.rdflivenews.pattern.Pattern>> clusters, double threshold) {
        return link(clusters, SimpleLinker.getDBpediaPropertyLabels(), threshold);
    }

    public Mapping link(Set<Cluster<Pattern>> clusters, Map<String, Set<String>> propertyLabels, double threshold) {
        MemoryCache source = new MemoryCache();
        for (Cluster c : clusters) {
            source.addTriple(c.getUri(), "label", c.getName());
        }

        MemoryCache target = new MemoryCache();

        for (String label : propertyLabels.keySet()) {
            for (String uri : propertyLabels.keySet()) {
                target.addTriple(uri, "label", label);
            }
        }

        Instruction inst = new Instruction(Instruction.Command.RUN, "trigrams(x.label, y.label)", threshold + "", -1, -1, -1);
        ExecutionEngine ee = new ExecutionEngine(source, target, "?x", "?y");
        Mapping m = ee.executeRun(inst);
        return m;
    }

    public static void main(String[] args) {

        RdfLiveNews.init();

        List<String> names = Arrays.asList("manager",
                ", director of",
                "director",
                ", the director of",
                "campaign manager");

        Cluster<Pattern> c = new Cluster<Pattern>();
        for (String s : names) {
            c.add(new DefaultPattern(s, s));
        }
        DefaultClusterLabeling labeler = new DefaultClusterLabeling();

        Set<Cluster<Pattern>> set = new HashSet<Cluster<Pattern>>();
        set.add(c);

        labeler.labelCluster(set);
        System.out.println(c.getName());
        SimpleLinker linker = new SimpleLinker();
        System.out.println(linker.link(set, 0.8));
    }
}
