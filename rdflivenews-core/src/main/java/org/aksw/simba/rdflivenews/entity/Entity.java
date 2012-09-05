/**
 * 
 */
package org.aksw.simba.rdflivenews.entity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Entity {

    private String label;
    private String uri;
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
    
    /**
     * @return the uri
     */
    public String getUri() {
    
        return uri;
    }

    
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
    
        this.uri = uri;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return this.uri + " @en:" + this.label + " (" + this.type + ")";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
        if (uri == null) {
            if (other.uri != null)
                return false;
        }
        else
            if (!uri.equals(other.uri))
                return false;
        return true;
    }
}
