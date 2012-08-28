/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.Deduplication;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public abstract class DefaultDeduplication implements Deduplication {

    protected int fromTimeSlice;
    protected int toTimeSlice;
    protected int windowSize;
    protected Set<Integer> duplicateIds = new HashSet<>();
    
    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.deduplication.Deduplication#runDeduplication(int, int)
     */
    public Set<Integer> runDeduplication(int fromTimeSlice, int toTimeSlice, int windowSize) {

        this.fromTimeSlice = fromTimeSlice;
        this.toTimeSlice = toTimeSlice;
        this.windowSize = windowSize;
        
        // 1. load index of all data before fromFrame
        Set<String> source = getSource();
        Set<String> target = getTarget(); 
        
//        System.out.println("SOURCE = "+source.size());
//        for (String s : source ) System.out.println(s);
//        System.out.println("TARGET = "+target.size());
//        for (String t : target ) System.out.println(t);
        // 2. deduplicate & delete duplicates for the current time slices, i.e., target
        target.removeAll(deduplicate(target, target));
//        System.out.println("SOURCE");
//        for ( String s : source) System.out.println(s);
//        System.out.println(" ");
//        System.out.println(" ");
        
//        System.out.println("TARGET");
//        for (String t : target ) System.out.println(t);
                
        // 3. deduplicate & delete duplicates for the old and new data
        deduplicate(source, target);
        deduplicateClones();
        return null;
    }

    
    /**
     * @param fromTimeSlice the fromTimeSlice to set
     */
    public void setFromTimeSlice(int fromTimeSlice) {
    
        this.fromTimeSlice = fromTimeSlice;
    }

    
    /**
     * @param toTimeSlice the toTimeSlice to set
     */
    public void setToTimeSlice(int toTimeSlice) {
    
        this.toTimeSlice = toTimeSlice;
    }

    
    /**
     * @param windowSize the windowSize to set
     */
    public void setWindowSize(int windowSize) {
    
        this.windowSize = windowSize;
    }
}
