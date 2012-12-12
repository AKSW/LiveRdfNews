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
public class ClusterEvaluationResult implements Comparable<ClusterEvaluationResult> {

    private Map<String,String> config = new LinkedHashMap<String,String>();
    private Double accuracy;
    private double ppv;
    private double sensitivity;
    private double intraClusterSimilarity;
    private double interClusterSimilarity;
    
    public ClusterEvaluationResult(double sensitivity, double positivePredictedValue, Double accuracy) {
        
        this.sensitivity = sensitivity;
        this.ppv = positivePredictedValue;
        this.accuracy = accuracy;
    }
    
    public ClusterEvaluationResult() {

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
        
        return "Config: " + buffer.toString() + "\n\tSensitivity: " + sensitivity + "\tPPV: " + ppv + "\tAccuracy: " + accuracy +
                "\n\t\tIntra-Clustersimilarity: " + intraClusterSimilarity + "\tInter-Clustersimilarity: " + interClusterSimilarity;
    }

    @Override
    public int compareTo(ClusterEvaluationResult o) {

        return -this.accuracy.compareTo(o.accuracy);
    }

    public void addConfigOption(String key, double value) {

        this.config.put(key, String.valueOf(value));
    }

    public void setSensitivity(double sensitivity) {

        this.sensitivity = sensitivity;
    }

    public void setPositivePredictedValue(double positivePredictedValue) {

        this.ppv = positivePredictedValue;
    }

    public void setAccuracy(double accuracy) {

        this.accuracy = accuracy;
    }

    public void setIntraClusterSimilarity(double intraClusterSimilarity) {

        this.intraClusterSimilarity = intraClusterSimilarity;
    }

    public void setInterClusterSimilarity(double interClusterSimilarity) {

        this.interClusterSimilarity = interClusterSimilarity;
    }
}
