/**
 * 
 */
package org.aksw.patternsearch;

import java.util.List;

import org.aksw.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface PatternSearcher {

    /**
     * Extracts patterns from a SINGLE sentence! Implementing classes
     * can decide which tags they should use.
     * 
     * @param taggedSentence
     * @return
     */
    public List<Pattern> extractPatterns(String taggedSentence);
    
    /**
     * Merges the NE or POS tags of subsequent words so that those words 
     * are only one token long. Also needs to remove tags from the starting word
     * so that only one tag per token exists. The tagged sentences needs to be
     * separated with " " (spaces)!
     * 
     * @param nerTaggedSentence
     * @return
     */
    public List<String> mergeTagsInSentences(String taggedSentence);
}
