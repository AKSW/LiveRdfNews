package org.aksw;

import java.io.File;
import java.io.IOException;

import org.aksw.concurrency.RssDirectoryReader;
import org.aksw.config.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.sun.syndication.io.FeedException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class FeedParserTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public FeedParserTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(FeedParserTest.class);
    }

    /**
     * Rigourous Test :-)
     * 
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public void testApp() throws InvalidFileFormatException, IOException {

        NewsCrawler.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/config.ini")));
        RssDirectoryReader reader = new RssDirectoryReader();
     
        try {
            
            reader.queryRssFeeds();
        }
        catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FeedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}