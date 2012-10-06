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
import org.aksw.simba.rdflivenews.RdfLiveNews;
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
    private static final Set<String> FILTER_TOKENS = new HashSet<>(Arrays.asList(null, "", "•", "about", "last", "been", "also", "its", "over", "one", "but", "which", "their", "are", "were", "after", "had", "be", "it", ":", "have", "--", "who", "-lrb-", "an", "-rrb-", "has", "he", "his", "``", "as", "from", "by", "was", "with", "''", "at", "|", "http", "that", "is", "for", "on", "'s", "a", "in", "and", "to", "of", "the", ","));

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.filter.PatternFilter#filter(java.util.List)
     */
    @Override
    public List<Pattern> filter(List<Pattern> patternsOfIteration) {

        Iterator<Pattern> patternIterator = patternsOfIteration.iterator();
        while ( patternIterator.hasNext() ) {
            
            Pattern pattern = patternIterator.next();
            String nlr = pattern.getNaturalLanguageRepresentation().toLowerCase().trim();
            String tags = pattern.getNaturalLanguageRepresentationWithTags();
            
            Set<String> naturalLanguageRepresentationChunks = new HashSet<String>(Arrays.asList(nlr.split(" ")));
            naturalLanguageRepresentationChunks.removeAll(Constants.STOP_WORDS);
            naturalLanguageRepresentationChunks.removeAll(FILTER_TOKENS);
            
            // remove this from the list of all patterns
            if ( naturalLanguageRepresentationChunks.isEmpty() ) {
                
                patternIterator.remove();
                continue;
            }
            
            // patterns which start or end with "and" are usually crap
            if ( nlr.startsWith("and ") || nlr.endsWith("and") || nlr.contains(":") || nlr.contains("#") || nlr.contains("http") || nlr.contains("|") || 
                 (nlr.contains("<") && nlr.contains(">") || nlr.contains("p.m.") || nlr.contains("a.m.") || nlr.contains("/") )   ) {
                
                patternIterator.remove();
                continue;
            }
            
            // we can only check this if the pattern search is part of speech tag based
            if ( RdfLiveNews.CONFIG.getStringSetting("search", "method").equals("POS") ) {

                // we want to remove all patterns which do not have a verb or a noun in it
                if ( !tags.contains("_NN") && !tags.contains("_V") ) {
                    
                    patternIterator.remove();
                    continue;
                }
            }
            
            // to long patterns are useless
            if ( nlr.length() > 50 || nlr.matches("^[A-Z].*") || nlr.matches(".* [A-Z] .*") ) {
                
                patternIterator.remove();
                continue;
            }
            
            // TODO
            // filter patterns where one of the entity it was learned from is almost always the same
        }
        
        return patternsOfIteration;
    }
}
