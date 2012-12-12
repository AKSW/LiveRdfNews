package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.HashSet;
import java.util.Set;


public class DatatypePropertyTriple extends AbstractTriple {

    private String objectValue;
    private String refinedSubjectLabel;

    public DatatypePropertyTriple(String subjectLabel, String subjectUri, String patternLabel, String objectLabel, Set<Integer> ids) {

        this.subjectLabel = subjectLabel;
        this.subjectUri = subjectUri;
        this.patternLabel = patternLabel;
        this.objectValue = objectLabel;
        this.sentenceIds.addAll(ids);
    }

    @Override
    public void setObject(String object) {

        this.objectValue = object;
    }

    @Override
    public String getObject() {

        return this.objectValue;
    }

    @Override
    public String getKey() {

        return this.subjectLabel + " " + this.patternLabel + " " + this.objectValue + " " + this.sentenceIds;
    }

    public void setRefinedSubjectLabel(String refinedLabel) {

        this.refinedSubjectLabel = refinedLabel;
    }
    
    public String getRefinedSubjectLabel() {

        return this.refinedSubjectLabel;
    }

    public Set<String> getMentions() {

        return new HashSet<String>();
    }
}
