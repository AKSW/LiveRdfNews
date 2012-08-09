/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
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
//            Collections.shuffle(this.rssFeeds);
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

        int i = 1;
        // check every rss feed
        for (String feedUrl : rssFeeds) {
            
            System.out.println(i++ + ": " + feedUrl);
            logger.info("Getting article urls from feed: " + feedUrl);

            XmlReader reader = null;
            String link = "";

            try {

                reader = new XmlReader(new URL(feedUrl));
                SyndFeed feed = new SyndFeedInput().build(reader);

                for (Iterator<SyndEntry> syndEntryIterator = feed.getEntries().iterator(); syndEntryIterator.hasNext();) {
                    
                    link = syndEntryIterator.next().getLink();
                        
                    // we only want to add the uri if the uri is not already
                    // in the queue or in the database
                    if (!QueueManager.getInstance().isUriQueued(link) && IndexManager.getInstance().isNewArticle(link)) {

                        QueueManager.getInstance().addArticleToCrawlQueue(link);
                        this.logger.info("Added new article URL: " + link);
                    }
                    else
                        logger.info("Article already known... skipping: " + link);
                }
            }
            catch (NullPointerException npe) {
                
                logger.debug("Error parsing feed: " + feedUrl + " and url: " + link, npe);
            }
            catch (IOException uhe) {

                logger.debug("Error parsing feed: " + feedUrl, uhe);
            }
            catch (IllegalArgumentException iae) {
                
                logger.debug("Error parsing feed: " + feedUrl, iae);
            }
            catch (ParsingFeedException pfe) {

                logger.debug("Error parsing feed: " + feedUrl, pfe);
            }
            finally {

                if (reader != null) reader.close();
            }
        }
        
        System.out.println("Finished reading list of rss feeds... waiting for restart");
    }
}
