/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.simba.rdflivenews.NewsCrawler;
import org.aksw.simba.rdflivenews.crawler.ArticleCrawlerThread;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CrawlRssFeedEntryThread implements Runnable {

    private BlockingQueue<String> queue = null;
    
    public CrawlRssFeedEntryThread(BlockingQueue<String> queue) {

        this.queue = queue;
    }

    /**
     * 
     */
    public void run() {

        int numberOfThreads = NewsCrawler.CONFIG.getIntegerSetting("crawl", "numberOfThreads");
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        for ( int i = 0 ; i < numberOfThreads ; i++ )
            executor.execute(new ArticleCrawlerThread(queue));            
    }
}
