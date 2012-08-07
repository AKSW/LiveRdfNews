/**
 * 
 */
package org.aksw.pair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Pair<T1, T2> {

    private T1 firstEntity;
    private T2 secondEntity;
    
    /**
     * 
     * @param firstEntity
     * @param secondEntity
     */
    public Pair(T1 firstEntity, T2 secondEntity) {

        this.firstEntity = firstEntity;
        this.secondEntity = secondEntity;
    }

    
    /**
     * @return the firstEntity
     */
    public T1 getFirstEntity() {
    
        return firstEntity;
    }

    
    /**
     * @param firstEntity the firstEntity to set
     */
    public void setFirstEntity(T1 firstEntity) {
    
        this.firstEntity = firstEntity;
    }

    
    /**
     * @return the secondEntity
     */
    public T2 getSecondEntity() {
    
        return secondEntity;
    }

    
    /**
     * @param secondEntity the secondEntity to set
     */
    public void setSecondEntity(T2 secondEntity) {
    
        this.secondEntity = secondEntity;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstEntity == null) ? 0 : firstEntity.hashCode());
        result = prime * result + ((secondEntity == null) ? 0 : secondEntity.hashCode());
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
        Pair other = (Pair) obj;
        if (firstEntity == null) {
            if (other.firstEntity != null)
                return false;
        }
        else
            if (!firstEntity.equals(other.firstEntity))
                return false;
        if (secondEntity == null) {
            if (other.secondEntity != null)
                return false;
        }
        else
            if (!secondEntity.equals(other.secondEntity))
                return false;
        return true;
    }
}
