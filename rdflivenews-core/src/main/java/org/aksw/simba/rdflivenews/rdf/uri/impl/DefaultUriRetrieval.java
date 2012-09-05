/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultUriRetrieval implements UriRetrieval {

    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    
    @Override
    public String getUri(String label) {

        return luceneRefinementManager.getPossibleUri(label);
    }
}
