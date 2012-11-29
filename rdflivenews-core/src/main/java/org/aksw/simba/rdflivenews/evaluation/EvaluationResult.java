/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EvaluationResult implements Comparable<EvaluationResult> {

    private String config;
    private Double accuracy;
    private float ppv;
    private float sensitivity;
    
    public EvaluationResult(String config, float sensitivity, float positivePredictedValue, Double accuracy) {
        
        this.config = config;
        this.sensitivity = sensitivity;
        this.ppv = positivePredictedValue;
        this.accuracy = accuracy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Config: " + config + "\n\tAccuracy: " + accuracy + "\t\tPPV: " + ppv + "\t\tSensitivity: " + sensitivity;
    }

    @Override
    public int compareTo(EvaluationResult o) {

        return -this.accuracy.compareTo(o.accuracy);
    }
}
