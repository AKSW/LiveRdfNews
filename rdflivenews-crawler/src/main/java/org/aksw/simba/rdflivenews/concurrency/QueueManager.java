/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.util.Collections;
import java.util.Stack;

import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueueManager {
    
    private static QueueManager INSTANCE = null;
    private static Stack<String> crawlStack = new Stack<String>();
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
        
        crawlStack.add(articleUrl);
        Collections.shuffle(crawlStack);
    }
    
    /**
     * 
     * @return
     */
    public synchronized String removeArticleFromCrawlQueue() {
        
        String uri = !crawlStack.isEmpty() ? crawlStack.pop() : null;
        logger.debug("Removed uri from crawlStack: " + uri);
        return uri;
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isUriQueued(String uri) {
        
        this.logger.debug("Queueing uri: " + uri);
        return crawlStack.contains(uri);
    }
}
