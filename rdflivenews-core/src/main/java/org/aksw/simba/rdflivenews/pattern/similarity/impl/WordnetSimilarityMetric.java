/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.apache.log4j.chainsaw.Main;

import com.github.gerbsen.similarity.wordnet.SimilarityAssessor;
import com.github.gerbsen.similarity.wordnet.WordNotFoundException;



/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetSimilarityMetric implements SimilarityMetric {

    private SimilarityAssessor similarityAssessor = new SimilarityAssessor();
    
    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric#calculateSimilartiy(org.aksw.simba.rdflivenews.pattern.Pattern, org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public double calculateSimilarity(Pattern pattern1, Pattern pattern2) {

        double maximum = 0;
        int comparison = 0;
        
        String[] partsOfPattern2 = pattern2.getNaturalLanguageRepresentation().split(" ");
        
        for ( String partOfPattern1 : pattern1.getNaturalLanguageRepresentation().split(" ") ) {
            
            if ( Constants.STOP_WORDS.contains(partOfPattern1.toLowerCase()) ) continue;
            
            for ( int i = 0 ; i < partsOfPattern2.length ; i++) {
                
                if ( Constants.STOP_WORDS.contains(partsOfPattern2[i].toLowerCase()) ) continue;
                
                try {
                    
                    maximum += this.similarityAssessor.getSimilarity(partOfPattern1, partsOfPattern2[i]);
                    comparison++;
                }
                catch (WordNotFoundException e) { /* we dont care about not found words */ }
            }
        }
        
        return maximum == 0D ? 0D : maximum / comparison;
    }
    
    public static void main(String[] args) {

        Pattern pattern1 = new DefaultPattern(", the capital of");
        Pattern pattern2 = new DefaultPattern(", the former capital of");
        
        WordnetSimilarityMetric m = new WordnetSimilarityMetric();
        System.out.println(m.calculateSimilarity(pattern1, pattern2));
    }
}
