/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.util.ArrayList;
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

import com.github.gerbsen.math.Frequency;


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
                
                // mark the pair as not no, so that we dont process it again in subsequent iterations
                pair.setNew(false);

                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getFirstEntity().setUri(this.uriRetrieval.getUri(pair.getFirstEntity().getLabel()));
                
                // we can only find types if we have a uri from dbpedia
                if (   pair.getFirstEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX) 
                    || pair.getFirstEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getFirstEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refinement", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUPER_CLASS) : 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUB_CLASS));
                }
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getSecondEntity().setUri(this.uriRetrieval.getUri(pair.getSecondEntity().getLabel()));
                
                // we can only find types if we have a uri from dbpedia
                if (    pair.getSecondEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX)
                   ||   pair.getSecondEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getSecondEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getSecondEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refinement", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUPER_CLASS) : 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUB_CLASS));
                }
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
        
        String type = Constants.OWL_THING;
        
        if ( !foundTypes.isEmpty() ) {
            
            Frequency frequency = new Frequency();
            for ( String foundType : foundTypes ) frequency.addValue(foundType);
            type = (String) frequency.sortByValue().get(0).getKey();
        }
        
        return type;
    }
    
    /**
     * Refines a pattern with the help of the refine(Pattern pattern) method
     * 
     * @param patterns
     */
    public void refinePatterns(List<Pattern> patterns) {

        for ( Pattern pattern : patterns ) this.refinePattern(pattern);
        this.luceneRefinementManager.close();
    }
}
