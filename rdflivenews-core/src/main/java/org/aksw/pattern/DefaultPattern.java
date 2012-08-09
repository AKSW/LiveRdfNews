/**
 * 
 */
package org.aksw.pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.entity.Entity;
import org.aksw.pair.EntityPair;
import org.aksw.pair.Pair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPattern implements Pattern {

    private String naturalLanguageRepresentation;
    private String naturalLanguageRepresentationWithTags;
    
    private Map<Integer,EntityPair> entityPairs;
    
    private int totalOccurrence;
    private Map<String, Integer> typesFirstEntity;
    private Map<String, Integer> typesSecondEntity;
    private String favouriteTypeSecondEntity;
    private String favouriteTypeFirstEntity;
    private Set<Integer> luceneSentenceIds;
    
    /**
     * 
     * @param patternString
     */
    public DefaultPattern(String patternString) {

        this.entityPairs = new HashMap<Integer,EntityPair>();
        this.typesFirstEntity = new HashMap<String,Integer>();
        this.typesSecondEntity = new HashMap<String,Integer>();
        this.luceneSentenceIds = new HashSet<Integer>();
        this.favouriteTypeFirstEntity = "";
        this.favouriteTypeSecondEntity = "";
        this.naturalLanguageRepresentation = patternString;
        this.totalOccurrence = 1;
    }

    /**
     * 
     */
    public DefaultPattern() {
        
        this("N/A");
    }

    /**
     * 
     */
    public void increaseOccurrence() {

        this.totalOccurrence++;
    }

    /**
     * 
     */
    public void setNaturalLanguageRepresentation(String patternString) {

        this.naturalLanguageRepresentation = patternString;
    }

    /**
     * 
     */
    public void setNaturalLanguageRepresentationWithTags(String patternStringWithTags) {

        this.naturalLanguageRepresentationWithTags = patternStringWithTags;
    }

    /**
     * 
     */
    public String getNaturalLanguageRepresentation() {

        return this.naturalLanguageRepresentation;
    }
    
    /**
     * 
     */
    public void setFavouriteTypeFirstEntity(String favouriteTypeFirstEntity) {

        this.favouriteTypeFirstEntity = favouriteTypeFirstEntity;
    }

    /**
     * 
     */
    public void setFavouriteTypeSecondEntity(String favouriteTypeSecondEntity) {

        this.favouriteTypeSecondEntity = favouriteTypeSecondEntity;        
    }

    /**
     * 
     */
    public Map<String, Integer> getTypesSecondEntity() {

        return this.typesSecondEntity;
    }

    /**
     * 
     */
    public Map<String, Integer> getTypesFirstEntity() {

        return this.typesFirstEntity;
    }
    
    /**
     * 
     * @param pair
     */
    public void addLearnedFromEntities(EntityPair pair) {

        if ( this.entityPairs.containsKey(pair.hashCode()) ) {
            
            EntityPair oldPair = ((EntityPair) this.entityPairs.get(pair.hashCode()));
            oldPair.increaseOccurrence();
            oldPair.addLuceneSentencIds(pair.getLuceneSentenceIds());
        }
        else this.entityPairs.put(pair.hashCode(), pair);
    }
    
    /**
     * 
     * @param pair
     */
    public void addManyLearnedFromEntities(List<EntityPair> pairs) {

        for ( EntityPair pair : pairs )
            this.addLearnedFromEntities(pair);
    }

    /**
     * 
     */
    public List<EntityPair> getLearnedFromEntities() {

        return new ArrayList<EntityPair>(this.entityPairs.values());
    }
    
    /**
     * @return all <bold>new</bold> entities
     */
    public List<EntityPair> getNewEntities(){
        
        List<EntityPair> pairs = new ArrayList<EntityPair>();
        for ( EntityPair pair : this.entityPairs.values() ) if ( pair.isNew() ) pairs.add(pair);
                
        return pairs;
    }
    
    /**
     * 
     */
    public Set<Integer> getLuceneSentenceIds() {

        return this.luceneSentenceIds;
    }
    
    /**
     * 
     */
    public int getTotalOccurrence() {

        return this.entityPairs.size();
    }

    /**
     * 
     */
    public Set<Integer> getFoundInSentencesIds() {

        Set<Integer> sentenceIds = new HashSet<Integer>();
        for ( EntityPair pair : this.entityPairs.values() )
            sentenceIds.addAll(pair.getLuceneSentenceIds());
        
        return sentenceIds;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Pattern: arg1 "      +  this.naturalLanguageRepresentation + " arg2");
        builder.append("\nTagged-Pattern: arg1 "  + this.naturalLanguageRepresentationWithTags + " arg2");
        builder.append("\nOccurrence: "    + this.totalOccurrence);
        
        int i = 1;
        for ( Pair<Entity,Entity> pair : this.entityPairs.values() )
            builder.append("\n\t"+ i++ +": " + pair);
                
        return builder.append("\n").toString();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((naturalLanguageRepresentation == null) ? 0 : naturalLanguageRepresentation.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultPattern other = (DefaultPattern) obj;
        if (naturalLanguageRepresentation == null) {
            if (other.naturalLanguageRepresentation != null)
                return false;
        }
        else
            if (!naturalLanguageRepresentation.equals(other.naturalLanguageRepresentation))
                return false;
        return true;
    }
}
