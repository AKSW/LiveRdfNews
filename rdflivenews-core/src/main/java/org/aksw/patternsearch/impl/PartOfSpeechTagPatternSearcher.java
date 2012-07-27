/**
 * 
 */
package org.aksw.patternsearch.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.pattern.Pattern;
import org.aksw.patternsearch.PatternSearcher;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PartOfSpeechTagPatternSearcher implements PatternSearcher {

    /* (non-Javadoc)
     * @see org.aksw.patternsearch.PatternSearcher#extractPatterns(java.lang.String)
     */
    public List<Pattern> extractPatterns(String taggedSentence) {

        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.aksw.patternsearch.PatternSearcher#mergeTagsInSentences(java.lang.String)
     */
    /**
     * 
     */
    public List<String> mergeTagsInSentences(String nerTaggedSentence) {

        List<String> tokens = new ArrayList<String>();
        String lastToken = "";
        String lastTag = "";
        String currentTag = "";
        String newToken = "";

        for (String currentToken : nerTaggedSentence.replace("_NNPS", "_NNP").split(" ")) {
            
            currentTag = currentToken.substring(currentToken.lastIndexOf("_") + 1);

            // we need to check for the previous token's tag
            if (currentToken.endsWith("_NNP")) {

                // we need to merge the cell
                if (currentTag.equals(lastTag)) {

                    newToken = lastToken.substring(0, lastToken.lastIndexOf("_")) + " " + currentToken;
                    tokens.set(tokens.size() - 1, newToken);
                }
                // different tag found so just add it
                else
                    tokens.add(currentToken);
            }
            else {

                // add the current token
                tokens.add(currentToken);
            }
            // update for next iteration
            lastToken = tokens.get(tokens.size() - 1);
            lastTag = currentTag;
        }
        return tokens;
    }
}
