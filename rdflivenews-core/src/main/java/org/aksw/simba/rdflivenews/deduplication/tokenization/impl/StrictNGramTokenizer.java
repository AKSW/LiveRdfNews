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
public class StrictNGramTokenizer implements Tokenizer {

    private int ngramSize = 3;

    public Set<String> tokenize(String s, int q) {
        if (s == null) {
            s = "";
        }

        //remove double blanks        
        while (s.contains("  ")) {
            s = s.replaceAll("  ", " ");
        }
        s = s.trim();

        while (s.length() < q) {
            s = s + "_";
        }

        Set<String> tokens = new HashSet<String>();
        for (int i = 0; i < s.length() - q + 1; i++) {
            tokens.add(s.substring(i, i + q));
        }
        return tokens;
    }

    public static void main(String args[]) {
        StrictNGramTokenizer ng = new StrictNGramTokenizer();
        System.out.println(ng.tokenize("12345", 3));
    }

    @Override
    public Set<String> tokenize(String s) {
        return tokenize(s, ngramSize);
    }
}
