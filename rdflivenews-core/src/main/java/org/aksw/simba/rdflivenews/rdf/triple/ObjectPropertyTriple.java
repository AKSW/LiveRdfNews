package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.Set;

import edu.stanford.nlp.util.StringUtils;


public class ObjectPropertyTriple extends AbstractTriple {

    private String objectUri;
    private String objectLabel;
    
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
    
    public String getObjectLabel() {

        return this.objectLabel;
    }
    @Override
    public String getKey() {

        return this.subjectLabel + " " + this.patternLabel + " " + this.objectLabel + " " + StringUtils.join(this.sentenceIds, ",");
    }
}
