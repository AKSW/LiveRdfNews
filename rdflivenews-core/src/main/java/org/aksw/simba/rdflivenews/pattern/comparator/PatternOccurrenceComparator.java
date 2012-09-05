/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.comparator;

import java.util.Comparator;

import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 * This comparator can be used to sort a list of patterns according to 
 * the patterns number of occurrence. Patterns with highest occurrences
 * come first.
 */
public class PatternOccurrenceComparator implements Comparator<Pattern> {

    @Override
    public int compare(Pattern o1, Pattern o2) {

        return o2.getTotalOccurrence() - o1.getTotalOccurrence() ;
    }
}
