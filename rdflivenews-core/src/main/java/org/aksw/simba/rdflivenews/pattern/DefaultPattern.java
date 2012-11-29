/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pair.Pair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPattern implements Pattern {

    private String naturalLanguageRepresentation;
    private String naturalLanguageRepresentationWithTags = "";

    private Map<Integer,EntityPair> entityPairs;
    
    private int totalOccurrence;
    private Double score;
    private String favouriteTypeSecondEntity;
    private String favouriteTypeFirstEntity;
    private String exampleSentence;
    private String clazz;
    
    /**
     * 
     * @param patternString
     */
    public DefaultPattern(String patternString) {

        this.entityPairs = new HashMap<Integer,EntityPair>();
        this.favouriteTypeFirstEntity = "";
        this.favouriteTypeSecondEntity = "";
        this.naturalLanguageRepresentation = patternString;
        this.totalOccurrence = 1;
        this.exampleSentence = "";
        this.score = 0D;
    }
    
    /**
     * 
     * @param patternString
     */
    public DefaultPattern(String patternString, String taggedString) {
        
        this(patternString);
        this.naturalLanguageRepresentationWithTags = taggedString;
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
    public void increaseOccurrence(int i) {

        this.totalOccurrence += i;
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
    public String getNaturalLanguageRepresentation() {

        return this.naturalLanguageRepresentation;
    }

    /**
     * 
     */
    public void setNaturalLanguageRepresentationWithTags(String patternStringWithTags) {

        this.naturalLanguageRepresentationWithTags = patternStringWithTags;
    }
    
    /**
     * @return the naturalLanguageRepresentationWithTags
     */
    public String getNaturalLanguageRepresentationWithTags() {
    
        return naturalLanguageRepresentationWithTags;
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
     * @return the favouriteTypeSecondEntity
     */
    public String getFavouriteTypeSecondEntity() {
    
        return favouriteTypeSecondEntity;
    }

    
    /**
     * @return the favouriteTypeFirstEntity
     */
    public String getFavouriteTypeFirstEntity() {
    
        return favouriteTypeFirstEntity;
    }

    /**
     * 
     */
    public List<String> getTypesSecondEntity() {

        List<String> types = new ArrayList<String>();
        for ( EntityPair entityPair : this.entityPairs.values()) types.add(entityPair.getSecondEntity().getType());
        
        return types;
    }

    /**
     * 
     */
    public List<String> getTypesFirstEntity() {

        List<String> types = new ArrayList<String>();
        for ( EntityPair entityPair : this.entityPairs.values()) types.add(entityPair.getFirstEntity().getType());
        
        return types;
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

        Set<Integer> luceneSentencesIds = new TreeSet<Integer>();
        for ( EntityPair pair : this.entityPairs.values() ) luceneSentencesIds.addAll(pair.getLuceneSentenceIds());
        return luceneSentencesIds;
    }
    
    /**
     * 
     */
    public int getTotalOccurrence() {

        int occurrence = 0;
        for ( EntityPair pair : this.entityPairs.values() ) occurrence += pair.getOccurrence();
        return occurrence;
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
        builder.append("Pattern: "+this.favouriteTypeFirstEntity+" "      +  this.naturalLanguageRepresentation + " " + this.favouriteTypeSecondEntity);
        builder.append("\nTagged-Pattern: arg1 "  + this.naturalLanguageRepresentationWithTags + " arg2");
        builder.append("\nExample: " + this.exampleSentence);
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

    @Override
    public void setScore(Double score) {

        this.score = score;
    }

    @Override
    public Double getScore() {

        return this.score;
    }

    /**
     * @return the exampleSentence
     */
    public String getExampleSentence() {
    
        return exampleSentence;
    }

    /**
     * @param exampleSentence the exampleSentence to set
     */
    public void setExampleSentence(String exampleSentence) {
    
        this.exampleSentence = exampleSentence;
    }

    @Override
    public boolean isAboveThresholds() {

        return this.getTotalOccurrence() >= RdfLiveNews.CONFIG.getIntegerSetting("scoring", "occurrenceThreshold");
    }

    /**
     * @return the clazz
     */
    public String getClazz() {

        return clazz;
    }

    /**
     * @param clazz the clazz to set
     */
    public void setClazz(String clazz) {

        this.clazz = clazz;
    }
}
