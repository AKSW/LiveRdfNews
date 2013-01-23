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
import org.aksw.simba.rdflivenews.pattern.refinement.jena.SubclassChecker;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;
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
    
    public void tessstGetDeepestSubclass() {
        
        Set<String> urisOfClasses = new HashSet<>(Arrays.asList("http://dbpedia.org/ontology/Actor", "http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/Agent", "http://dbpedia.org/ontology/Artist"));
        assertEquals("http://dbpedia.org/ontology/Actor", new DefaultTypeDeterminer().getTypeClass(urisOfClasses, DETERMINER_TYPE.SUB_CLASS));
        assertEquals("http://dbpedia.org/ontology/Person", new DefaultTypeDeterminer().getTypeClass(urisOfClasses, DETERMINER_TYPE.SUPER_CLASS));
        
        urisOfClasses = new HashSet<String>(Arrays.asList("http://dbpedia.org/ontology/Person", "http://dbpedia.org/ontology/MusicalArtist", "http://dbpedia.org/ontology/Artist"));
        assertEquals("http://dbpedia.org/ontology/MusicalArtist", new DefaultTypeDeterminer().getTypeClass(urisOfClasses, DETERMINER_TYPE.SUB_CLASS));
        assertEquals("http://dbpedia.org/ontology/Person", new DefaultTypeDeterminer().getTypeClass(urisOfClasses, DETERMINER_TYPE.SUPER_CLASS));
    }
    
    public void testGetFavouriteType() 
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        PatternRefiner refiner = null;//new DefaultPatternRefiner();
        
        // test the mostly used common subclass
        Pattern pattern1 = new DefaultPattern();
        pattern1.addLearnedFromEntities(new EntityPair(new Entity("Fox Mulder", "PER"), new Entity("David Duchovny", "PER"), 1));
        pattern1.addLearnedFromEntities(new EntityPair(new Entity("Dana Scully", "PER"), new Entity("Gillan Anderson", "PER"), 1));
        pattern1.addLearnedFromEntities(new EntityPair(new Entity("Batman", "PER"), new Entity("Christian Bale", "PER"), 1));
        pattern1.addLearnedFromEntities(new EntityPair(new Entity("Iron Man", "PER"), new Entity("Robert Downey Jr.", "PER"), 1));
        
        RdfLiveNews.CONFIG.setStringSetting("refinement", "typing", "SUB_CLASS");
        refiner.refinePattern(pattern1);
        assertEquals("http://dbpedia.org/ontology/FictionalCharacter", pattern1.getFavouriteTypeFirstEntity());
        assertEquals("http://dbpedia.org/ontology/Person", pattern1.getFavouriteTypeSecondEntity());
        
        // test the mostly used common subclass
        Pattern pattern2 = new DefaultPattern();
        pattern2.addLearnedFromEntities(new EntityPair(new Entity("Fox Mulder", "PER"), new Entity("David Duchovny", "PER"), 1));
        pattern2.addLearnedFromEntities(new EntityPair(new Entity("Dana Scully", "PER"), new Entity("Gillan Anderson", "PER"), 1));
        pattern2.addLearnedFromEntities(new EntityPair(new Entity("Batman", "PER"), new Entity("Christian Bale", "PER"), 1));
        pattern2.addLearnedFromEntities(new EntityPair(new Entity("Iron Man", "PER"), new Entity("Robert Downey Jr.", "PER"), 1));
        
        RdfLiveNews.CONFIG.setStringSetting("refinement", "typing", "SUPER_CLASS");
        refiner.refinePattern(pattern2);
        assertEquals("http://dbpedia.org/ontology/Person", pattern2.getFavouriteTypeFirstEntity());
        assertEquals("http://dbpedia.org/ontology/Person", pattern2.getFavouriteTypeSecondEntity());
        
        // test complex example
        RdfLiveNews.CONFIG.setStringSetting("refinement", "typing", "SUPER_CLASS");
        Pattern p1 = this.generatePattern1();
        refiner.refinePattern(p1);
        assertEquals("http://dbpedia.org/ontology/Person", p1.getFavouriteTypeFirstEntity());
        assertEquals("http://dbpedia.org/ontology/Place", p1.getFavouriteTypeSecondEntity());
        
        RdfLiveNews.CONFIG.setStringSetting("refinement", "typing", "SUB_CLASS");
        Pattern p2 = this.generatePattern1();
        refiner.refinePattern(p2);
        assertEquals("http://dbpedia.org/ontology/OfficeHolder", p2.getFavouriteTypeFirstEntity());
        assertEquals("http://dbpedia.org/ontology/City", p2.getFavouriteTypeSecondEntity());
        
