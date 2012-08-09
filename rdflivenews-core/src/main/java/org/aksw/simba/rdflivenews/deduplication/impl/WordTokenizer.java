/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class WordTokenizer implements Tokenizer{

    @Override
    public Set<String> tokenize(String s, int q) {
        s = s.toLowerCase();
        String[] split = s.split(" ");
        Set<String> result = new HashSet<String>();
        result.addAll(Arrays.asList(split));
        return result;
    }
    
}
