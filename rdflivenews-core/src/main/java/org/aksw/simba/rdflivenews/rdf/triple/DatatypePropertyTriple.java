package org.aksw.simba.rdflivenews.rdf.triple;


public class DatatypePropertyTriple extends AbstractTriple {

    private String objectValue;

    public DatatypePropertyTriple(String subjectLabel, String subjectUri, String patternLabel, String objectLabel, String id) {

        this.subjectLabel = subjectLabel;
        this.subjectUri = subjectUri;
        this.patternLabel = patternLabel;
        this.objectValue = objectLabel;
        this.sentenceIds.add(Integer.valueOf(id));
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
}
