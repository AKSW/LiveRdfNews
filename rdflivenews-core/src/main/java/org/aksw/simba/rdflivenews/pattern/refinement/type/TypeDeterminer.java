/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.type;

import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface TypeDeterminer {

    /**
     * 
     * @param urisOfClass
     * @return
     */
    public String getTypeClass(Set<String> urisOfClass, DETERMINER_TYPE type);
}
