package org.aksw.simba.rdflivenews.pattern.refinement;

import java.util.List;

import org.aksw.simba.rdflivenews.pattern.Pattern;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface PatternRefiner {

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern);
    
    /**
     * 
     * @param patterns
     */
    public void refinePatterns(List<Pattern> patterns);
}
