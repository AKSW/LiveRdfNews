/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.filter.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPatternFilter implements PatternFilter {
    
    /**
     * use this list to modify the list of stop words
     */
    private static final Set<String> FILTER_TOKENS = new HashSet<>(Arrays.asList("about", "last", "been", "also", "its", "over", "one", "but", "which", "their", "are", "were", "after", "had", "be", "it", ":", "have", "--", "who", "-LRB-", "an", "-RRB-", "has", "he", "his", "``", "as", "from", "by", "was", "with", "''", "at", "that", "is", "for", "on", "'s", "a", "in", "and", "to", "of", "the", ","));

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.filter.PatternFilter#filter(java.util.List)
     */
    @Override
    public List<Pattern> filter(List<Pattern> patternsOfIteration) {

        Iterator<Pattern> patternIterator = patternsOfIteration.iterator();
        while ( patternIterator.hasNext() ) {
            
            String nlr = patternIterator.next().getNaturalLanguageRepresentation().toLowerCase().trim();
            
            Set<String> naturalLanguageRepresentationChunks = new HashSet<String>(Arrays.asList(nlr.split(" ")));
            naturalLanguageRepresentationChunks.removeAll(Constants.STOP_WORDS);
            naturalLanguageRepresentationChunks.removeAll(FILTER_TOKENS);
            
            // remove this from the list of all patterns
            if ( naturalLanguageRepresentationChunks.isEmpty() ) {
                
                patternIterator.remove();
                continue;
            }
            
            // patterns which start with and are usually crap
            if ( nlr.startsWith("and ") ) {
                
                patternIterator.remove();
                continue;
            }
        }
        
        return patternsOfIteration;
    }
}
