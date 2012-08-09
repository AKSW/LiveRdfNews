/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

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
    public void runDeduplication(int fromTimeSlice, int toTimeSlice, int windowSize) {

        // 1. load index of all data before fromFrame
        Set<String> source = getSource(fromTimeSlice, windowSize);
        Set<String> target = getTarget(fromTimeSlice, toTimeSlice);
        // 2. deduplicate & delete duplicates for the current time slices, i.e., target
        target.removeAll(deduplicate(target, target, fromTimeSlice));
        // 3. deduplicate & delete duplicates for the old and new data
        deduplicate(source, target, fromTimeSlice);
    }
}
