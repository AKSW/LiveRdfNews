/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator;

import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface SimilarityGenerator {

    public Set<Similarity> calculateSimilarities();
}
