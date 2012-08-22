/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication;

import java.util.Set;

/**
 *
 * @author ngonga
 */
public interface Deduplication {
    
    /**
     * @param fromTimeSlice
     * @param toTimeSlice
     * @param windowSize - how many timeslices should be considered
     */
    public void runDeduplication(int fromTimeSlice, int toTimeSlice, int windowSize);
    
    /**
     * 
     * @param fromTimeSlice
     * @param window
     * @return
     */
    public Set<String> getSource(int fromTimeSlice, int window);
    
    /**
     * 
     * @param fromTimeSlice
     * @param toTimeSlice
     * @return
     */
    public Set<String> getTarget(int fromTimeSlice, int toTimeSlice);
    
    /**
     * 
     * @param source
     * @param target
     * @param fromTimeSlice
     * @return
     */
    public Set<String> deduplicate(Set<String> source, Set<String> target, int fromTimeSlice);
        public void deduplicateClones(int toTimeSlice);

}
