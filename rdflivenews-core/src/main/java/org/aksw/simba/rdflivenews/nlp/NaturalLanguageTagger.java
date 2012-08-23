/**
 * 
 */
package org.aksw.simba.rdflivenews.nlp;

import java.util.Set;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface NaturalLanguageTagger {

    public void annotateSentencesInIndex(Set<Integer> newFoundNonDuplicateIds);
}
