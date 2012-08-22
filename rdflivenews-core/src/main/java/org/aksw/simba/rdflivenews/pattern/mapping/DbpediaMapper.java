/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.mapping;

import java.util.Set;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface DbpediaMapper {

    /**
     * 
     * @param cluster
     */
    public void map(Set<Cluster<Pattern>> cluster);

}
