package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;

import edu.stanford.nlp.util.StringUtils;


public class ObjectPropertyTriple extends AbstractTriple {

    private String objectUri;
    private String objectLabel;
    private String refinedSubjectLabel;
    private String refinedObjectLabel;
    
    public ObjectPropertyTriple(String subjectLabel, String subjectUri, String patternLabel, String objectLabel, String objectUri, Set<Integer> id) {

        this.subjectLabel = subjectLabel;
        this.subjectUri = subjectUri;
        this.patternLabel = patternLabel;
        this.objectLabel = objectLabel;
        this.objectUri = objectUri;
        this.sentenceIds.addAll(id);
    }

    @Override
    public void setObject(String objectUri) {

        this.objectUri = objectUri;
    }
    
    @Override
    public String getObject() {

        return this.objectUri;
    }
    
    public String getSubjectUriPrefixed() {
        
        return this.subjectUri.replace(Constants.DBPEDIA_RESOURCE_PREFIX, "dbpr:")
                .replace(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX, "rln:");
    }
    
    public String getObjectUriPrefixed() {
        
        return this.objectUri.replace(Constants.DBPEDIA_RESOURCE_PREFIX, "dbpr:")
                .replace(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX, "rln:");
    }
    
    public String getObjectLabel() {

        return this.objectLabel;
    }
    @Override
    public String getKey() {

        return this.subjectLabel + " " + this.patternLabel + " " + this.objectLabel + " " + StringUtils.join(this.sentenceIds, ",");
    }

    public void setRefinedSubjectLabel(String refinedLabel) {

        this.refinedSubjectLabel = refinedLabel; 
    }

    public void setRefinedObjectLabel(String refinedLabel) {

        this.refinedObjectLabel = refinedLabel;
    }

    
    /**
     * @return the refinedSubjectLabel
     */
    public String getRefinedSubjectLabel() {
    
        return refinedSubjectLabel;
    }

    
    /**
     * @return the refinedObjectLabel
     */
    public String getRefinedObjectLabel() {
    
        return refinedObjectLabel;
    }
}
