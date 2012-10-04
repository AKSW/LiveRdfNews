/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.clustering;

import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface PatternClustering {

    public Set<Cluster<Pattern>> clusterPatterns(Set<Similarity> similarities, Double similarityThreshold);
}
