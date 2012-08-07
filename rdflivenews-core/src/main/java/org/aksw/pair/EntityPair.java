/**
 * 
 */
package org.aksw.pair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EntityPair extends Pair<Entity,Entity> {

    private int occurrence;
    private boolean isNew = true;
    
    /**
     * 
     */
    public EntityPair(Entity firstEntity, Entity secondEntity) {
        super(firstEntity, secondEntity);
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
}
