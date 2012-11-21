/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.clustering.merging;

import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface ClusterMerger {

    /**
     * 
     * @param cluster
     */
    public void mergeCluster(Set<Cluster<Pattern>> clusters);
}
