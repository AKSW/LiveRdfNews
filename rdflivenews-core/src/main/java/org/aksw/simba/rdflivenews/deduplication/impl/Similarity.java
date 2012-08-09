/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class Similarity {

    Tokenizer tokenizer;
    int q = 3;

    public Similarity(int q) {
        tokenizer = new NGramTokenizer();
    }

    public Similarity() {
        tokenizer = new NGramTokenizer();
    }

    public double getSimilarity(String x, String y) {
        Set<String> yTokens = tokenizer.tokenize(y, q);
        Set<String> xTokens = tokenizer.tokenize(x, q);
        return getSimilarity(xTokens, yTokens);
    }
    
    public double getSimilarity(Set<String> X, Set<String> Y)
    {
        double x = (double)X.size();
        double y = (double)Y.size();  
        //create a kopy of X
        Set<String> K = new HashSet<String>();
        for(String s: X) K.add(s);
        K.retainAll(Y);
        double z = (double)K.size();
        return z/(x+y-z);        
    }
    
    public static void main(String args[])
    {
        System.out.println(new Similarity().getSimilarity("abcd", "abcde"));
    }
}
