package org.aksw.simba.rdflivenews.deduplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.impl.FastDeduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.index.Sentence;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.NumericUtils;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


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
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(DeduplicationTest.class);
    }
    
    /**
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void testGetSource() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        // prepare the index
        IndexManager.getInstance().deleteIndex();
        this.addSentencesToLuceneIndex();

        List<String> firstTimeSlice = this.createSentenceFirstTimeSlice();
        
        Deduplication deduplication = null;
        
        // do we get the correct amount of sentences for the first timeslice
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(1);
        deduplication.setWindowSize(1);
        assertEquals(5, deduplication.getSource().size());
        
        // are those sentences the correct ones
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(1);
        deduplication.setWindowSize(1);
        assertEquals(new HashSet<String>(firstTimeSlice), deduplication.getSource());
        
        // just to make sure :)
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(1);
        deduplication.setWindowSize(1);
        firstTimeSlice.add("This is a stupid sentence");
        assertNotSame(firstTimeSlice, deduplication.getSource());
        
        // window larger then possible timeslices should also work
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(1);
        deduplication.setWindowSize(2);
        assertEquals(5, deduplication.getSource().size());
    }
    
    /**
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void testGetTarget() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        // prepare the index
        IndexManager.getInstance().deleteIndex();
        this.addSentencesToLuceneIndex();

        List<String> secondTimeSlice = this.createSentenceSecondTimeSlice();

        Deduplication deduplication = null;

        // the second timeslice is the delta S, so we have 6 entries
        // there is 1 duplicate sentence in the list of 7 sentences 
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(2);
        deduplication.setWindowSize(1);
        assertEquals(7, deduplication.getTarget().size());
        
        // lets see if we get the correct sentences, use the hashset to remove duplicates
        // since only works if we have a similarity threshold of 1
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(2);
        deduplication.setWindowSize(1);
        assertEquals(new HashSet<String>(secondTimeSlice), deduplication.getTarget());
        
        // this should not work
        deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(2);
        deduplication.setWindowSize(1);
        secondTimeSlice.add("This is a stupid sentence");
        assertNotSame(secondTimeSlice, deduplication.getTarget());
    }
    
    public void testWrongInput() {
        
        Deduplication deduplication = new FastDeduplication();
        deduplication.setFromTimeSlice(1);
        deduplication.setToTimeSlice(0);
        deduplication.setWindowSize(1);
        
        // zero window should not be possible
        try {
            
            deduplication.getTarget();
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
        // from time slice needs to be greate than 0
        try {
            
            deduplication.setFromTimeSlice(-1);
            deduplication.setToTimeSlice(1);
            deduplication.getTarget();
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
        
        // zero window should not be possible
        try {
            
            deduplication.setFromTimeSlice(1);
            deduplication.setToTimeSlice(0);
            deduplication.setWindowSize(0);
            assertEquals(5, deduplication.getSource().size());
            fail("this should have thrown an exception");
        }
        catch (Exception expected) { /* expected don't do anything */ }
    }
    
    public void testDeduplication() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {        
        // prepare the index
        IndexManager.getInstance().deleteIndex();
        this.addSentencesToLuceneIndex();
        
        int fromTimeSliceId = 0;
        int toTimeSliceId   = 1;
        int window          = 1;
        
        Deduplication deduplication = new FastDeduplication();
        deduplication.runDeduplication(fromTimeSliceId, toTimeSliceId, window);

        TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);
        LuceneManager.query(IndexManager.INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, NumericUtils.intToPrefixCoded(1))), collector);
        assertEquals(1, collector.getTotalHits());
        assertEquals(5, IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(toTimeSliceId).size());
        
        fromTimeSliceId = 1;
        toTimeSliceId   = 2;
        window          = 1;
        
        deduplication = new FastDeduplication();
        deduplication.runDeduplication(fromTimeSliceId, toTimeSliceId, window);
        
        collector = TopScoreDocCollector.create(100, false);
        LuceneManager.query(IndexManager.INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, NumericUtils.intToPrefixCoded(2))), collector);
        assertEquals(6, collector.getTotalHits());
        assertEquals(2, IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(toTimeSliceId).size());
    }
    
    /**
     * 
     */
    private void addSentencesToLuceneIndex() {
        
        this.addSentencesToIndex(this.createSentenceFirstTimeSlice(), 1);
        this.addSentencesToIndex(this.createSentenceSecondTimeSlice(), 2);
        this.addSentencesToIndex(this.createSentenceSecondTimeSlice(), 3);
    }
    
    /**
     * 
     * @param sentences
     * @param timeSlice
     */
    public void addSentencesToIndex(List<String> sentences, int timeSlice) {
        
        List<Sentence> newSentences = new ArrayList<Sentence>(); 
        for ( String sent : sentences ) {
            
            Sentence sentence = new Sentence();
            sentence.setArticleUrl("http://article.com/number1");
            sentence.setText(sent.replaceAll("_[A-Z]*", ""));
            sentence.setNerTaggedSentence(sent);
            sentence.setPosTaggedSentence(sent);
            sentence.setTimeSliceID(timeSlice);
            sentence.setExtractionDate(new Date());
            newSentences.add(sentence);
        }
        
        IndexManager.getInstance().addSentences(newSentences);
    }
    
    /**
     * 
     * @return
     */
    public List<String> createSentenceFirstTimeSlice(){
        
        List<String> results = new ArrayList<String>();
        results.add("It is an enterprise that is meant to send a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        results.add("But it will require partner nations in the gulf to put aside rivalries , share information and coordinate their individual arsenals of interceptor missiles to create a defensive shield encompassing all the regional allies .");
        results.add("Secretary of State Hillary Rodham Clinton , among the first to raise the need for the missile shield three years ago , sought to spur the gulf allies on during a recent visit to Saudi Arabia .");
        results.add("'' We can do even more to defend the gulf through cooperation on ballistic missile defense , '' she said during a session in March of the Gulf Cooperation Council , which includes Bahrain , Kuwait , Oman , Qatar , Saudi Arabia and the United Arab Emirates.");
        results.add("That would include deploying radars to increase the range of early warning coverage across the Persian Gulf , as well as introducing command , control and communications systems that could exchange that information with missile interceptors whose triggers are held by individual countries.");
        results.add("That would include deploying radars to increase the range of early warning coverage across the Persian Gulf , as well as introducing command , control and communications systems that could exchange that information with missile interceptors whose triggers are held by individual countries.");
        
        return results;
    }
    
    /**
     * 
     * @return
     */
    public List<String> createSentenceSecondTimeSlice(){
        
        List<String> results = new ArrayList<String>();
        // duplicate in target and in source
        results.add("It is an enterprise that is meant to send a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        results.add("It is an enterprise that is meant to send a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise ."); 
        // new
        results.add("It is not an enterprise that is meant to be a pointed message to Tehran, and that becomes more urgent as tensions with Iran rise .");
        // new
        results.add("This is a completely different sentence which has absolutely nothing to do with the previous sentences because it 's totally different .");
        // duplicated to source
        results.add("But it will require partner nations in the gulf to put aside rivalries , share information and coordinate their individual arsenals of interceptor missiles to create a defensive shield encompassing all the regional allies .");
        // duplicated to source
        results.add("Secretary of State Hillary Rodham Clinton , among the first to raise the need for the missile shield three years ago , sought to spur the gulf allies on during a recent visit to Saudi Arabia .");
        // duplicated to source
        results.add("'' We can do even more to defend the gulf through cooperation on ballistic missile defense , '' she said during a session in March of the Gulf Cooperation Council , which includes Bahrain , Kuwait , Oman , Qatar , Saudi Arabia and the United Arab Emirates.");
        // duplicated to source
        results.add("That would include deploying radars to increase the range of early warning coverage across the Persian Gulf , as well as introducing command , control and communications systems that could exchange that information with missile interceptors whose triggers are held by individual countries.");
        
        return results;
    }
}