/**
 * 
 */
package org.aksw.simba.rdflivenews.crawler;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import org.aksw.simba.rdflivenews.concurrency.RssDirectoryReader;
import org.apache.log4j.Logger;

import com.sun.syndication.io.FeedException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class UpdateRssFeedsTask extends TimerTask {

    private Logger logger               = Logger.getLogger(UpdateRssFeedsTask.class);
    private RssDirectoryReader reader   = null;
    
    public UpdateRssFeedsTask(BlockingQueue<String> queue) {

        this.reader = new RssDirectoryReader(queue);
    }

    /**
     * 
     */
    public void run() {
        
        try {
            
            logger.info("Starting to update RSS feeds!");
            reader.queryRssFeeds();
            logger.info("Finished updating RSS feeds!");
        }
        catch (IllegalArgumentException e) {
            
            e.printStackTrace();
            logger.error(e);
        }
        catch (IOException e) {

            e.printStackTrace();
            logger.error(e);
        }
        catch (FeedException e) {
            
            e.printStackTrace();
            logger.error(e);
        }
    }
}
