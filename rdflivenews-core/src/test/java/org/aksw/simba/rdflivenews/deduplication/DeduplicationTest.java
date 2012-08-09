package org.aksw.simba.rdflivenews.deduplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.impl.FastDeduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.index.Sentence;
import org.aksw.simba.rdflivenews.pattern.extraction.PatternExtractionTest;
import org.aksw.simba.rdflivenews.refinement.PatternRefiner;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class DeduplicationTest extends TestCase {


    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public DeduplicationTest(String testName) throws InvalidFileFormatException, IOException {
        super(testName);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.INDEX_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "test");
        IndexManager.getInstance().deleteIndex();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(DeduplicationTest.class);
    }
    
    public void testDeduplication() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        this.addSentencesToIndex(this.createSentenceFirstTimeSlice(), 1);
        this.addSentencesToIndex(this.createSentenceSecondTimeSlice(), 2);
        this.addSentencesToIndex(this.createSentenceSecondTimeSlice(), 3);
        
        Deduplication deduplication = new FastDeduplication();
        
        Method method = FastDeduplication.class.getDeclaredMethod("getSource", int.class, int.class);
        method.setAccessible(true);
        
        Set<String> firstTimeSlice = this.createSentenceFirstTimeSlice();
        // do we get the correct amountof sentences for the first timeslice
        assertEquals(5, ((Set<String>) method.invoke(deduplication, 1, 1)).size());
        // are those sentences the correct ones
        assertEquals(firstTimeSlice, method.invoke(deduplication, 1, 1));
        // just to make sure :)
        firstTimeSlice.add("This is a stupid sentence");
        assertNotSame(this.createSentenceSecondTimeSlice(), method.invoke(deduplication, 1, 1));
        
        // zero window should not be possible
        try {
            
            assertEquals(5, ((Set<String>) method.invoke(deduplication, 1, 0)).size());
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
        
        // window larger then possible timeslices should also work
        assertEquals(5, ((Set<String>) method.invoke(deduplication, 1, 2)).size());
        
        method = FastDeduplication.class.getDeclaredMethod("getTarget", int.class, int.class);
        method.setAccessible(true);
        
        Set<String> secondTimeSlice = this.createSentenceSecondTimeSlice();
        
        // the second timeslice is the delta S, so we have 7 entries 
        assertEquals(7, ((Set<String>) method.invoke(deduplication, 1, 2)).size());
        assertEquals(secondTimeSlice, method.invoke(deduplication, 1, 2));
        // this should not work
        secondTimeSlice.add("This is a stupid sentence");
        assertNotSame(this.createSentenceSecondTimeSlice(), method.invoke(deduplication, 1, 2));
        
        // zero window should not be possible
        try {
            
            method.invoke(deduplication, 1, 0);
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
        // zero window should not be possible
        try {
            
            method.invoke(deduplication, 0, 1);
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
        
        method = FastDeduplication.class.getDeclaredMethod("deduplicate", Set.class, Set.class, int.class);
        method.setAccessible(true);
        System.out.println(method.invoke(deduplication, secondTimeSlice, secondTimeSlice, 1));
    }
    
    public void addSentencesToIndex(Set<String> sentences, int timeSlice) {
        
        List<Sentence> newSentences = new ArrayList<Sentence>(); 
        for ( String sent : sentences ) {
            
            Sentence sentence = new Sentence();
            sentence.setArticleUrl("http://article.com/number1");
            sentence.setText(sent.replaceAll("_[A-Z]*", ""));
            sentence.setNerTaggedSentence(sent);
            sentence.setTimeSliceID(timeSlice);
            sentence.setExtractionDate(new Date());
            newSentences.add(sentence);
        }
        
        IndexManager.getInstance().addSentences(newSentences);
    }
    
    public Set<String> createSentenceFirstTimeSlice(){
        
        Set<String> results = new HashSet<String>();
        results.add("It is an enterprise that is meant to send a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        results.add("But it will require partner nations in the gulf to put aside rivalries , share information and coordinate their individual arsenals of interceptor missiles to create a defensive shield encompassing all the regional allies .");
        results.add("Secretary of State Hillary Rodham Clinton , among the first to raise the need for the missile shield three years ago , sought to spur the gulf allies on during a recent visit to Saudi Arabia .");
        results.add("'' We can do even more to defend the gulf through cooperation on ballistic missile defense , '' she said during a session in March of the Gulf Cooperation Council , which includes Bahrain , Kuwait , Oman , Qatar , Saudi Arabia and the United Arab Emirates.");
        results.add("That would include deploying radars to increase the range of early warning coverage across the Persian Gulf , as well as introducing command , control and communications systems that could exchange that information with missile interceptors whose triggers are held by individual countries.");
        
        return results;
    }
    
    public Set<String> createSentenceSecondTimeSlice(){
        
        Set<String> results = new HashSet<String>();
        results.add("It is an enterprise that is meant to send a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        results.add("It is an enterprise that is meant to send a pointed message to Berlin, and that becomes more urgent as tensions with Iran rise .");
        results.add("It is not an enterprise that is meant to be a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        results.add("But it will require partner nations in the gulf to put aside rivalries , share information and coordinate their individual arsenals of interceptor missiles to create a defensive shield encompassing all the regional allies .");
        results.add("Secretary of State Hillary Rodham Clinton , among the first to raise the need for the missile shield three years ago , sought to spur the gulf allies on during a recent visit to Saudi Arabia .");
        results.add("'' We can do even more to defend the gulf through cooperation on ballistic missile defense , '' she said during a session in March of the Gulf Cooperation Council , which includes Bahrain , Kuwait , Oman , Qatar , Saudi Arabia and the United Arab Emirates.");
        results.add("That would include deploying radars to increase the range of early warning coverage across the Persian Gulf , as well as introducing command , control and communications systems that could exchange that information with missile interceptors whose triggers are held by individual countries.");
        
        return results;
    }
}