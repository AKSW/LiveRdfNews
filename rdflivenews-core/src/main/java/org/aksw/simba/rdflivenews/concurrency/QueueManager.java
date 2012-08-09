/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueueManager {
    
    private static QueueManager INSTANCE = null;
    private static Queue<String> crawlQueue = new ConcurrentLinkedQueue<String>();
    private Logger logger = Logger.getLogger(getClass());
    
    /**
     * singleton
     */
    private QueueManager() {}

    /**
     * 
     * @return
     */
    public static QueueManager getInstance() {
        
        if ( QueueManager.INSTANCE == null ) QueueManager.INSTANCE = new QueueManager();
        return QueueManager.INSTANCE;
    }
    
    /**
     * 
     * @param articleUrl
     */
    public void addArticleToCrawlQueue(String articleUrl) {
        
        crawlQueue.add(articleUrl);
    }
    
    /**
     * 
     * @return
     */
    public synchronized String removeArticleFromCrawlQueue() {
        
        String uri = crawlQueue.poll();
        logger.debug("Removed uri from crawlQueue: " + uri);
        return uri;
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isUriQueued(String uri) {
        
        this.logger.debug("Queueing uri: " + uri);
        return crawlQueue.contains(uri);
    }
}
