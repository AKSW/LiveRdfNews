/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.search.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.search.PatternSearcher;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PartOfSpeechTagPatternSearcher implements PatternSearcher {
    
    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.simba.rdflivenews.patternsearch.PatternSearcher#extractPatterns(java.lang.String)
     */
    public List<Pattern> extractPatterns(String sentence, String posTaggedSentence, int luceneSentenceId) {

        List<Pattern> patterns       = new ArrayList<Pattern>();
        List<String> mergedSentence = mergeTagsInSentences(posTaggedSentence);
        
        for (int i = 0; i < mergedSentence.size(); i++) {
            
            // no pattern start found so far, so just skip the token
            if ( !mergedSentence.get(i).endsWith("_NNP") && patterns.isEmpty() ) continue;
            else {
                
                Pattern pattern             = new DefaultPattern();
                Entity firstEntity          = new Entity(mergedSentence.get(i).substring(0, mergedSentence.get(i).lastIndexOf("_")), Constants.OWL_THING);
                Entity secondEntity         = null;

                List<String> nlrWithoutTags = new ArrayList<String>();
                List<String> nlrWithTags    = new ArrayList<String>();

                for (int j = i + 1; j < mergedSentence.size(); j++) {
                    
                    String argument2 = mergedSentence.get(j);
                    
                    // add the word
                    if ( !mergedSentence.get(j).endsWith("_NNP") ) {
                        
                        nlrWithoutTags.add(argument2.substring(0, argument2.lastIndexOf("_")));
                        nlrWithTags.add(mergedSentence.get(j));
                    }
                    // pattern is finished, so add the second argument
                    else {

                        secondEntity = new Entity(argument2.substring(0, argument2.lastIndexOf("_")), Constants.OWL_THING);
                        
                        i = j - 1;
                        break;
                    }
                }
                
                // the last pattern of a sentence will never have a closing argument
                // also filter out empty patterns
                if ( nlrWithoutTags != null && !nlrWithoutTags.isEmpty() && 
                        secondEntity != null && !secondEntity.getLabel().isEmpty() ) {
                    
                    if ( RdfLiveNews.CONFIG.getBooleanSetting("search", "ignoreDays") ) 
                        if ( Constants.WEEK_DAYS.contains(firstEntity.getLabel()) || 
                             Constants.WEEK_DAYS.contains(secondEntity.getLabel()) || 
                             firstEntity.getLabel().equals(secondEntity.getLabel()) ) 
                                    continue;

                    pattern.addLearnedFromEntities(new EntityPair(firstEntity,secondEntity,luceneSentenceId));
                    pattern.setNaturalLanguageRepresentation(StringUtils.join(nlrWithoutTags, " ").replaceAll(" [Tt]he$", ""));
                    pattern.setNaturalLanguageRepresentationWithTags(StringUtils.join(nlrWithTags, " ").replaceAll(" [Tt]he_[A-z]*$", ""));
                    pattern.setExampleSentence(sentence);
                    
                    patterns.add(pattern);
                }
            }
        }
        
        return patterns;
    }
    
    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.simba.rdflivenews.patternsearch.PatternSearcher#mergeTagsInSentences(java.lang.String)
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
