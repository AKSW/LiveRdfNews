/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aksw.simba.rdflivenews.NewsCrawler;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.mvn.MavenHelper;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.ParsingFeedException;
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
            Collections.shuffle(this.rssFeeds);
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

                // get all the links and shuffle them this way we
                // dont crawl from the same domains so often and can 
                // decrease the amount of time each crawler waits after 
                // each link
                List<String> links = new ArrayList<String>();
                for (Iterator<SyndEntry> syndEntryIterator = feed.getEntries().iterator(); syndEntryIterator.hasNext();)
                    links.add(syndEntryIterator.next().getLink());
                Collections.shuffle(links);
                
                for ( String link : links ) {
                    
                    // we only want to add the uri if the uri is not already
                    // in the queue or in the database
                    if (!QueueManager.getInstance().isUriQueued(link) && IndexManager.getInstance().isNewArticle(link)) {

                        QueueManager.getInstance().addArticleToCrawlQueue(link);
                        logger.info("Added new entry to queue: " + link);
                    }
                    else
                        logger.info("Article already known... skipping: " + link);
                }
            }
            catch (ParsingFeedException pfe) {

                logger.error("Error parsing feed: " + feedUrl, pfe);
            }
            finally {

                if (reader != null) reader.close();
            }
        }
    }
}
