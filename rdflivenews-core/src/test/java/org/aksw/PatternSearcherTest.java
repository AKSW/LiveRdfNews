package org.aksw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.patternsearch.PatternSearcher;
import org.aksw.simba.rdflivenews.patternsearch.impl.NamedEntityTagPatternSearcher;
import org.aksw.simba.rdflivenews.patternsearch.impl.PartOfSpeechTagPatternSearcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple TESTCRAWLER.
 */
public class PatternSearcherTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public PatternSearcherTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(PatternSearcherTest.class);
    }

    /**
     * @throws Exception 
     */
    public void testNerPatternMerging() throws Exception {

        String nerTestString = "This_OTHER was_OTHER Microsoft_ORGANIZATION BARACK_PERSON of_OTHER clean_OTHER USA_ORGANIZATION Nation_ORGANIZATION by_OTHER this_OTHER test_PLACE test1_PLACE test2_PLACE ._OTHER";
        
        PatternSearcher patternSearcher = new NamedEntityTagPatternSearcher();
        List<String> mergedSentence = patternSearcher.mergeTagsInSentences(nerTestString);
        List<String> goldSentence   = new ArrayList<String>(Arrays.asList("This_OTHER", "was_OTHER", "Microsoft_ORGANIZATION", "BARACK_PERSON", "of_OTHER", "clean_OTHER", "USA Nation_ORGANIZATION", "by_OTHER", "this_OTHER", "test test1 test2_PLACE", "._OTHER")); 
        
        assertTrue(mergedSentence.equals(goldSentence));
        
        long start = System.nanoTime();
        List<Pattern> patterns = new ArrayList<Pattern>(patternSearcher.extractPatterns(nerTestString, 1));
        System.out.println("NER Took " + (System.nanoTime() - start) + "ns");
        assertTrue(patterns.size() == 2);
        assertTrue(patterns.get(0).getNaturalLanguageRepresentation().equals("of clean") ^ patterns.get(1).getNaturalLanguageRepresentation().equals("of clean"));
        assertTrue(patterns.get(0).getNaturalLanguageRepresentation().equals("by this") ^ patterns.get(1).getNaturalLanguageRepresentation().equals("by this"));
    }
    
    public void testPosPatternMerging() {
        
        String posTestString = "Barack_NNP Obama_NNP was_VBD the_DT first_JJ President_NNPS who_WDT is_VBZ head_NN of_IN Factory_NNP Inc._NNP ._.";
        
        PatternSearcher patternSearcher = new PartOfSpeechTagPatternSearcher();
        List<String> mergedSentence = patternSearcher.mergeTagsInSentences(posTestString);
        List<String> goldSentence   = new ArrayList<String>(Arrays.asList("Barack Obama_NNP", "was_VBD", "the_DT" ,"first_JJ", "President_NNP", "who_WDT", "is_VBZ", "head_NN", "of_IN", "Factory Inc._NNP", "._.")); 
        
        assertTrue(mergedSentence.equals(goldSentence));
        
        long start = System.nanoTime();
        List<Pattern> patterns = new ArrayList<Pattern>(patternSearcher.extractPatterns(posTestString, 1));
        System.out.println("POS Took " + (System.nanoTime() - start) + "ns");
        assertTrue(patterns.size() == 2);
        assertTrue(patterns.get(0).getNaturalLanguageRepresentation().equals("was the first") ^ patterns.get(1).getNaturalLanguageRepresentation().equals("was the first"));
        assertTrue(patterns.get(0).getNaturalLanguageRepresentation().equals("who is head of") ^ patterns.get(1).getNaturalLanguageRepresentation().equals("who is head of"));
    }
}
