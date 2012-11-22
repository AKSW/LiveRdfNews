/**
 * 
 */
package org.aksw.simba.rdflivenews.entity;

import org.aksw.simba.rdflivenews.Constants;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Entity {

    private String label;
    private String uri;
    private String type = Constants.OWL_THING;
    
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

        String entity = "";

        if ( this.uri != null ) {
            
            entity += this.uri.replace(Constants.DBPEDIA_RESOURCE_PREFIX, "dbpr:")
                    .replace(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX, "rln:");
        }
        
        entity += " @en:" + this.label;
        
        if ( this.type != null )
            entity += " (" + this.type.replace(Constants.DBPEDIA_ONTOLOGY_PREFIX, "dbpo:").replace(Constants.OWL_THING, "owl:Thing") + ")";
        
        return entity;
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
