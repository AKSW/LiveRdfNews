/**
 * 
 */
package org.aksw.crawler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.aksw.NewsCrawler;
import org.aksw.concurrency.CrawlQueueManager;
import org.aksw.index.IndexManager;
import org.aksw.index.NewsArticle;
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
    
    /**
     * Returns a new article or null if we could not generate it.
     * 
     * @param url
     * @return
     */
    private NewsArticle crawlArticle(String url) {
        
        try {
            
            JResult res = fetcher.fetchAndExtract(url, NewsCrawler.CONFIG.getIntegerSetting("crawl", "timeout"), true);
            
            NewsArticle article = new NewsArticle();
            article.setArticleUrl(url);
            article.setImageUrl(res.getImageUrl());
            article.setTitle(res.getTitle());
            article.setText(res.getText());
            article.setTimeSliceID(NewsCrawler.TIME_SLICE_ID);
            article.setExtractionDate(res.getDate() != null ? format.parse(res.getDate()) : new Date());
            article.setKeywords(res.getKeywords() != null ? new HashSet<String>(res.getKeywords()) : new HashSet<String>());
            
            return article;
        }
        catch (Exception e) {
            
            e.printStackTrace();
            logger.error("Error crawling html from url: " + url, e);
        }
        
        return null;
    }

    /**
     * 
     */
    @Override public void run() {

        // this thread should also run forever
        while ( true ) {
            
            // if there is an uri on the queue the try to crawl the rss entry
            String uri = CrawlQueueManager.getInstance().removeArticleFromCrawlQueue();
            if ( uri != null ) {
                
                NewsArticle article = crawlArticle(uri);
                
                if ( article != null ) 
                    IndexManager.getInstance().addNewsArticle(article);    
            }
            // wait so that we dont run this method over and over if no rss feeds are avaiable
            try {
                
                sleep(5000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
