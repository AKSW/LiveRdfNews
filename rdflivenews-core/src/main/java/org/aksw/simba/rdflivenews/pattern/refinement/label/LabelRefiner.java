/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.label;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface LabelRefiner {

    String refineLabel(String label, Integer sentenceId);
}
