/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.linking.impl;

import com.hp.hpl.jena.query.*;
import de.uni_leipzig.simba.cache.MemoryCache;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.execution.ExecutionEngine;
import de.uni_leipzig.simba.execution.Instruction;
import de.uni_leipzig.simba.measures.string.QGramSimilarity;
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
    public static final String endpoint = "http://dbpedia.org/sparql";

    @Override
    public Mapping link(Set<Cluster<org.aksw.simba.rdflivenews.pattern.Pattern>> clusters, double threshold) {
        return quadraticComparison(clusters, getDBpediaPropertyLabels(), threshold);
    }

    public Mapping link(Set<Cluster<Pattern>> clusters, Map<String, Set<String>> propertyLabels, double threshold) {
        MemoryCache source = new MemoryCache();
        for (Cluster c : clusters) {
            source.addTriple(c.getUri(), "label", c.getName());
        }

        MemoryCache target = new MemoryCache();

        for (String uri : propertyLabels.keySet()) {
            for (String label : propertyLabels.keySet()) {
                target.addTriple(uri, "label", label);
            }
        }
        System.out.println("Linking ....");
        Instruction inst = new Instruction(Instruction.Command.RUN, "qgrams(x.label, y.label)", threshold + "", -1, -1, -1);
        ExecutionEngine ee = new ExecutionEngine(source, target, "?x", "?y");
        Mapping m = ee.executeRun(inst);
        return m;
    }
    
    public Mapping quadraticComparison(Set<Cluster<Pattern>> source, Map<String, Set<String>> target, double threshold)
    {
        QGramSimilarity sim = new QGramSimilarity();
        Mapping m = new Mapping();
        double score;
        for(Cluster c: source)
        {
            Set<String> names = new HashSet<String>();
            names.add(c.getName());
            for(String t: target.keySet())
            {
                score = sim.getSimilarity(c.getName(), new ArrayList<String>(target.get(t)).get(0));
               // System.out.println("comparing "+names+" and "+target.get(t)+" Got "+score);
                if(score > threshold)
                    m.add(c.getUri(), t, score);
            }
        }
        return m;
    }

        /**
     * Returns a map of property labels to a set of properties that have that
     * given label
     *
     * @return
     */
    public static Map<String, Set<String>> getDBpediaPropertyLabels() {
        String query = "SELECT ?p ?l FROM <http://dbpedia.org> WHERE {?p a <http://www.w3.org/2002/07/owl#ObjectProperty>. "
                + "?p <http://www.w3.org/2000/01/rdf-schema#label> ?l . FILTER( lang(?l) = 'en' )} ";
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        try {
            Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
            QueryExecution qexec;
            qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);

            ResultSet results = qexec.execSelect();
            String p, l;
            while (results.hasNext()) {

                QuerySolution soln = results.nextSolution();
                l = soln.get("l").asLiteral().getLexicalForm();
                p = soln.getResource("p").getURI();
                if (!map.containsKey(p)) {
                    map.put(p, new HashSet<String>());
                }
                map.get(p).add(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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
        Linker linker = new LimesLinker();
        System.out.println("=========================" + linker.link(set, 0.5));
    }
}
