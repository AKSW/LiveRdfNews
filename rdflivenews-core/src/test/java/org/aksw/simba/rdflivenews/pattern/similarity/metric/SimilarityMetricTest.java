/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.metric;

import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.extraction.PatternExtractionTest;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.impl.TakelabSimilarityMetric;
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

        SimilarityMetric wordnetMetric = new WordnetSimilarityMetric();
        Pattern pattern1, pattern2, pattern3, pattern4, pattern5, pattern6;
        
        pattern1 = new DefaultPattern(", the capital of");
        pattern2 = new DefaultPattern(", the former capital of");
        System.out.println("', the capital of' & ', the former capital of': " + wordnetMetric.calculateSimilarity(pattern1, pattern2));
        assertTrue(0.5 == wordnetMetric.calculateSimilarity(pattern1, pattern2));
        
        pattern3 = new DefaultPattern(", the sister of");
        pattern4 = new DefaultPattern(", the mother of");
        System.out.println("', the sister of' & ', the mother of': " + wordnetMetric.calculateSimilarity(pattern1, pattern2));
        assertTrue(0.5 < wordnetMetric.calculateSimilarity(pattern3, pattern4));
        
        SimilarityMetric takelabMetric = new TakelabSimilarityMetric();
        
        System.out.println(takelabMetric.calculateSimilarity(pattern1, pattern2));
        System.out.println(takelabMetric.calculateSimilarity(pattern3, pattern4));
        
        pattern5 = new DefaultPattern("first baseman");
        pattern6 = new DefaultPattern("was the first");
        System.out.println(takelabMetric.calculateSimilarity(pattern5, pattern6));
    }
}
