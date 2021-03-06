/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.search.impl;

import java.util.ArrayList;
import java.util.List;

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
public class NamedEntityTagPatternSearcher implements PatternSearcher {

    /**
     * 
     */
    public List<Pattern> extractPatterns(String sentence, String nerTaggedSentence, int luceneSentenceId) {

        List<Pattern> patterns      = new ArrayList<Pattern>();
        List<String> mergedSentence = mergeTagsInSentences(nerTaggedSentence);

        for (int i = 0; i < mergedSentence.size(); i++) {

            // no pattern start found so far, so just skip the token
            if ( mergedSentence.get(i).endsWith("_OTHER") && patterns.isEmpty() ) continue;
            else {
                
                Pattern pattern             = new DefaultPattern();
                Entity firstEntity          = new Entity(mergedSentence.get(i).substring(0, mergedSentence.get(i).lastIndexOf("_")), 
                                                         mergedSentence.get(i).substring(mergedSentence.get(i).lastIndexOf("_") + 1));
                Entity secondEntity         = null;
                
                List<String> nlrWithoutTags = new ArrayList<String>();
                List<String> nlrWithTags    = new ArrayList<String>();

                for (int j = i + 1; j < mergedSentence.size(); j++) {
                    
                    String argument2 = mergedSentence.get(j);
                    
                    // add the word
                    if ( mergedSentence.get(j).endsWith("_OTHER") ) {
                        
                        nlrWithoutTags.add(argument2.substring(0, argument2.lastIndexOf("_")));
                        nlrWithTags.add(mergedSentence.get(j));
                    }
                    // pattern is finished, so add the second argument
                    else {

                        secondEntity = new Entity(argument2.substring(0, argument2.lastIndexOf("_")), 
                                                  argument2.substring(argument2.lastIndexOf("_") + 1));
                        
                        i = j - 1;
                        break;
                    }
                }
                
                // the last pattern of a sentence will never have a closing argument
                // also filter out empty patterns
                if ( nlrWithoutTags != null && !nlrWithoutTags.isEmpty() && 
                        secondEntity != null && !secondEntity.getLabel().isEmpty() ) {

                    pattern.addLearnedFromEntities(new EntityPair(firstEntity,secondEntity,luceneSentenceId));
                    pattern.setNaturalLanguageRepresentation(StringUtils.join(nlrWithoutTags, " ").replaceAll(" the$", ""));
                    pattern.setNaturalLanguageRepresentationWithTags(StringUtils.join(nlrWithTags, " ").replaceAll(" the_[A-z]*$", ""));
                    
                    patterns.add(pattern);
                }
            }
        }

        return patterns;
    }

    /**
     * 
     */
    public List<String> mergeTagsInSentences(String nerTaggedSentence) {

        List<String> tokens = new ArrayList<String>();
        String lastToken = "";
        String lastTag = "";
        String currentTag = "";
        String newToken = "";
        
        for (String currentToken : nerTaggedSentence.split(" ")) {

            currentTag = currentToken.substring(currentToken.lastIndexOf("_") + 1);

            // we need to check for the previous token's tag
            if (!currentToken.endsWith("_OTHER")) {

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
    
    public static void main(String[] args) {

        String nerTaggedSentence = "Mr._PERSON X_PERSON said_OTHER :_OTHER \"_OTHER Test_OTHER Test_OTHER \"_OTHER and_OTHER \"_OTHER Test_OTHER \"_OTHER \"_OTHER";
        
        System.out.println(nerTaggedSentence);
    }
}
