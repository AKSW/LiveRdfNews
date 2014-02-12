/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern;

import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SpeedTest {

	public static void main(String[] args) {
		
		String test = "As Iranians responded to the victory of the cleric Hassan Rowhani in the country’s presidential race over the weekend by erupting into street parties not seen in many years, it almost seemed as if some sort of reformist revolution could be under way.";
		
		StanfordNLPPartOfSpeechTagger pos = new StanfordNLPPartOfSpeechTagger();
		StanfordNLPNamedEntityRecognition ner= new StanfordNLPNamedEntityRecognition();
		
		long start = System.currentTimeMillis();
		for ( int i = 0 ; i < 1000; i++ ) {
			
			pos.getAnnotatedSentence(test);
		}
		System.out.println("POS: " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		for ( int i = 0 ; i < 1000; i++ ) {
			
			ner.getAnnotatedSentence(test);
		}
		System.out.println("POS: " + (System.currentTimeMillis() - start));
	}
}
