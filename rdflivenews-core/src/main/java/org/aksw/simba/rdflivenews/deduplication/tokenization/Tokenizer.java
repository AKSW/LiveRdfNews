/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.tokenization;

import java.util.Set;

/**
 *
 * @author ngonga
 */
public interface Tokenizer {
    
    /**
     * 
     * @param s
     * @param q
     * @return
     */
    public Set<String> tokenize(String s, int q);
}
