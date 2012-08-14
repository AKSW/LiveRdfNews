package org.aksw.simba.rdflivenews.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.aksw.simba.rdflivenews.NewsCrawler;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.apache.lucene.index.CorruptIndexException;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import junit.framework.TestCase;


public class IndexTest extends TestCase {

    public IndexTest() throws InvalidFileFormatException, IOException {
        
        // load the config, we dont need to configure logging because the log4j config is on the classpath
        NewsCrawler.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/newscrawler-config.ini")));
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.INDEX_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "test");
        IndexManager.getInstance().deleteIndex();
    }
    
    public void testGetHighestTimeSliceId() throws CorruptIndexException, IOException {
        
        // no documents in the index so should be 0
        assertEquals(0, IndexManager.getInstance().getNumberOfDocuments());
        assertEquals(0, IndexManager.getInstance().getHighestTimeSliceId());
        this.addSentencesToIndex();
        assertEquals(1000, IndexManager.getInstance().getNumberOfDocuments());
        assertEquals(999, IndexManager.getInstance().getHighestTimeSliceId());
    }
    
    private void addSentencesToIndex() {
        
        List<Sentence> sentences = new ArrayList<Sentence>();
        for ( int i = 0 ; i < 1000 ; i++ ) {
            
            Sentence sentence = new Sentence();
            sentence.setArticleUrl("http://article.com/number1");
            sentence.setText("Test");
            sentence.setNerTaggedSentence("Test_PERSON");
            sentence.setTimeSliceID(i);
            sentence.setExtractionDate(new Date());
            sentences.add(sentence);
        }
        
        IndexManager.getInstance().addSentences(sentences);
    }
}
