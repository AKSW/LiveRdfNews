/**
 * 
 */
package org.aksw.simba.rdflivenews.pair;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.entity.Entity;

import edu.stanford.nlp.util.StringUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EntityPair extends Pair<Entity,Entity> {

    private int occurrence = 1;
    private boolean isNew = true;
    private Set<Integer> luceneSentenceIds = new HashSet<Integer>();
    
    /**
     * @param luceneSentenceId 
     * 
     */
    public EntityPair(Entity firstEntity, Entity secondEntity, int luceneSentenceId) {
        super(firstEntity, secondEntity);
        
        this.addLuceneSentenceId(luceneSentenceId);
    }

    /**
     * 
     * @param luceneSentenceId
     */
    private void addLuceneSentenceId(int luceneSentenceId) {

        this.luceneSentenceIds.add(luceneSentenceId);
    }

    /**
     * 
     */
    public void increaseOccurrence() {

        this.occurrence++;
    }

    
    /**
     * @return the occurrence
     */
    public int getOccurrence() {
    
        return occurrence;
    }

    
    /**
     * @param occurrence the occurrence to set
     */
    public void setOccurrence(int occurrence) {
    
        this.occurrence = occurrence;
    }

    
    /**
     * @return the isNew
     */
    public boolean isNew() {
    
        return isNew;
    }

    
    /**
     * @param isNew the isNew to set
     */
    public void setNew(boolean isNew) {
    
        this.isNew = isNew;
    }

    /**
     * @return the luceneSentenceId
     */
    public Set<Integer> getLuceneSentenceIds() {

        return luceneSentenceIds;
    }

    /**
     * @param luceneSentenceId the luceneSentenceId to set
     */
    public void setLuceneSentenceId(Set<Integer> luceneSentenceIds) {

        this.luceneSentenceIds = luceneSentenceIds;
    }
    
    /**
     * 
     * @param luceneSentenceIds
     */
    public void addLuceneSentencIds(Set<Integer> luceneSentenceIds) {

        this.luceneSentenceIds.addAll(luceneSentenceIds);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return this.firstEntity + " - " + this.secondEntity + " / sentenceIDs: " + StringUtils.join(this.luceneSentenceIds, ", ") + " / occurrence: " + this.occurrence;
    }

    /**
     * 
     * @return
     */
    public boolean hasValidUris() {

        return this.firstEntity.getUri() != null && this.firstEntity.getUri().startsWith("http://") && 
                this.secondEntity.getUri() != null && this.secondEntity.getUri().startsWith("http://");
    }
}
