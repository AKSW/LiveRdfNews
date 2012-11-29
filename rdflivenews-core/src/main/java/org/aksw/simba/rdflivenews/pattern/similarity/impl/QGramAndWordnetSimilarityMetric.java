/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class QGramAndWordnetSimilarityMetric implements SimilarityMetric {

    private WordnetSimilarity similarity = WordnetSimilarity.LIN;
    
    private double qgramParamter = 0D;
    private double wordnetParamter = 0D;
    
    SimilarityMetric qgram = new QGramSimilarityMetric();
    SimilarityMetric wordnet = new WordnetSimilarityMetric();

    /*
     * (non-Javadoc)
     * 
     * @see org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric#
     * calculateSimilartiy(org.aksw.simba.rdflivenews.pattern.Pattern,
     * org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public double calculateSimilarity(Pattern p1, Pattern p2) {
        
        ((WordnetSimilarityMetric) wordnet).setWordnetSimilarity(similarity);
        return ((qgramParamter * qgram.calculateSimilarity(p1, p2)) + (wordnetParamter * wordnet.calculateSimilarity(p1, p2)) / 2D);
    }

    /**
     * Default is LIN metric
     * 
     * @param sim
     */
    public void setWordnetSimilarity(WordnetSimilarity sim) {

        this.similarity = sim;
    }
    
    /**
     * @param qgramParamter the qgramParamter to set
     */
    public void setQgramParamter(double qgramParamter) {
    
        this.qgramParamter = qgramParamter;
    }

    /**
     * @param wordnetParamter the wordnetParamter to set
     */
    public void setWordnetParamter(double wordnetParamter) {
    
        this.wordnetParamter = wordnetParamter;
    }
}
