package org.aksw;

import java.util.Collection;
import java.util.Date;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class SnacktoryTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public SnacktoryTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(SnacktoryTest.class);
    }

    /**
     * Rigourous Test :-)
     * @throws Exception 
     */
    public void testApp() throws Exception {

        HtmlFetcher fetcher = new HtmlFetcher();
        // set cache. e.g. take the map implementation from google collections:
        // fetcher.setCache(new MapMaker().concurrencyLevel(20).maximumSize(count).expireAfterWrite(minutes, TimeUnit.MINUTES).makeMap();
        JResult res = fetcher.fetchAndExtract("http://www.nytimes.com/2012/07/20/world/europe/explosion-on-bulgaria-tour-bus-kills-at-least-five-israelis.html?pagewanted=all", 1000, true);
        String text = res.getText(); 
        String title = res.getTitle(); 
        String date = res.getDate();
        String imageUrl = res.getImageUrl();
        Collection<String> keywords = res.getKeywords();
        
        System.out.println(text);
        System.out.println(title);
        System.out.println(imageUrl);
        System.out.println(date);
        System.out.println(keywords);
    }
}
