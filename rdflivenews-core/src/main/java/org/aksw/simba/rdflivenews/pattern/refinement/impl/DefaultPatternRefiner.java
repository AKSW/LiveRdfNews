/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.jena.SubclassChecker;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;
import org.aksw.simba.rdflivenews.util.ReflectionManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPatternRefiner implements PatternRefiner {
    
    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    private UriRetrieval uriRetrieval = null;
    
    public DefaultPatternRefiner() {
        
        this.uriRetrieval = (UriRetrieval) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "uriretrieval"));
    }

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern) {

        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
            pair.getFirstEntity().setUri(this.uriRetrieval.getUri(pair.getFirstEntity().getLabel()));
            // we can only find types if we have a uri from dbpedia
            if ( pair.getFirstEntity().getUri().startsWith(Constants.DBPEDIA_ONTOLOGY_PREFIX) )
                pair.getFirstEntity().setType(
                        SubclassChecker.getDeepestSubclass(luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri())));
            
            // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
            pair.getSecondEntity().setUri(this.uriRetrieval.getUri(pair.getSecondEntity().getLabel()));
            // we can only find types if we have a uri from dbpedia
            if ( pair.getSecondEntity().getUri().startsWith(Constants.DBPEDIA_ONTOLOGY_PREFIX) )
                pair.getSecondEntity().setType(
                        SubclassChecker.getDeepestSubclass(luceneRefinementManager.getTypesOfResource(pair.getSecondEntity().getUri())));
            
            // mark the pair as not no, so that we dont process it again in subsequent iterations
            pair.setNew(false);
        }
    }

    /**
     * 
     * @param types
     * @param uri
     * @param foundTypes
     * @return
     */
    private String generateFavouriteType(Map<String,Integer> types, Set<String> foundTypes) {
        
        for ( String foundType : foundTypes ) {

            if ( types.containsKey(foundType) ) types.put(foundType, types.get(foundType) + 1);
            else types.put(foundType, 1);
        }
        
        int maximum          = -1;
        String favouriteType = "";
        
        for ( Map.Entry<String, Integer> entry : types.entrySet() ) {
            
            if ( entry.getValue() > maximum ) {
                
                maximum         = entry.getValue();
                favouriteType   = entry.getKey();
            }
        }
        
        return favouriteType;
    }
    
    /**
     * 
     * @param types
     * @param label
     * @return
     */
    private String generateFavouriteType(Map<String,Integer> types, String label) {

        return this.generateFavouriteType(types, 
                        luceneRefinementManager.getTypesOfResource(luceneRefinementManager.getPossibleUri(label)));
    }

    /**
     * Refines a pattern with the help of the refine(Pattern pattern) method
     * 
     * @param patterns
     */
    public void refinePatterns(List<Pattern> patterns) {

        for ( Pattern pattern : patterns ) this.refinePattern(pattern);
    }
}
