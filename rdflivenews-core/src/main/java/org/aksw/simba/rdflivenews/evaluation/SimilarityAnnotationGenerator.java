/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.apache.commons.io.FileUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityAnnotationGenerator {

    public static void main(String[] args) throws IOException {

    	PatternClustering clustering = new BorderFlowPatternClustering();
    	for ( Cluster<Pattern> cluster : clustering.clusterPatterns(createSimilarities(new QGramAndWordnetSimilarityMetric()),	 0.3) ){
    		
    		for ( Pattern p : cluster) System.out.println(p.getNaturalLanguageRepresentation());
    		System.out.println();
    		System.out.println();
    	}
    }
    
    private static Set<Similarity> createSimilarities(SimilarityMetric metric) throws IOException {

        List<String> lines = FileUtils.readLines(new File("/Users/gerb/Desktop/patterns1percent5occ.pattern"));

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
}