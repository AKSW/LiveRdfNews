package org.aksw.simba.rdflivenews.rdf.triple;

import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;

import edu.stanford.nlp.util.StringUtils;


public class ObjectPropertyTriple extends AbstractTriple {

    private String objectUri;
    private String objectLabel;
    private String refinedSubjectLabel;
    private String refinedObjectLabel;
    public Cluster<Pattern> cluster;
    
    public ObjectPropertyTriple(String subjectLabel, String subjectUri, String patternLabel, String objectLabel, String objectUri, Set<Integer> id) {

//    	try {
    		
    		this.subjectUri = subjectUri;//URLDecoder.decode(subjectUri, "UTF-8");
    		this.objectUri = objectUri;//URLDecoder.decode(objectUri, "UTF-8");
//		} 
//    	catch (UnsupportedEncodingException e) {
//			 TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
        this.subjectLabel = subjectLabel;
        this.patternLabel = patternLabel;
        this.objectLabel = objectLabel;
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
