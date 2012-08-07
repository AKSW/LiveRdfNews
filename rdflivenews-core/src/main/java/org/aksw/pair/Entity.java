/**
 * 
 */
package org.aksw.pair;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Entity {

    private String label;
    private String type;
    
    /**
     * @param label
     * @param type
     */
    public Entity(String label, String type) {

        this.label = label;
        this.type = type;
    }

    /**
     * @return the label
     */
    public String getLabel() {
    
        return label;
    }
    
    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
    
        this.label = label;
    }
    
    /**
     * @return the type
     */
    public String getType() {
    
        return type;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(String type) {
    
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Entity other = (Entity) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        }
        else
            if (!label.equals(other.label))
                return false;
        if (type == null) {
            if (other.type != null)
                return false;
        }
        else
            if (!type.equals(other.type))
                return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return this.label + " (" + this.type + ")";
    }
}
