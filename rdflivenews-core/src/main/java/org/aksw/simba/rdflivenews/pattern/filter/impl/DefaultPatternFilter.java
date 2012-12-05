/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.filter.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.filter.PatternFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.chainsaw.Main;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


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
            
            Set<String> naturalLanguageRepresentationLowerCaseChunks = new HashSet<String>(Arrays.asList(nlr.split(" ")));
            naturalLanguageRepresentationLowerCaseChunks.removeAll(Constants.STOP_WORDS);
            naturalLanguageRepresentationLowerCaseChunks.removeAll(FILTER_TOKENS);
            
            Set<String> naturalLanguageRepresentationNormalCaseChunks = new HashSet<String>(Arrays.asList(pattern.getNaturalLanguageRepresentation().split(" ")));
            naturalLanguageRepresentationNormalCaseChunks.removeAll(Constants.STOP_WORDS);
            naturalLanguageRepresentationNormalCaseChunks.removeAll(FILTER_TOKENS);
            
            // remove this from the list of all patterns
            if ( naturalLanguageRepresentationLowerCaseChunks.isEmpty() ) {
                
                patternIterator.remove();
                continue;
            }
            
            // cleaned patterns which contain only uppercase letters like ", FL" are removed
            boolean isAllUpperCase = true;
            for ( String chunk : naturalLanguageRepresentationNormalCaseChunks) if ( !StringUtils.isAllUpperCase(chunk.trim()) ) isAllUpperCase = false;
            if ( isAllUpperCase ) {

                patternIterator.remove();
                continue;
            }
            
            // patterns which start or end with "and" are usually crap
            if ( nlr.startsWith("and ") || nlr.startsWith(";") || nlr.startsWith("&") || nlr.endsWith("and") || nlr.contains(":") || nlr.contains("#") || nlr.contains("http") || nlr.contains("|") || 
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
            if ( nlr.length() > 50 || Character.isUpperCase(pattern.getNaturalLanguageRepresentation().charAt(0)) 
                    || pattern.getNaturalLanguageRepresentation().matches(".* [A-Z] .*") ) {
                
                patternIterator.remove();
                continue;
            }
            // patterns with too much strange characters are deleted
            if ( nlr.length() - nlr.replaceAll("[^A-Za-z0-9 ,'`]", "").length() > 1 ) {
                
                patternIterator.remove();
                continue;
            }
            
            if ( nlr.matches("; [Ee]diting by") ) {
                
                patternIterator.remove();
                continue;
            }
            
            // TODO
            // filter patterns where one of the entity it was learned from is almost always the same
        }
        
        return patternsOfIteration;
    }
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        
        List<Pattern> patterns = new ArrayList<>();
        Pattern p = new DefaultPattern(", FL");
        patterns.add(p);
        
        DefaultPatternFilter f = new DefaultPatternFilter();
        f.filter(patterns);
        
        System.out.println(patterns);
    }
}
