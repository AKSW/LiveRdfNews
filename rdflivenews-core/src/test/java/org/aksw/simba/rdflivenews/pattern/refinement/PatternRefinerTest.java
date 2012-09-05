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
    
    public void testGetDeepestSubclass() {
        
        Set<String> urisOfClasses = new HashSet<>(Arrays.asList("http://dbpedia.org/ontology/Actor", "http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/Agent", "http://dbpedia.org/ontology/Artist"));
        assertEquals("http://dbpedia.org/ontology/Actor", SubclassChecker.getDeepestSubclass(urisOfClasses));
    }
    
    public void testGetFavouriteType() 
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        PatternRefiner refiner = new DefaultPatternRefiner();
        
        Method method = DefaultPatternRefiner.class.getDeclaredMethod("generateFavouriteType", Map.class, Set.class);
        method.setAccessible(true);
        
        Map<String,Integer> oldTypes = new HashMap<String,Integer>();
        Set<String> newTypes = new HashSet<String>();
        // nothing in old or new so nothing can be done
        assertEquals("", method.invoke(refiner, oldTypes, newTypes));
        
        // add one there and one there, 2x Person & 1x FictionalCharacter > Person
        oldTypes.put("http://dbpedia.org/ontology/Person", 2);
        newTypes.add("http://dbpedia.org/ontology/FictionalCharacter");
        assertEquals("http://dbpedia.org/ontology/Person", method.invoke(refiner, oldTypes, newTypes));
        
        // 1x Person 2x FictionalCharacter
        oldTypes.put("http://dbpedia.org/ontology/Person", 1);
        newTypes.add("http://dbpedia.org/ontology/FictionalCharacter");
        assertEquals("http://dbpedia.org/ontology/FictionalCharacter", method.invoke(refiner, oldTypes, newTypes));
    }
}
