/**
 * 
 */
package org.aksw;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

import org.aksw.concurrency.CrawlRssFeedEntryThread;
import org.aksw.concurrency.ShutdownThread;
import org.aksw.config.Config;
import org.aksw.crawler.UpdateRssFeedsTask;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NewsCrawler {

    public static int TIME_SLICE_ID = 0;
    public static Config CONFIG = null;
    private static Logger logger = Logger.getLogger(NewsCrawler.class);
    
    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException, InterruptedException {
        
        // load the config, we dont need to configure logging because the log4j config is on the classpath
        NewsCrawler.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/config.ini")));
        
        // with this hook we can guaranty that the lucene index is closed correctly
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        
        // this is the starting point for the slice, this time + the slice length make a time slice
        long startTime = System.currentTimeMillis();

        // start to update the rss entries which are then crawled periodically
        Timer updateFeedTimer = new Timer();
        UpdateRssFeedsTask updateFeeds = new UpdateRssFeedsTask();
        updateFeedTimer.schedule(updateFeeds, 0, NewsCrawler.CONFIG.getLongSetting("crawl", "updateRssInterval")); // TODO make this configurable
        logger.info("Started RSS update task!");

        // this thread get's started and will never and, it opens a new thread pool where one thread crawls one rss entry
        CrawlRssFeedEntryThread crawlRssFeedsThread = new CrawlRssFeedEntryThread();
        crawlRssFeedsThread.run();
        logger.info("Started RSS crawl threads!");
        
        // the crawler is supposed to run forever
        while ( true ) {

            // the time has come to create a new slice
            if ( System.currentTimeMillis() - startTime >= NewsCrawler.CONFIG.getLongSetting("timeSlice", "duration") )
                NewsCrawler.TIME_SLICE_ID++;
            
            // avoid 100% cpu load 
            Thread.sleep(5000);
        }
    }
}
