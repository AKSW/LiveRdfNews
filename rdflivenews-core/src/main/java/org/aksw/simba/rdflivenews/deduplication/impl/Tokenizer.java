/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.Set;

/**
 *
 * @author ngonga
 */
public interface Tokenizer {
    Set<String> tokenize(String s, int q);
}
