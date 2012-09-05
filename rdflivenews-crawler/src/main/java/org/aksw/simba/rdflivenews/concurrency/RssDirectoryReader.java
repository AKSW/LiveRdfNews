/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.aksw.simba.rdflivenews.NewsCrawler;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.github.gerbsen.maven.MavenUtil;
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

    private List<String> rssFeeds           = null;
    private Logger logger                   = Logger.getLogger(getClass());
    private BlockingQueue<String> queue     = null;

    /**
     * Reads the list of urls from the rss-list.txt file
     * @param queue 
     */
    public RssDirectoryReader(BlockingQueue<String> queue) {

        try {

            this.rssFeeds = FileUtils.readLines(MavenUtil.loadFile("/rss-list.txt"), "UTF-8");
            Collections.shuffle(this.rssFeeds);
            this.queue = queue;
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
            
            logger.info("Getting article urls from feed: " + feedUrl);

            XmlReader reader = null;
            String link = "";

            try {

                reader = new XmlReader(new URL(feedUrl));
                SyndFeed feed = new SyndFeedInput().build(reader);

                for (Iterator<SyndEntry> syndEntryIterator = feed.getEntries().iterator(); syndEntryIterator.hasNext();) {
                    
                    SyndEntry entry = syndEntryIterator.next();
                    
                    link = entry.getUri();
                    if ( !link.startsWith("http://") ) link = entry.getLink();
                    if ( link.startsWith("http://") ) {

                        // we only want to add the uri if the uri is not already
                        // in the queue or in the database
                        if (!this.queue.contains(link) && IndexManager.getInstance().isNewArticle(link)) {

                            this.queue.put(link);
                            this.logger.info("Added new article URL: " + link);
                        }
                        else {
                            
                            logger.info("Article already known... skipping: " + link);
                        }
                    }
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
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {

                if (reader != null) reader.close();
            }
        }
        
        System.out.println("Finished reading list of rss feeds... waiting for restart");
    }
}
