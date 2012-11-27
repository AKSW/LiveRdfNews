/**
 * 
 */
package org.aksw.simba.rdflivenews.crawler;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.aksw.simba.rdflivenews.RdfLiveNewsCrawler;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.index.Sentence;
import org.aksw.simba.rdflivenews.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.apache.log4j.Logger;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ArticleCrawlerThread extends Thread {

    private HtmlFetcher fetcher         = new HtmlFetcher();
    private DateFormat format           = new SimpleDateFormat("yyyy/MM/dd");
    private Logger logger               = Logger.getLogger(ArticleCrawlerThread.class);
    private BlockingQueue<String> queue = null;
    
    public ArticleCrawlerThread(BlockingQueue<String> queue) {

        this.queue  = queue;
    }

    /**
     * 
     */
    @Override public void run() {

        // thread needs to run forever
        while (true) {

            // if there is an uri on the queue the try to crawl the rss entry
            String uri = null;
            try {
                
                uri = queue.take();
            }
            catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
                
            logger.debug("Starting to crawl uri: " + uri);
            List<Sentence> sentences = crawlArticle(uri);
            
            if ( sentences != null ) IndexManager.getInstance().addSentences(sentences);    
            
            // wait so that we dont run this method over and over if no rss feeds are avaiable
            try {
                
                sleep(RdfLiveNewsCrawler.CONFIG.getLongSetting("crawl", "crawlerWaitTime"));
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Returns a new article or null if we could not generate it.
     * 
     * @param url
     * @return
     */
    public List<Sentence> crawlArticle(String url) {
        
        // we need to save every article to the db because we need to know if 
        // we have crawled this article before
        List<Sentence> sentences = null;
        
        try {
            
            JResult res = fetcher.fetchAndExtract(url, RdfLiveNewsCrawler.CONFIG.getIntegerSetting("crawl", "timeout"), true);
            
            // some articles are read protected so they only show a small warning
            if ( res.getText() != null && res.getText().length() > 1000 ) {

                // create the list with the correct size, no need to expand
                List<String> parsedSentence = StanfordNLPSentenceBoundaryDisambiguation.getSentences(res.getText());
                sentences = new ArrayList<Sentence>(parsedSentence.size());
                
                for ( String sentenceText : parsedSentence) {
                    
                    Sentence sentence = new Sentence();
                    sentence.setArticleUrl(url);
                    sentence.setText(sentenceText);
                    sentence.setTimeSliceID(RdfLiveNewsCrawler.TIME_SLICE_ID);
                    sentence.setExtractionDate(this.parseDate(res.getDate()));
                    
                    sentences.add(sentence);
                }
            }
            
            return sentences;
        }
        catch (IllegalArgumentException iae) {
            
            logger.debug("Error crawling html from url: " + url, iae);
        }
        catch (FileNotFoundException iae) {
            
            logger.debug("Error crawling html from url: " + url, iae);
        }
        catch (Exception e) {
            
            logger.debug("Error crawling html from url: " + url, e);
        }
        
        return null;
    }
    
    /**
     * 
     * @param date
     * @return
     */
    private Date parseDate(String date) {

        try {
            
            return date != null ? format.parse(date) : new Date();
        }
        catch ( ParseException pe ) {
            
            return new Date();
        }
    }
}
