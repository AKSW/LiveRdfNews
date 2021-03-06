/**
 * 
 */
package org.aksw.simba.rdflivenews.cluster.labeling;

import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface ClusterLabeler {

    public void labelCluster(Set<Cluster<Pattern>> cluster);
}
