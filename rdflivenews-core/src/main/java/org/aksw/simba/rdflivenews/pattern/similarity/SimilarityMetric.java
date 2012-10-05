/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity;

import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface SimilarityMetric {

    public double calculateSimilarity(Pattern pattern1, Pattern pattern2);
}
