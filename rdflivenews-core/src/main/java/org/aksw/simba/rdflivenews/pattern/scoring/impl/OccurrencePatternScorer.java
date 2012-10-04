package org.aksw.simba.rdflivenews.pattern.scoring.impl;

import java.util.List;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer;


public class OccurrencePatternScorer implements PatternScorer {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer#scorePattern(org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public void scorePattern(Pattern pattern) {

        if ( pattern.getTotalOccurrence() >= RdfLiveNews.CONFIG.getIntegerSetting("scoring", "occurrenceThreshold") ) 
            pattern.setScore(1D);
        
        else pattern.setScore(0D);
    }

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.scoring.PatternScorer#scorePatterns(java.util.List)
     */
    @Override
    public void scorePatterns(List<Pattern> patterns) {

        for ( Pattern pattern : patterns ) 
            this.scorePattern(pattern);
    }
}
