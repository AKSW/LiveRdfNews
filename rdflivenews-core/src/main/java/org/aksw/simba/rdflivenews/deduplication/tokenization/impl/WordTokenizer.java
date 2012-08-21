/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.tokenization.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.tokenization.Tokenizer;

/**
 *
 * @author ngonga
 */
public class WordTokenizer implements Tokenizer{

    /**
     * 
     * @param s String to tokenize
     * @param q Has no effect
     * @return Set of tokens (words) for s
     */
    public Set<String> tokenize(String s) {
        
        if ( s == null || s.isEmpty() ) return new HashSet<String>();
        return new HashSet<String>(Arrays.asList(s.toLowerCase().split(" ")));
    }
}
