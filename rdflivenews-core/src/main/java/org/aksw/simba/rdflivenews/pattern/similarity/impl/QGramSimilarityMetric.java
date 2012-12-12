/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;

import edu.stanford.nlp.util.StringUtils;

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

        List<String> patternOne = new ArrayList<String>(Arrays.asList(pattern1.getNaturalLanguageRepresentation().toLowerCase().split(" ")));
        patternOne.removeAll(Constants.STOP_WORDS);
        String firstPattern = StringUtils.join(patternOne, " ");
        
        List<String> patternTwo = new ArrayList<String>(Arrays.asList(pattern2.getNaturalLanguageRepresentation().toLowerCase().split(" ")));
        patternTwo.removeAll(Constants.STOP_WORDS);
        String secondPattern = StringUtils.join(patternTwo, " ");
        
        return this.metric.getSimilarity(firstPattern, secondPattern);
    }
}
