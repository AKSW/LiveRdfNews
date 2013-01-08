/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.comparator;

import java.util.Comparator;

import org.aksw.simba.rdflivenews.pair.EntityPair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 * This comparator can be used to sort a list of patterns according to 
 * the patterns number of occurrence. Patterns with highest occurrences
 * come first.
 */
public class PatternEntityLabelComparator implements Comparator<EntityPair> {

    @Override
    public int compare(EntityPair o1, EntityPair o2) {

        int compare = o1.getFirstEntity().getLabel().compareTo(o1.getFirstEntity().getLabel());
        if ( compare != 0 ) return compare;
        else return o1.getSecondEntity().getLabel().compareTo(o2.getSecondEntity().getLabel());
    }
}
