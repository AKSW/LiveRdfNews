/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.linking.impl;

import com.hp.hpl.jena.query.*;
import java.util.*;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.cluster.labeling.DefaultClusterLabeling;
import org.aksw.simba.rdflivenews.deduplication.impl.FastNGram;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.NGramTokenizer;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.linking.Linker;

/**
 *
 * @author ngonga
 */
public class SimpleLinker implements Linker {

    public static final String endpoint = "http://dbpedia.org/sparql";

    @Override
    public Map<Cluster<Pattern>, Set<String>> link(Set<Cluster<Pattern>> clusters, double threshold) {
        return link(clusters, getDBpediaPropertyLabels(), threshold);
    }

    public Map<Cluster<Pattern>, Set<String>> link(Set<Cluster<Pattern>> clusters, Map<String, Set<String>> propertyLabels, double threshold) {
        Map<String, Set<Cluster<Pattern>>> map = new HashMap<>();
        for (Cluster<Pattern> c : clusters) {
            if (!map.containsKey(c.getName())) {
                map.put(c.getName(), new HashSet<Cluster<Pattern>>());
            }
            map.get(c.getName()).add(c);
        }
        Map<String, Map<String, Double>> dedupResult = FastNGram.compute(map.keySet(), propertyLabels.keySet(), new NGramTokenizer(3), threshold);
        Map<Cluster<Pattern>, Set<String>> result = new HashMap<>();

        for (String clusterLabel : dedupResult.keySet()) {
            // get cluster
            for (Cluster<Pattern> c : map.get(clusterLabel)) {
                result.put(c, new HashSet<String>());
                // get property label
                for (String property : dedupResult.get(clusterLabel).keySet()) {
                    //get property
                    for (String uri : propertyLabels.get(property)) {
                        result.get(c).add(uri);
                    }
                }
            }
        }
        return result;
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
    		
    		c.add(new DefaultPattern(s,s));
    	}
    	DefaultClusterLabeling labeler = new DefaultClusterLabeling();
    	
    	Set<Cluster<Pattern>> set = new HashSet<Cluster<Pattern>>();
    	set.add(c);
    	
    	labeler.labelCluster(set);
    	
    	SimpleLinker linker = new SimpleLinker();
    	linker.link(set, 0.0);
	}
}
