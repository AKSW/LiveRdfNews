/**
 * 
 */
package org.aksw.pattern;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.pair.EntityPair;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface Pattern {

    /**
     * 
     * @param patternString
     */
    public void setNaturalLanguageRepresentation(String patternString);

    /**
     * 
     * @param patternStringWithTags
     */
    public void setNaturalLanguageRepresentationWithTags(String patternStringWithTags);
 
    /**
     * 
     * @return
     */
    public String getNaturalLanguageRepresentation();
    
    /**
     * 
     */
    public void increaseOccurrence();

    /**
     * 
     * @return
     */
    public List<EntityPair> getLearnedFromEntities();

    /**
     * 
     * @param learnedFromPair
     */
    public void addLearnedFromEntities(EntityPair learnedFromEntities);
    
    /**
     * 
     * @param learnedFromEntities
     */
    public void addManyLearnedFromEntities(List<EntityPair> pairs);

    /**
     * 
     * @param generateFavouriteType
     */
    public void setFavouriteTypeFirstEntity(String generateFavouriteType);
    
    /**
     * 
     * @param generateFavouriteType
     */
    public void setFavouriteTypeSecondEntity(String generateFavouriteType);

    /**
     * 
     * @return
     */
    public Map<String, Integer> getTypesSecondEntity();

    /**
     * 
     * @return
     */
    public Map<String, Integer> getTypesFirstEntity();
    
    /**
     * 
     * @return
     */
    public Set<Integer> getLuceneSentenceIds();

    /**
     * 
     * @return
     */
    public int getTotalOccurrence();
    
    /**
     * @return a list of lucene document id's in which the pattern was found
     */
    public Set<Integer> getFoundInSentencesIds();
}
