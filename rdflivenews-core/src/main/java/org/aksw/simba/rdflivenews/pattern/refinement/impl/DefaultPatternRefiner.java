/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.TypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;
import org.aksw.simba.rdflivenews.util.ReflectionManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPatternRefiner implements PatternRefiner {
    
    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    private UriRetrieval uriRetrieval                             = null;
    private TypeDeterminer typer                                  = null;
    
    public DefaultPatternRefiner() {
        
        this.uriRetrieval = (UriRetrieval) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "uriretrieval"));
        this.typer        = new DefaultTypeDeterminer();
    }

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern) {
        
        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            if ( pair.isNew() ) {

//                System.out.println(pair);
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getFirstEntity().setUri(this.uriRetrieval.getUri(pair.getFirstEntity().getLabel()));
                
//                System.out.println(pair);
//                System.out.println(pair.getFirstEntity().getUri());
//                System.out.println(luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri()));
                
                // we can only find types if we have a uri from dbpedia
                if (   pair.getFirstEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX) 
                    || pair.getFirstEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getFirstEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refinement", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUPER_CLASS) : 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUB_CLASS));
                    
                    else pair.getFirstEntity().setType(Constants.OWL_THING);
                }
                    
                
//                System.out.println(pair);
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getSecondEntity().setUri(this.uriRetrieval.getUri(pair.getSecondEntity().getLabel()));
                
//                System.out.println(pair);
                
                // we can only find types if we have a uri from dbpedia
                if (    pair.getSecondEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX)
                   ||   pair.getSecondEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getSecondEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getSecondEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refinement", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUPER_CLASS) : 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUB_CLASS));
                    
                    else pair.getSecondEntity().setType(Constants.OWL_THING);
                }
                
//                System.out.println(pair);
                
//                System.out.println("\n\n");
                
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
            
            if ( !foundType.equals(Constants.OWL_THING) ) {

                if ( types.containsKey(foundType) ) types.put(foundType, types.get(foundType) + 1);
                else types.put(foundType, 1);
            }
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
            
            if ( pattern.getLearnedFromEntities().size() >= RdfLiveNews.CONFIG.getIntegerSetting("scoring", "occurrenceThreshold")) {

//                System.out.println("refining pattern " + i++ + " " + pattern.getNaturalLanguageRepresentation());
                this.refinePattern(pattern);
            }
        }
        
        this.luceneRefinementManager.close();
    }
}
