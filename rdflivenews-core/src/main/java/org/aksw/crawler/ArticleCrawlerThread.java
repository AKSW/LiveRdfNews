/**
 * 
 */
package org.aksw.crawler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.aksw.NewsCrawler;
import org.aksw.concurrency.QueueManager;
import org.aksw.index.IndexManager;
import org.aksw.index.NewsArticle;
import org.aksw.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.aksw.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.apache.log4j.Logger;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ArticleCrawlerThread extends Thread {

    private HtmlFetcher fetcher = new HtmlFetcher();
    private DateFormat format   = new SimpleDateFormat("yyyy/MM/dd");
    private Logger logger       = Logger.getLogger(ArticleCrawlerThread.class);
    
    // every crawler gets a pos and ner tagger
    private StanfordNLPPartOfSpeechTagger posTagger         = new StanfordNLPPartOfSpeechTagger();
    private StanfordNLPNamedEntityRecognition nerTagger     = new StanfordNLPNamedEntityRecognition();
    
    /**
     * 
     */
    @Override public void run() {

        // this thread should also run forever
        while ( true ) {
            
            // if there is an uri on the queue the try to crawl the rss entry
            String uri = QueueManager.getInstance().removeArticleFromCrawlQueue();
            if ( uri != null ) {
                
                logger.debug("Starting to crawl uri: " + uri);
                NewsArticle article = crawlArticle(uri);
                
                if ( article != null ) 
                    IndexManager.getInstance().addNewsArticle(article);    
            }
            // wait so that we dont run this method over and over if no rss feeds are avaiable
            try {
                
                sleep(NewsCrawler.CONFIG.getLongSetting("crawl", "crawlerWaitTime"));
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
    public NewsArticle crawlArticle(String url) {
        
        // we need to save every article to the db because we need to know if 
        // we have crawled this article before
        NewsArticle article = new NewsArticle();
        
        try {
            
            JResult res = fetcher.fetchAndExtract(url, NewsCrawler.CONFIG.getIntegerSetting("crawl", "timeout"), true);
            
            article.setArticleUrl(url);
            article.setImageUrl(res.getImageUrl());
            article.setTitle(res.getTitle());
            
            // some articles are read protected
            if ( res.getText() != null && res.getText().length() > 1000 ) {

                article.setText(res.getText());
                article.setNerTaggedText(nerTagger.getAnnotatedSentences(article.getText()));
                article.setPosTaggedText(posTagger.getAnnotatedSentence(article.getText()));
            }
            article.setTimeSliceID(NewsCrawler.TIME_SLICE_ID);
            article.setExtractionDate(this.parseDate(res.getDate()));
            article.setKeywords(res.getKeywords() != null ? new HashSet<String>(res.getKeywords()) : new HashSet<String>());
            
            return article;
        }
        catch (IllegalArgumentException iae) {
            
            logger.error("Error crawling html from url: " + url, iae);
        }
        catch (Exception e) {
            
            e.printStackTrace();
            logger.error("Error crawling html from url: " + url, e);
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
            
            return date != null ? format.parse(date) :  new Date();
        }
        catch ( ParseException pe ) {
            
            return new Date();
        }
    }
}
