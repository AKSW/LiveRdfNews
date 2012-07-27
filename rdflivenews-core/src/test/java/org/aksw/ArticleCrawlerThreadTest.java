/**
 * 
 */
package org.aksw;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.concurrency.QueueManager;
import org.aksw.config.Config;
import org.aksw.crawler.ArticleCrawlerThread;
import org.aksw.index.NewsArticle;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;



/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ArticleCrawlerThreadTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public ArticleCrawlerThreadTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(ArticleCrawlerThreadTest.class);
    }

    /**
     * Rigourous Test :-)
     * 
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public void testNerAndPostagging() throws InvalidFileFormatException, IOException {
        
        NewsCrawler.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/config.ini")));
        ArticleCrawlerThread t = new ArticleCrawlerThread();
        NewsArticle article = t.crawlArticle("http://www.nytimes.com/2012/07/24/world/middleeast/chemical-weapons-wont-be-used-in-rebellion-syria-says.html?_r=1&ref=global-home");
        
        assertTrue(article.getNerTaggedText() != null);
        assertTrue(!article.getNerTaggedText().isEmpty());
        assertTrue(article.getPosTaggedText() != null);
        assertTrue(!article.getPosTaggedText().isEmpty());
    }
}
