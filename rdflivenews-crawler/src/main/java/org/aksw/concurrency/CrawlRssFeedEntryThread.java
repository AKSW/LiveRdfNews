/**
 * 
 */
package org.aksw.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.NewsCrawler;
import org.aksw.crawler.ArticleCrawlerThread;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CrawlRssFeedEntryThread implements Runnable {

    /**
     * 
     */
    public void run() {

        int numberOfThreads = NewsCrawler.CONFIG.getIntegerSetting("crawl", "numberOfThreads");
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        for ( int i = 0 ; i < numberOfThreads ; i++ )
            executor.execute(new ArticleCrawlerThread());            
    }
}
