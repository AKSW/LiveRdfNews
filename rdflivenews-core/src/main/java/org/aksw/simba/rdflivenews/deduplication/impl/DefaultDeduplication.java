/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.deduplication.Deduplication;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public abstract class DefaultDeduplication implements Deduplication {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.deduplication.Deduplication#runDeduplication(int, int)
     */
    public Set<Integer> runDeduplication(int fromTimeSlice, int toTimeSlice, int windowSize) {

        // 1. load index of all data before fromFrame
        Set<String> source = getSource(fromTimeSlice, windowSize);
        Set<String> target = getTarget(fromTimeSlice + 1, toTimeSlice); // target should not include current source
        
//        System.out.println("SOURCE = "+source.size());
//        for (String s : source ) System.out.println(s);
//        System.out.println("TARGET = "+target.size());
//        for (String t : target ) System.out.println(t);
        // 2. deduplicate & delete duplicates for the current time slices, i.e., target
        target.removeAll(deduplicate(target, target, toTimeSlice));
//        System.out.println("SOURCE");
//        for ( String s : source) System.out.println(s);
//        System.out.println(" ");
//        System.out.println(" ");
        
//        System.out.println("TARGET");
//        for (String t : target ) System.out.println(t);
                
        // 3. deduplicate & delete duplicates for the old and new data
        deduplicate(source, target, toTimeSlice);
        deduplicateClones(toTimeSlice);
        return null;
    }
}
