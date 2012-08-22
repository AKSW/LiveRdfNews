/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf;

import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface RdfExtraction {

    void extractRdf(Set<Cluster<Pattern>> cluster);
}
