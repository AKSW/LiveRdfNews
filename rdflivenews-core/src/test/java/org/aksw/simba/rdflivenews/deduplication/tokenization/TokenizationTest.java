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
        
        NGramTokenizer ngramTokenizer = new NGramTokenizer(1);
        assertEquals(6, ngramTokenizer.tokenize("123456").size());
        assertEquals(new HashSet<String>(Arrays.asList("1", "2", "3", "4", "5", "6")), ngramTokenizer.tokenize("123456"));
        
        ngramTokenizer = new NGramTokenizer(2);
        assertEquals(6, ngramTokenizer.tokenize("123456").size());
        assertEquals(new HashSet<String>(Arrays.asList("45", "56", "23", "6_", "34", "12")), ngramTokenizer.tokenize("123456"));
        
        ngramTokenizer = new NGramTokenizer(3);
        assertEquals(6, ngramTokenizer.tokenize("123456").size());
        assertEquals(new HashSet<String>(Arrays.asList("56_", "123", "456", "234", "345", "6__")), ngramTokenizer.tokenize("123456"));
        
        ngramTokenizer = new NGramTokenizer(4);
        assertEquals(6, ngramTokenizer.tokenize("123456").size());
        assertEquals(new HashSet<String>(Arrays.asList("1234", "3456", "456_", "2345", "6___", "56__")), ngramTokenizer.tokenize("123456"));
        
        ngramTokenizer = new NGramTokenizer(5);
        assertEquals(6, ngramTokenizer.tokenize("123456").size());
        assertEquals(new HashSet<String>(Arrays.asList("23456", "56___", "456__", "6____", "3456_", "12345")), ngramTokenizer.tokenize("123456"));
        
        Tokenizer wordTokenizer = new WordTokenizer();
        assertEquals(5, wordTokenizer.tokenize("This is a test String!").size());
        assertEquals(new HashSet<String>(Arrays.asList("this", "is", "a", "test", "string!")), wordTokenizer.tokenize("This is a test String!"));
        assertEquals(0, wordTokenizer.tokenize("").size());
        assertEquals(0, wordTokenizer.tokenize(null).size());
    }
}
