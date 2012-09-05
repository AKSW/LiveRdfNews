/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.filter;

import java.util.List;

import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface PatternFilter {

    /**
     * Removes patterns from the list which to not abide certain 
     * thresholds, for example should remove patterns which only
     * contain stop words.
     * 
     * @param patternsOfIteration
     * @return 
     */
    public List<Pattern> filter(List<Pattern> patternsOfIteration);
}
