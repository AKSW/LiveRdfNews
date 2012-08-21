/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication.similarity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.NGramTokenizer;

import junit.framework.TestCase;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityTest extends TestCase {

    public void testSimilarity() {
        
        Set<String> oneTwoThreeFour     = new HashSet<String>(Arrays.asList("1", "2", "3", "4"));
        Set<String> fiveSixSevenEight   = new HashSet<String>(Arrays.asList("5", "6", "7", "8"));
        
        Similarity similarity = new Similarity(new NGramTokenizer(3));
        assertEquals(1.0, similarity.getSimilarity("1234", "1234"));
        assertEquals(0.0, similarity.getSimilarity("1234", "5678"));
        assertEquals(1.0, similarity.getSimilarity(oneTwoThreeFour, oneTwoThreeFour));
        assertEquals(0.0, similarity.getSimilarity(oneTwoThreeFour, fiveSixSevenEight));
        
        Similarity similarity3Gram = new Similarity(new NGramTokenizer(3));
        assertEquals(0.45, similarity3Gram.getSimilarity("acquired", "acquires"), 0.01);
    }
}
