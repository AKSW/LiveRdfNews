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
public class DummyDeduplication extends DefaultDeduplication {

    public Set<String> getSource() {

        return new HashSet<String>();
    }

    public Set<String> getTarget() {

        return new HashSet<String>();
    }

    public Set<String> deduplicate(Set<String> source, Set<String> target) {

        return new HashSet<String>();
    }

    public void deduplicateClones() {
    }
}
