/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.similarity;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.tokenization.Tokenizer;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.NGramTokenizer;

/**
 *
 * @author ngonga
 */
public class Similarity {

    private Tokenizer tokenizer;

    /**
     * 
     */
    public Similarity(Tokenizer tokenizer) {
        
        this.tokenizer = tokenizer;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public double getSimilarity(String x, String y) {
        
        Set<String> yTokens = tokenizer.tokenize(y);
        Set<String> xTokens = tokenizer.tokenize(x);
        
        System.out.println(yTokens);
        System.out.println(xTokens);
        return getSimilarity(xTokens, yTokens);
    }
    
    /**
     * 
     * @param setOne
     * @param setTwo
     * @return
     */
    public double getSimilarity(Set<String> setOne, Set<String> setTwo) {
        
        Set<String> copyOfSetOne = new HashSet<String>(setOne);
        copyOfSetOne.retainAll(setTwo);
        double z = (double) copyOfSetOne.size();
        return z / (setOne.size() + setTwo.size() - z);        
    }
    
    public static void main(String args[])
    {
        System.out.println(new Similarity(new NGramTokenizer(3)).getSimilarity("abcd", "abcde"));
    }
}
