/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.pattern.linking;

import de.uni_leipzig.simba.data.Mapping;
import java.util.Set;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;

/**
 *
 * @author ngonga
 */
public interface Linker {
    /** Retrieves properties used in a given endpoint and returns mapping between 
     * these properties and the patterns
     * 
     */
    public Mapping link(Set<Cluster<Pattern>> clusters, double threshold);
}
