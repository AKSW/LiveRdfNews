/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator;

import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface SimilarityGenerator {

    public Set<Similarity> calculateSimilarities(List<Pattern> patterns);
}