//        Pattern:  died in 
//        Tagged-Pattern: arg1 died_VBD in_IN arg2
//        Occurrence: 20
//            1: Pearl City (http://dbpedia.org/ontology/Book) - Aiea (http://dbpedia.org/ontology/Settlement) / sentenceIDs: 26998 / occurrence: 1
//            2: Honolulu (http://dbpedia.org/ontology/Settlement) - Honolulu (http://dbpedia.org/ontology/Settlement) / sentenceIDs: 5225496, 26972, 5225576 / occurrence: 2
//            3: Cde Julia Tukai Zvobgo (http://dbpedia.org/ontology/Politician) - Harare (http://dbpedia.org/ontology/City) / sentenceIDs: 161114 / occurrence: 1
//            4: Smith (http://dbpedia.org/ontology/Person) - May () / sentenceIDs: 254885 / occurrence: 1
//            5: Ed Bradley (http://dbpedia.org/ontology/Journalist) - November () / sentenceIDs: 6659 / occurrence: 1
//            6: Liddell (http://dbpedia.org/ontology/MemberOfParliament) - February () / sentenceIDs: 76766 / occurrence: 1
//            7: Jobs () - October () / sentenceIDs: 2636812 / occurrence: 1
//            8: Ewa Beach (http://dbpedia.org/ontology/MusicalArtist) - Ewa Beach (http://dbpedia.org/ontology/MusicalArtist) / sentenceIDs: 5225565 / occurrence: 1
//            9: Mr. Boisjoly (http://dbpedia.org/ontology/Scientist) - January () / sentenceIDs: 438198 / occurrence: 1
//            10: Rescuer Nick Hall () - June (http://dbpedia.org/ontology/Album) / sentenceIDs: 1311788 / occurrence: 1
//            11: Ewa Beach (http://dbpedia.org/ontology/MusicalArtist) - Aiea (http://dbpedia.org/ontology/Settlement) / sentenceIDs: 27016 / occurrence: 1
//            12: Aiea (http://dbpedia.org/ontology/Settlement) - Aiea (http://dbpedia.org/ontology/Settlement) / sentenceIDs: 5225519 / occurrence: 1
//            13: Apple Inc. (http://dbpedia.org/ontology/Company) - October () / sentenceIDs: 1327239 / occurrence: 1
//            14: Martin Fleischmann (http://dbpedia.org/ontology/Scientist) - England (http://dbpedia.org/ontology/Magazine) / sentenceIDs: 2740155 / occurrence: 1
//            15: Mr. Hegyes (http://dbpedia.org/ontology/Person) - January () / sentenceIDs: 2987042 / occurrence: 1
//            16: Wailuku (http://dbpedia.org/ontology/ArchitecturalStructure) - Maui Memorial Medical Center (http://dbpedia.org/ontology/ArchitecturalStructure) / sentenceIDs: 27006 / occurrence: 1
//            17: Ethelbert Chingonzo () - June (http://dbpedia.org/ontology/Album) / sentenceIDs: 385175 / occurrence: 1
//            18: Roach () - New York (http://dbpedia.org/ontology/Album) / sentenceIDs: 1073204 / occurrence: 1
//            19: Pearl City (http://dbpedia.org/ontology/Book) - Waipahu (http://dbpedia.org/ontology/Settlement) / sentenceIDs: 5225542 / occurrence: 1
    }

    private Pattern generatePattern1() {

        Pattern pattern = new DefaultPattern();
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Royal Horticultural Society", "PER"), new Entity("Dunbar", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Roosevelt", "PER"), new Entity("Maine", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Mr. Fujimoto", "PER"), new Entity("Pyongyang", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Hilton Head Island", "PER"), new Entity("Madhouse Vintage", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("National Public Safety Commission Chairman Jin Matsubara", "PER"), new Entity("Yasukuni", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Texarkana", "PER"), new Entity("James", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Prince Harry", "PER"), new Entity("Jamaica", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Mills", "PER"), new Entity("Washington", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Tyrann Mathieu", "PER"), new Entity("McNeese State", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("President Barack Obama", "PER"), new Entity("Colorado", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("State Hillary Clinton", "PER"), new Entity("Istanbul", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Nash", "PER"), new Entity("Columbus General Manager Scott Howson", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Mathieu", "PER"), new Entity("McNeese State", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Bonnie Wright", "PER"), new Entity("Kedougou", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Kelly Ayotte", "PER"), new Entity("Nellis Air Force Base", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("General Miller", "PER"), new Entity("Abu Ghraib ", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("President Lee", "PER"), new Entity("Takeshima", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Secretary-General", "PER"), new Entity("Botswana", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Melanie Barrow", "PER"), new Entity("Seattle", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Clinton", "PER"), new Entity("Nigeria", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Blanco", "PER"), new Entity("Tuesday", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("LSU", "PER"), new Entity("Husky Stadium", "LOC"), 1));
        pattern.addLearnedFromEntities(new EntityPair(new Entity("Darren Riley", "PER"), new Entity("Darent Valley Hospital", "LOC"), 1));
        return pattern; 
    }
}
