/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.nlp.ner.NamedEntityTagNormalizer;
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
            
            if ( pair.isNew() ) {

                System.out.println(pair);
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getFirstEntity().setUri(this.uriRetrieval.getUri(pair.getFirstEntity().getLabel()));
                // we can only find types if we have a uri from dbpedia
                if ( pair.getFirstEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX) )
                    pair.getFirstEntity().setType(
                            SubclassChecker.getDeepestSubclass(luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri())));
                
                System.out.println(pair);
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getSecondEntity().setUri(this.uriRetrieval.getUri(pair.getSecondEntity().getLabel()));
                // we can only find types if we have a uri from dbpedia
                if ( pair.getSecondEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX) )
                    pair.getSecondEntity().setType(
                            SubclassChecker.getDeepestSubclass(luceneRefinementManager.getTypesOfResource(pair.getSecondEntity().getUri())));
                
                System.out.println(pair);
                
                System.out.println("\n\n");
                
                // mark the pair as not no, so that we dont process it again in subsequent iterations
                pair.setNew(false);
            }
        }
        
        pattern.setFavouriteTypeFirstEntity(generateFavouriteType(pattern.getTypesFirstEntity()));
        pattern.setFavouriteTypeSecondEntity(generateFavouriteType(pattern.getTypesSecondEntity()));
    }

    /**
     * 
     * @param types
     * @param uri
     * @param foundTypes
     * @return
     */
    private String generateFavouriteType(List<String> foundTypes) {
        
        Map<String,Integer> types = new HashMap<String,Integer>();
        
        //  add them all to the list
        for ( String foundType : foundTypes ) {

            if ( types.containsKey(foundType) ) types.put(foundType, types.get(foundType) + 1);
            else types.put(foundType, 1);
        }
        
        // find the maximum
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
     * Refines a pattern with the help of the refine(Pattern pattern) method
     * 
     * @param patterns
     */
    public void refinePatterns(List<Pattern> patterns) {

        int i = 0;
        for ( Pattern pattern : patterns ) {
            
            System.out.println("refining pattern " + i++ + " " + pattern.getNaturalLanguageRepresentation());
            this.refinePattern(pattern);
        }
    }
}
