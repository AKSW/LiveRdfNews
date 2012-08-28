/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication;

import java.util.List;
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
     * @return the primary key of every sentence which is not duplicate with respect to all time slices so far
     */
    public Set<Integer> runDeduplication(int fromTimeSlice, int toTimeSlice, int windowSize);
    
    /**
     * 
     * @param fromTimeSlice
     * @param window
     * @return
     */
    public Set<String> getSource();
    
    /**
     * 
     * @param fromTimeSlice
     * @param toTimeSlice
     * @return
     */
    public Set<String> getTarget();
    
    /**
     * 
     * @param source
     * @param target
     * @param fromTimeSlice
     * @return
     */
    public Set<String> deduplicate(Set<String> source, Set<String> target);
    
    /**
     * 
     */
    public void deduplicateClones();
    
    /**
     * 
     * @param fromTimeSlice
     */
    public void setFromTimeSlice(int fromTimeSlice);
    
    /**
     * 
     * @param toTimeSlice
     */
    public void setToTimeSlice(int toTimeSlice);
    
    /**
     * 
     * @param windowSize
     */
    public void setWindowSize(int windowSize);
}
