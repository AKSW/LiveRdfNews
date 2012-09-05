/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.impl.DefaultPatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.jena.SubclassChecker;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternRefinerTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public PatternRefinerTest(String testName) throws InvalidFileFormatException, IOException{
        super(testName);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(PatternRefinerTest.class);
    }
    
    public void tesstGetDeepestSubclass() {
        
        Set<String> urisOfClasses = new HashSet<>(Arrays.asList("http://dbpedia.org/ontology/Actor", "http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/Agent", "http://dbpedia.org/ontology/Artist"));
        assertEquals("http://dbpedia.org/ontology/Actor", SubclassChecker.getDeepestSubclass(urisOfClasses));
    }
    
    public void testGetFavouriteType() 
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Pattern pattern = new DefaultPattern();
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Fox Mulder", "PER"), new Entity("David Duchovny", "PER"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Dana Scully", "PER"), new Entity("Gillan Anderson", "PER"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Batman", "PER"), new Entity("Christian Bale", "PER"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Iron Man", "PER"), new Entity("Robert Downey Jr.", "PER"), 1));
        
        PatternRefiner refiner = new DefaultPatternRefiner();
        refiner.refinePattern(pattern);
        
        assertEquals("http://dbpedia.org/ontology/FictionalCharacter", pattern.getFavouriteTypeFirstEntity());
        assertEquals("http://dbpedia.org/ontology/Person", pattern.getFavouriteTypeSecondEntity());
    }
}
