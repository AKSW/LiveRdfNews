/**
 * 
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultUriRetrieval implements UriRetrieval {

    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    
    @Override
    public Map<String, String> getUris(String text, List<String> labels) {
        
        Map<String, String> labelsToUris = new HashMap<String, String>();
        for ( String label : labels ) labelsToUris.put(label, this.luceneRefinementManager.getPossibleUri(label));
        
        return labelsToUris;
    }
    
    @Override
    public String getUri(String label) {

        return luceneRefinementManager.getPossibleUri(label);
    }
}
