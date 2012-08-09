/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.tokenization.impl;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.tokenization.Tokenizer;

/**
 *
 * @author ngonga
 */
public class NGramTokenizer implements Tokenizer {

    public Set<String> tokenize(String s, int q) {
        
        if (s == null) s = "";
        
        //remove double blanks
        while (s.contains("  ")) s = s.replaceAll("  ", " ");
        
        s = s.trim();
        
        for ( int i = 1 ; i < q ; i++ ) s = s + "_";
        
        System.out.println(s);
        
        Set<String> tokens = new HashSet<String>();
        
        for ( int i = 0 ; i < s.length() - 2 ; i++) 
            tokens.add(s.substring(i, i + 3));
        
        return tokens;
    }
}

