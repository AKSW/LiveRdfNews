/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication.tokenization;

import java.util.Arrays;
import java.util.HashSet;

import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.NGramTokenizer;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.WordTokenizer;

import junit.framework.TestCase;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TokenizationTest extends TestCase {

    /**
     * @param name
     */
    public TokenizationTest(String name) {

        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();
    }
    
    public void testTokenization() {
        
        NGramTokenizer ngramTokenizer = new NGramTokenizer();
        System.out.println(ngramTokenizer.tokenize("123456", 1));
        System.out.println(ngramTokenizer.tokenize("123456", 2));
        System.out.println(ngramTokenizer.tokenize("123456", 3));
        System.out.println(ngramTokenizer.tokenize("123456", 4));
        System.out.println(ngramTokenizer.tokenize("123456", 5));
        
        Tokenizer wordTokenizer = new WordTokenizer();
        assertEquals(5, wordTokenizer.tokenize("This is a test String!", 0).size());
        assertEquals(new HashSet<String>(Arrays.asList("this", "is", "a", "test", "string!")), wordTokenizer.tokenize("This is a test String!", 0));
        assertEquals(0, wordTokenizer.tokenize("", 0).size());
        assertEquals(0, wordTokenizer.tokenize(null, 0).size());
    }
}
