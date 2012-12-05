/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EvaluationResult implements Comparable<EvaluationResult> {

    private Map<String,String> config = new LinkedHashMap<String,String>();
    private Double accuracy;
    private float ppv;
    private float sensitivity;
    
    public EvaluationResult(float sensitivity, float positivePredictedValue, Double accuracy) {
        
        this.sensitivity = sensitivity;
        this.ppv = positivePredictedValue;
        this.accuracy = accuracy;
    }
    
    public EvaluationResult() {

        // TODO Auto-generated constructor stub
    }

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
            buffer.append("\t").append(configEntry.getKey()).append(":\t\t").append(configEntry.getValue()).append("\n");
        
        return "Config: " + buffer.toString() + "\n\tAccuracy: " + accuracy + "\tPPV: " + ppv + "\tSensitivity: " + sensitivity;
    }

    @Override
    public int compareTo(EvaluationResult o) {

        return -this.accuracy.compareTo(o.accuracy);
    }

    public void addConfigOption(String key, double value) {

        this.config.put(key, String.valueOf(value));
    }

    public void setSensitivity(float sensitivity) {

        this.sensitivity = sensitivity;
    }

    public void setPositivePredictedValue(float positivePredictedValue) {

        this.ppv = positivePredictedValue;
    }

    public void setAccuracy(double accuracy) {

        this.accuracy = accuracy;
    }
}
