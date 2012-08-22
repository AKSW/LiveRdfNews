/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.scoring;

import java.util.List;

import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface PatternScorer {

    /**
     * 
     * @param pattern
     */
    public void scorePattern(Pattern pattern);
    
    /**
     * 
     * @param patterns
     */
    public void scorePatterns(List<Pattern> patterns);
}
