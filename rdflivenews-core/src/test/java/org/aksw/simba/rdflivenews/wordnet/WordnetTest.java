/**
 * 
 */
package org.aksw.simba.rdflivenews.wordnet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.smu.tspell.wordnet.SynsetType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetTest extends TestCase {
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(WordnetTest.class);
    }
    
    public void testGetSynsets() {
        
        Set<String> bathroom = new HashSet<>(Arrays.asList("lav", "can", "toilet", "lavatory", "john", "bath", "privy", "bathroom"));
        Set<String> wakeAll  = new HashSet<>(Arrays.asList("awaken", "come alive", "inflame", "wake up", "aftermath", "awake", "heat", "Wake", "backwash", "fire up", "stir up", "waken", "rouse", "wake", "arouse", "ignite", "Wake Island", "viewing"));
        Set<String> wakeVerb = new HashSet<>(Arrays.asList("come alive", "awaken", "fire up", "stir up", "waken", "rouse", "inflame", "arouse", "wake", "ignite", "wake up", "awake", "heat"));
        
        assertEquals(bathroom, Wordnet.getInstance().getWordnetExpansion("bathroom"));
        assertEquals(wakeAll, Wordnet.getInstance().getWordnetExpansion("wake"));
        assertEquals(wakeVerb, Wordnet.getInstance().getWordnetExpansion("wake", SynsetType.VERB));
    }
}
