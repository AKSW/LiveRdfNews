/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.metric;

import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.extraction.PatternExtractionTest;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.WordnetSimilarityMetric;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityMetricTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public SimilarityMetricTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(SimilarityMetricTest.class);
    }
    
    public void testWordnetSimilarity() {

        WordnetSimilarityMetric m = new WordnetSimilarityMetric();
        Pattern pattern1, pattern2;
        
        pattern1 = new DefaultPattern(", the capital of");
        pattern2 = new DefaultPattern(", the former capital of");
        assertTrue(0.5 == m.calculateSimilarity(pattern1, pattern2));
        
        pattern1 = new DefaultPattern(", the sister of");
        pattern2 = new DefaultPattern(", the mother of");
        assertTrue(0.5 < m.calculateSimilarity(pattern1, pattern2));
    }
}
