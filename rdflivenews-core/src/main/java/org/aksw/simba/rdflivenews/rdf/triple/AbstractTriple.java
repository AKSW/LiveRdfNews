package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractTriple implements Triple {

    protected String subjectUri;
    protected String subjectLabel;
    protected String subjectType;
    
    protected String propertyLabel;
    protected String propertyType;
    
    protected String patternLabel;
    
    protected Set<Integer> sentenceIds = new HashSet<>();
    
    /**
     * @return the patternLabel
     */
    public String getPatternLabel() {
    
        return patternLabel;
    }

    /**
     * @param patternLabel the patternLabel to set
     */
    public void setPatternLabel(String patternLabel) {
    
        this.patternLabel = patternLabel;
    }

    /**
     * @return the sentenceIds
     */
    public Set<Integer> getSentenceId() {
    
        return sentenceIds;
    }

    
    /**
     * @param sentenceIds the sentenceIds to set
     */
    public void setSentenceId(Set<Integer> sentenceId) {
        
        this.sentenceIds = sentenceId;
    }

    /**
     * @return the subjectUri
     */
    public String getSubjectUri() {
    
        return subjectUri;
    }
    
    /**
     * @param subjectUri the subjectUri to set
     */
    public void setSubjectUri(String subjectUri) {
    
        this.subjectUri = subjectUri;
    }
    
    /**
     * @return the subjectLabel
     */
    public String getSubjectLabel() {
    
        return subjectLabel;
    }
    
    /**
     * @param subjectLabel the subjectLabel to set
     */
    public void setSubjectLabel(String subjectLabel) {
    
        this.subjectLabel = subjectLabel;
    }
    
    /**
     * @return the subjectType
     */
    public String getSubjectType() {
    
        return subjectType;
    }
    
    /**
     * @param subjectType the subjectType to set
     */
    public void setSubjectType(String subjectType) {
    
        this.subjectType = subjectType;
    }
    
    /**
     * @return the propertyLabel
     */
    public String getPropertyLabel() {
    
        return propertyLabel;
    }
    
    /**
     * @param propertyLabel the propertyLabel to set
     */
    public void setPropertyLabel(String propertyLabel) {
    
        this.propertyLabel = propertyLabel;
    }
    
    /**
     * @return the propertyType
     */
    public String getPropertyType() {
    
        return propertyType;
    }
    
    /**
     * @param propertyType the propertyType to set
     */
    public void setPropertyType(String propertyType) {
    
        this.propertyType = propertyType;
    }
}
