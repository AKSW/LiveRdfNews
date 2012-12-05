package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.Set;


public interface Triple {

    /**
     * @return the subjectUri
     */
    public String getSubjectUri();
    
    /**
     * @param subjectUri the subjectUri to set
     */
    public void setSubjectUri(String subjectUri);
    
    /**
     * @return the subjectLabel
     */
    public String getSubjectLabel();
    
    /**
     * @param subjectLabel the subjectLabel to set
     */
    public void setSubjectLabel(String subjectLabel) ;
    
    /**
     * @return the subjectType
     */
    public String getSubjectType() ;
    
    /**
     * @param subjectType the subjectType to set
     */
    public void setSubjectType(String subjectType);
    
    /**
     * @return the propertyLabel
     */
    public String getPropertyLabel();
    
    /**
     * @param propertyLabel the propertyLabel to set
     */
    public void setPropertyLabel(String propertyLabel);
    
    /**
     * @return the propertyType
     */
    public String getPropertyType() ;
    
    /**
     * @param propertyType the propertyType to set
     */
    public void setPropertyType(String propertyType);
    
    /**
     * @param object
     */
    public void setObject(String object);
    
    /**
     * @return
     */
    public String getObject();
    
    /**
     * @return the sentenceIds
     */
    public Set<Integer> getSentenceId();

    /**
     * @param sentenceIds the sentenceIds to set
     */
    public void setSentenceId(Set<Integer> ids);
    
    /**
     * @return the patternLabel
     */
    public String getPatternLabel();
    
    /**
     * @param patternLabel the patternLabel to set
     */
    public void setPatternLabel(String patternLabel);

    public String getKey();
}
