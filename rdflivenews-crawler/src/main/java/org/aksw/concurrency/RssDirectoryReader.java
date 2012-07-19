/**
 * 
 */
package org.aksw.concurrency;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.aksw.NewsCrawler;
import org.aksw.index.IndexManager;
import org.aksw.mvn.MavenHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class RssDirectoryReader {

    private List<String> rssFeeds = null;
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Reads the list of urls from the rss-list.txt file
     */
    public RssDirectoryReader() {

        try {

            this.rssFeeds = FileUtils.readLines(MavenHelper.loadFile("/rss-list.txt"), "UTF-8");
        }
        catch (IOException e) {

            throw new RuntimeException(String.format("Could not load the rss feed list from %s.", NewsCrawler.CONFIG.getStringSetting("rss", "feedlist")), e);
        }
    }

    /**
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FeedException
     */
    public void queryRssFeeds() throws IOException, IllegalArgumentException, FeedException {

        // check every rss feed
        for (String feedUrl : rssFeeds) {

            XmlReader reader = null;

            try {

                reader = new XmlReader(new URL(feedUrl));
                SyndFeed feed = new SyndFeedInput().build(reader);

                for (Iterator i = feed.getEntries().iterator(); i.hasNext();) {

                    String uri = ((SyndEntry) i.next()).getUri();
                    
                    // we only want to add the uri if the uri not already in the queue or in the database
                    if ( !CrawlQueueManager.getInstance().isUriQueued(uri) && IndexManager.getInstance().isNewArticle(uri) ) {
                        
                        CrawlQueueManager.getInstance().addArticleToCrawlQueue(uri);
                        logger.info("Added new entry to queue: " + uri);
                    }
                }
            }
            finally {
                
                if (reader != null) reader.close();
            }
        }
    }
}