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

    private int ngramSize = 3;
    
    public NGramTokenizer(int ngramSize) {
        
        this.ngramSize = ngramSize;
    }
    
    public Set<String> tokenize(String s) {

        if (s == null) {
            s = "";
        }

        //remove double blanks
        while (s.contains("  ")) {
            s = s.replaceAll("  ", " ");
        }

        s = s.trim();

        for (int i = 1; i < this.ngramSize; i++) {
            s = s + "_";
        }

        Set<String> tokens = new HashSet<String>();

        for (int i = 0; i < s.length() - this.ngramSize + 1; i++) {
            tokens.add(s.substring(i, i + this.ngramSize));
        }

        return tokens;
    }
}
