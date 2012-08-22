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
public class OccurrencyPatternScorer implements PatternScorer {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer#scorePattern(org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public void scorePattern(Pattern pattern) {

        pattern.setScore(pattern.getTotalOccurrence() > 5 ? 1D : 0);
    }

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer#scorePatterns(java.util.List)
     */
    @Override
    public void scorePatterns(List<Pattern> patterns) {

        for ( Pattern pattern : patterns ) this.scorePattern(pattern);
    }
}
