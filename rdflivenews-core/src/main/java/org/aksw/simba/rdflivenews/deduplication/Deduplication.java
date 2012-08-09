/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication;

/**
 *
 * @author ngonga
 */
public interface Deduplication {
    public void runDeduplication(int fromFrame, int toFrame);
}
