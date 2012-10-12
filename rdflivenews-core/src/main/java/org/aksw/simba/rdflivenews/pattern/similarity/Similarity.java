/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity;

import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Similarity {

    private Pattern pattern1;
    private Pattern pattern2;
    private double similarity;

    public Similarity(Pattern pattern1, Pattern pattern2, double similarity) {
        
        this.pattern1 = pattern1;
        this.pattern2 = pattern2;
        this.similarity = similarity;
    }

    
    /**
     * @return the pattern1
     */
    public Pattern getPattern1() {
    
        return pattern1;
    }

    
    /**
     * @return the pattern2
     */
    public Pattern getPattern2() {
    
        return pattern2;
    }

    
    /**
     * @return the similarity
     */
    public double getSimilarity() {
    
        return similarity;
    }
}
