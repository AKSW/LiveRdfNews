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
    
    public Similarity(Pattern pattern1, Pattern pattern2) {
        
        this.pattern1 = pattern1;
        this.pattern2 = pattern2;
        this.similarity = 0D;
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
    
    /**
     * 
     * @param similarity
     */
    public void setSimilarity(double similarity) {

        this.similarity = similarity;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern1 == null) ? 0 : pattern1.hashCode());
        result = prime * result + ((pattern2 == null) ? 0 : pattern2.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Similarity other = (Similarity) obj;
        if (pattern1 == null) {
            if (other.pattern1 != null)
                return false;
        }
        else
            if (!pattern1.equals(other.pattern1))
                return false;
        if (pattern2 == null) {
            if (other.pattern2 != null)
                return false;
        }
        else
            if (!pattern2.equals(other.pattern2))
                return false;
        return true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return this.pattern1.getNaturalLanguageRepresentation() + "\t" + this.pattern2.getNaturalLanguageRepresentation() + "\t" + this.similarity;
    }
}
