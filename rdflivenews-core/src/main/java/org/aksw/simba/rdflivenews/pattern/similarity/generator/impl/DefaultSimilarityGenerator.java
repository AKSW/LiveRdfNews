/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.SimilarityGenerator;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultSimilarityGenerator implements SimilarityGenerator {

    private SimilarityMetric similarityMetric;
    private List<Pattern> patterns;

    public DefaultSimilarityGenerator(SimilarityMetric similarityMetric, List<Pattern> patterns) {

        this.similarityMetric = similarityMetric;
        this.patterns = patterns;
    }

    @Override
    public Set<Similarity> calculateSimilarities() {

        Set<Similarity> similarities = new HashSet<>();
        
        for ( Pattern pattern1 : this.patterns ) {
            for ( Pattern pattern2 : this.patterns ) {

                // avoid having identities in the set
                if ( !pattern1.equals(pattern2) )
                    similarities.add(new Similarity(pattern1, pattern2, this.similarityMetric.calculateSimilartiy(pattern1, pattern2)));
            }
        }
        return similarities; 
    }
}
