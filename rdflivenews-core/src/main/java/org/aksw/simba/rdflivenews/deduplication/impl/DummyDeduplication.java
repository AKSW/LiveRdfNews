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

    public Set<String> getSource(int fromTimeSlice, int window) {

        return new HashSet<String>();
    }

    public Set<String> getTarget(int fromTimeSlice, int toTimeSlice) {

        return new HashSet<String>();
    }

    public Set<String> deduplicate(Set<String> source, Set<String> target, int fromTimeSlice) {

        return new HashSet<String>();
    }
}
