/**
 * 
 */
package org.aksw.simba.rdflivenews.deduplication;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface Deduplication {

    /**
     * 
     * @param fromTimeSlice
     * @param toTimeSlice
     */
    public void deduplicate(int fromTimeSlice, int toTimeSlice);
}
