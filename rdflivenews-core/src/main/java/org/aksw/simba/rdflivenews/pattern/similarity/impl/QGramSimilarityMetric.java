/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;

import uk.ac.shef.wit.simmetrics.SimpleExample;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QGramSimilarityMetric implements SimilarityMetric {
    
    private AbstractStringMetric metric = new QGramsDistance();

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric#calculateSimilartiy(org.aksw.simba.rdflivenews.pattern.Pattern, org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public double calculateSimilarity(Pattern pattern1, Pattern pattern2) {

        return this.metric.getSimilarity(pattern1.getNaturalLanguageRepresentation(), pattern2.getNaturalLanguageRepresentation());
    }

}
