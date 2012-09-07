/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.filter.impl.DefaultPatternFilter;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefinerTest;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternFilterTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public PatternFilterTest(String testName) throws InvalidFileFormatException, IOException{
        super(testName);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(PatternFilterTest.class);
    }
    
    public void testPatternFiltering() {
        
        List<Pattern> goodPatterns = new ArrayList<>();
        goodPatterns.add(new DefaultPattern(", told the", ",_, told_VBD the_DT"));
        goodPatterns.add(new DefaultPattern("representative", "representative_NN"));
        
        List<Pattern> badPatterns = new ArrayList<>();
        badPatterns.add(new DefaultPattern("-LRB- ``"));
        badPatterns.add(new DefaultPattern("representative", "JJ"));
        badPatterns.add(new DefaultPattern("'s former", "'s_POS former_ADJ"));
        
        List<Pattern> patterns = new ArrayList<>();
        patterns.addAll(goodPatterns);
        patterns.addAll(badPatterns);
        
        PatternFilter filter = new DefaultPatternFilter();
        patterns = filter.filter(patterns);
        
        assertEquals(goodPatterns.size(), patterns.size());
    }
}
