/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface SimilarityMetric {

    public double calculateSimilarity(Pattern pattern1, Pattern pattern2);
}
