/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DisambiguationEvaluationResult implements Comparable<DisambiguationEvaluationResult> {

    private Map<String,String> config = new LinkedHashMap<String,String>();
    
    Float subjectPrecision          = 0F;
    Float objectPrecision           = 0F;
    Float subjectAndObjectPrecision = 0F;
    
    Float subjectRecall             = 0F;
    Float objectRecall              = 0F;
    Float subjectAndObjectRecall    = 0F;
    
    Float subjectUriFMeasure           = 0F;  
    Float objectUriFMeasure            = 0F;
    Float subjectAndObjectUriFMeasure  = 0F;
    
    /**
     * 
     * @param key
     * @param value
     */
    public void addConfigOption(String key, String value) {
        
        this.config.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer("Config: \n");
        for ( Map.Entry<String, String> configEntry : config.entrySet()) 
            buffer.append("\t").append(configEntry.getKey()).append(":\t").append(configEntry.getValue()).append("\n");
        
        buffer.append("\tSubject-Precision: " + this.subjectPrecision).append("\n");                   
        buffer.append("\tObject-Precision: " + this.objectPrecision).append("\n");                     
        buffer.append("\tSubject-Object-Precision: " + this.subjectAndObjectPrecision).append("\n\n");   
                                                                        
//        buffer.append("\tSubject-Recall: " + this.subjectRecall).append("\n");                         
//        buffer.append("\tObject-Recall: " + this.objectRecall).append("\n");                           
//        buffer.append("\tSubject-Object-Recall: " + this.subjectAndObjectRecall).append("\n\n");         
//                                                                        
//        buffer.append("\tSubject-F-Measure: " + this.getSubjectUriFMeasure()).append("\n");                 
//        buffer.append("\tObject-F-Measure: " + this.getObjectUriFMeasure()).append("\n");                   
//        buffer.append("\tSubject-Object-F-Measure: " + this.getSubjectAndObjectUriFMeasure()).append("\n");
        
        return buffer.toString(); 
    }

    private Float getSubjectAndObjectUriFMeasure() {

        return (2F * subjectAndObjectPrecision * subjectAndObjectRecall) / (subjectAndObjectPrecision + subjectAndObjectRecall);
    }

    private Float getObjectUriFMeasure() {

        return (2F * objectPrecision * objectRecall) / (objectPrecision + objectRecall);
    }

    private Float getSubjectUriFMeasure() {

        return (2F * subjectPrecision * subjectRecall) / (subjectPrecision + subjectRecall);
    }

    @Override
    public int compareTo(DisambiguationEvaluationResult o) {

        return -this.getSubjectAndObjectPrecision().compareTo(o.getSubjectAndObjectPrecision());
    }

    public void addConfigOption(String key, double value) {

        this.config.put(key, String.valueOf(value));
    }
    
    /**
     * @return the subjectPrecision
     */
    public Float getSubjectPrecision() {
    
        return subjectPrecision;
    }

    
    /**
     * @param subjectPrecision the subjectPrecision to set
     */
    public void setSubjectPrecision(Float subjectPrecision) {
    
        this.subjectPrecision = subjectPrecision;
    }

    
    /**
     * @return the objectPrecision
     */
    public Float getObjectPrecision() {
    
        return objectPrecision;
    }

    
    /**
     * @param objectPrecision the objectPrecision to set
     */
    public void setObjectPrecision(Float objectPrecision) {
    
        this.objectPrecision = objectPrecision;
    }

    
    /**
     * @return the subjectAndObjectPrecision
     */
    public Float getSubjectAndObjectPrecision() {
    
        return subjectAndObjectPrecision;
    }

    
    /**
     * @param subjectAndObjectPrecision the subjectAndObjectPrecision to set
     */
    public void setSubjectAndObjectPrecision(Float subjectAndObjectPrecision) {
    
        this.subjectAndObjectPrecision = subjectAndObjectPrecision;
    }

    
    /**
     * @return the subjectRecall
     */
    public Float getSubjectRecall() {
    
        return subjectRecall;
    }

    
    /**
     * @param subjectRecall the subjectRecall to set
     */
    public void setSubjectRecall(Float subjectRecall) {
    
        this.subjectRecall = subjectRecall;
    }

    
    /**
     * @return the objectRecall
     */
    public Float getObjectRecall() {
    
        return objectRecall;
    }

    
    /**
     * @param objectRecall the objectRecall to set
     */
    public void setObjectRecall(Float objectRecall) {
    
        this.objectRecall = objectRecall;
    }

    
    /**
     * @return the subjectAndObjectRecall
     */
    public Float getSubjectAndObjectRecall() {
    
        return subjectAndObjectRecall;
    }

    
    /**
     * @param subjectAndObjectRecall the subjectAndObjectRecall to set
     */
    public void setSubjectAndObjectRecall(Float subjectAndObjectRecall) {
    
        this.subjectAndObjectRecall = subjectAndObjectRecall;
    }
}
