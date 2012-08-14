/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueueManager {
    
    private static QueueManager INSTANCE = null;
    private static BlockingQueue<String> crawlStack = new LinkedBlockingQueue<String>();
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
    public synchronized void addArticleToCrawlQueue(String articleUrl) {
        
        try {
            
            crawlStack.put(articleUrl);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @return
     */
    public synchronized String removeArticleFromCrawlQueue() {
        
        String uri = "";
        try {
            
            uri = crawlStack.take();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.debug("Removed uri from crawlStack: " + uri);
        return uri;
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isUriQueued(String uri) {
        
        this.logger.debug("Queued uri: " + uri);
        System.out.println("start contains query");
        boolean contains = crawlStack.contains(uri);
        System.out.println("finished contains query: " + contains);
        return contains;
    }
    
    /**
     * 
     * @return
     */
    public int getNumberOfQueuedArticles() {
        
        return crawlStack.size();
    }
}
