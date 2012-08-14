/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.refinement.LuceneRefinementManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneRefinementManagerTest extends TestCase {

    private LuceneRefinementManager luceneRefinementManager;
    
    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public LuceneRefinementManagerTest(String testName) throws InvalidFileFormatException, IOException {
        super(testName);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        this.luceneRefinementManager = new LuceneRefinementManager();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(LuceneRefinementManagerTest.class);
    }

    /**
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public void testUriRetrieval() {
       
        assertEquals("http://dbpedia.org/resource/Mahidpur", luceneRefinementManager.getPossibleUri("Mahidpur"));
        assertEquals("http://dbpedia.org/resource/Microsoft", luceneRefinementManager.getPossibleUri("Microsoft"));
        assertEquals("http://dbpedia.org/resource/Fox_Mulder", luceneRefinementManager.getPossibleUri("Fox Mulder"));
        assertEquals("http://dbpedia.org/resource/Apple_Inc.", luceneRefinementManager.getPossibleUri("Apple Inc."));
    }
    
    /**
     * 
     */
    public void testTypesRetrieval() {
        
        Set<String> appleTypes = new HashSet<String>(Arrays.asList("http://dbpedia.org/ontology/Organisation", "http://dbpedia.org/ontology/Company"));
        assertEquals(appleTypes, this.luceneRefinementManager.getTypesOfResource("http://dbpedia.org/resource/Apple_Inc."));
        
        Set<String> foxMulderTypes = new HashSet<String>(Arrays.asList("http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/FictionalCharacter"));
        assertEquals(foxMulderTypes, this.luceneRefinementManager.getTypesOfResource("http://dbpedia.org/resource/Fox_Mulder"));
    }
}
