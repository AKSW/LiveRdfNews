/**
 * 
 */
package org.aksw.crawler;

import java.io.IOException;
import java.util.TimerTask;

import org.aksw.concurrency.RssDirectoryReader;
import org.apache.log4j.Logger;

import com.sun.syndication.io.FeedException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class UpdateRssFeedsTask extends TimerTask {

    private Logger logger               = Logger.getLogger(UpdateRssFeedsTask.class);
    private RssDirectoryReader reader   = new RssDirectoryReader();
    
    /**
     * 
     */
    public void run() {
        
        try {
            
            reader.queryRssFeeds();
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
