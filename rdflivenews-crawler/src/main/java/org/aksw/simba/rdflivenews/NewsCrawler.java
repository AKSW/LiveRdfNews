/**
 * 
 */
package org.aksw.simba.rdflivenews;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aksw.simba.rdflivenews.concurrency.CrawlRssFeedEntryThread;
import org.aksw.simba.rdflivenews.concurrency.ShutdownThread;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.crawler.UpdateRssFeedsTask;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.statistics.StatisticsTask;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NewsCrawler {

    public static Integer TIME_SLICE_ID = -1;
    public static Config CONFIG = null;
    private static Logger logger = Logger.getLogger(NewsCrawler.class);
    public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    
    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException, InterruptedException {

        // load the config, we dont need to configure logging because the log4j config is on the classpath
        NewsCrawler.CONFIG = new Config(new Ini(NewsCrawler.class.getResourceAsStream("/newscrawler-config.ini")));
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getResourceAsStream("/rdflivenews-config.ini")));
        
        // with this hook we can guaranty that the lucene index is closed correctly
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        
        // if we stop the crawler and want to resume it we need to start with a higher id
        TIME_SLICE_ID = IndexManager.getInstance().getHighestTimeSliceId() + 1;
        
        // this is the starting point for the slice, this time + the slice length make a time slice
        long startTime = System.currentTimeMillis();

        // start to update the rss entries which are then crawled periodically
        Timer updateFeedTimer = new Timer();
        UpdateRssFeedsTask updateFeeds = new UpdateRssFeedsTask(queue);
        updateFeedTimer.schedule(updateFeeds, 0, NewsCrawler.CONFIG.getLongSetting("crawl", "updateRssInterval"));
        logger.info("Started RSS update task!");
        
        // start the statistics module
        Timer statisticsTimer = new Timer();
        StatisticsTask statTask = new StatisticsTask();
        statisticsTimer.schedule(statTask, 0, NewsCrawler.CONFIG.getLongSetting("statistics", "updateStatisticsInterval"));

        // this thread get's started and will never and, it opens a new thread pool where one thread crawls one rss entry
        CrawlRssFeedEntryThread crawlRssFeedsThread = new CrawlRssFeedEntryThread(queue);
        crawlRssFeedsThread.run();
        logger.info("Started RSS crawl threads!");
        
        // the crawler is supposed to run forever
        while ( true ) {

            // the time has come to create a new slice
            if ( System.currentTimeMillis() - startTime >= NewsCrawler.CONFIG.getLongSetting("timeSlice", "duration") ) {
                
                // increase the timeslice and reset the start time
                NewsCrawler.TIME_SLICE_ID++;
                startTime = System.currentTimeMillis();
            }
            
            // avoid 100% cpu load 
            Thread.sleep(5000);
        }
    }
}
