/**
 * 
 */
package org.aksw.simba.rdflivenews.concurrency;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.RdfLiveNewsCrawler;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.rss.RssFeed;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.lucene.LuceneManager;
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
    private BlockingQueue<RssFeed> queue     = null;
    private IndexSearcher searcher			= null;

    /**
     * Reads the list of urls from the rss-list.txt file
     * @param queue 
     */
    public RssDirectoryReader(BlockingQueue<RssFeed> queue) {

        try {

            this.rssFeeds = FileUtils.readLines(MavenUtil.loadFile("/rss-list.txt"), "UTF-8");
//            Collections.shuffle(this.rssFeeds);
            this.queue = queue;
            this.searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        }
        catch (IOException e) {

            throw new RuntimeException(String.format("Could not load the rss feed list from %s.", RdfLiveNewsCrawler.CONFIG.getStringSetting("rss", "feedlist")), e);
        }
    }
    
	public static void main(String[] args) throws InvalidFileFormatException, IOException {

		// load the config, we dont need to configure logging because the log4j config is on the classpath
        RdfLiveNewsCrawler.CONFIG = new Config(new Ini(RdfLiveNewsCrawler.class.getClassLoader().getResourceAsStream("newscrawler-config.ini")));
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        IndexManager.getInstance();
		RssDirectoryReader reader = new RssDirectoryReader(new LinkedBlockingQueue<RssFeed>());
		
		for (String feedUrl : reader.rssFeeds) {

			try {
				
				int size = new SyndFeedInput().build(new XmlReader(new URL(feedUrl))).getEntries().size();
				if ( size > 0 ) System.out.println(feedUrl);
				
			} catch (IllegalArgumentException | FeedException | IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
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
            RssFeed rssFeed = new RssFeed();

            try {

                reader = new XmlReader(new URL(feedUrl));
                SyndFeed feed = new SyndFeedInput().build(reader);

                for (Iterator<SyndEntry> syndEntryIterator = feed.getEntries().iterator(); syndEntryIterator.hasNext();) {
                    
                    SyndEntry entry = syndEntryIterator.next();
                    
                    rssFeed.link = entry.getUri();
                    rssFeed.publishedDate = entry.getPublishedDate();
                    
                    if ( !rssFeed.link.startsWith("http://") ) rssFeed.link = entry.getLink();
                    if ( rssFeed.link.startsWith("http://") ) {

                        // we only want to add the uri if the uri is not already
                        // in the queue or in the database
                        if (!this.queue.contains(rssFeed.link) && IndexManager.getInstance().isNewArticle(searcher, rssFeed.link)) {

                            this.queue.put(rssFeed);
                            this.logger.info("Added new article URL: " + rssFeed.link);
                        }
                        else {
                            
                            logger.info("Article already known... skipping: " + rssFeed.link);
                        }
                    }
                }
            }
            catch (NullPointerException npe) {
                
                logger.debug("Error parsing feed: " + feedUrl + " and url: " + rssFeed.link, npe);
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
