/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetSimilarityMetric implements SimilarityMetric {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric#calculateSimilartiy(org.aksw.simba.rdflivenews.pattern.Pattern, org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public double calculateSimilartiy(Pattern pattern1, Pattern pattern2) {

        return 0;
    }
}
