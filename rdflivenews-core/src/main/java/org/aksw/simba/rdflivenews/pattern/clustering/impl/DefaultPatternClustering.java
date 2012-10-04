/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.clustering.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPatternClustering implements PatternClustering {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.clustering.PatternClustering#clusterPatterns(java.util.List)
     */
    @Override
    public Set<Cluster<Pattern>> clusterPatterns(Set<Similarity> similarities, Double similarityThreshold) {

        return new HashSet<Cluster<Pattern>>();
    }
}
