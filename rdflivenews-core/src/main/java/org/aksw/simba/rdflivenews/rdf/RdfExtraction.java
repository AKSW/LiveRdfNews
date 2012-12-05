/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf;

import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface RdfExtraction {

    List<Triple> extractRdf(Set<Cluster<Pattern>> cluster);
}
