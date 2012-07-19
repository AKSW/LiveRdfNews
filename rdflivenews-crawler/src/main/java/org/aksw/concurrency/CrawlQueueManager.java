/**
 * 
 */
package org.aksw.concurrency;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CrawlQueueManager {
    
    private static CrawlQueueManager INSTANCE = null;
    private static CrawlQueue queue = new CrawlQueue();
    
    /**
     * singleton
     */
    private CrawlQueueManager() {
        
    }

    /**
     * 
     * @return
     */
    public static CrawlQueueManager getInstance() {
        
        if ( CrawlQueueManager.INSTANCE == null ) CrawlQueueManager.INSTANCE = new CrawlQueueManager();
        return CrawlQueueManager.INSTANCE;
    }
    
    /**
     * 
     * @param articleUrl
     */
    public void addArticleToCrawlQueue(String articleUrl) {
        
        queue.add(articleUrl);
    }
    
    /**
     * 
     * @return
     */
    public String removeArticleFromCrawlQueue() {
        
        return queue.poll();
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isUriQueued(String uri) {

        return queue.contains(uri);
    }
}
