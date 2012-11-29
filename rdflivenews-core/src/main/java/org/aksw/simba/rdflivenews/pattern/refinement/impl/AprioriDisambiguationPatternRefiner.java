/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;
import org.aksw.simba.rdflivenews.pattern.refinement.type.TypeDeterminer;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;
import org.aksw.simba.rdflivenews.rdf.uri.impl.AprioriBasedDisambiguation;
import org.aksw.simba.rdflivenews.rdf.uri.impl.DefaultUriRetrieval;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.math.Frequency;
import com.github.gerbsen.time.TimeUtil;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class AprioriDisambiguationPatternRefiner implements PatternRefiner {

    private final LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager();
    private UriRetrieval uriRetrieval                             = null;
    private TypeDeterminer typer                                  = null;
    
    public AprioriDisambiguationPatternRefiner() {
        
        String url = RdfLiveNews.CONFIG.getStringSetting("refiner", "url");
        String username = RdfLiveNews.CONFIG.getStringSetting("refiner", "username");
        String password = RdfLiveNews.CONFIG.getStringSetting("refiner", "password");
        
        this.uriRetrieval = new AprioriBasedDisambiguation(url, username, password);
        this.typer        = new DefaultTypeDeterminer();
    }

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern) {
        
        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            if ( pair.isNew() ) {
                
                // mark the pair as not new, so that we dont process it again in subsequent iterations
                pair.setNew(false);
                
                // get both uris for both labels at the same time
                List<String> entityLabels = Arrays.asList(pair.getFirstEntity().getLabel(), pair.getSecondEntity().getLabel());
                Map<String, String> labelToUriMapping = this.uriRetrieval.getUris(
                        IndexManager.getInstance().getStringValueFromDocument(pair.getLuceneSentenceIds().iterator().next(), Constants.LUCENE_FIELD_TEXT),
                        entityLabels);
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getFirstEntity().setUri(labelToUriMapping.get(pair.getFirstEntity().getLabel()));
                
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
                pair.getSecondEntity().setUri(labelToUriMapping.get(pair.getSecondEntity().getLabel()));
                
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
        
        long startAll = System.currentTimeMillis();
        for ( Pattern pattern : patterns ) {
            
            long start = System.currentTimeMillis();
            if ( pattern.isAboveThresholds() ) this.refinePattern(pattern);
            System.out.println("Refining Pattern: " + pattern.getNaturalLanguageRepresentation() + " (Support: "+ pattern.getLearnedFromEntities().size()+") took " + (System.currentTimeMillis() - start) + "ms.");
        }
        System.out.println("Refining " + patterns.size() + " patterns took: " + TimeUtil.convertMilliSeconds(System.currentTimeMillis() - startAll) + ".");
        
        luceneRefinementManager.close();
    }
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;

        List<String> entities = new ArrayList<String>(Arrays.asList("Chad Hurley", "Vishal Sikka", "Vivek Wadhwa"));
        String test = "Other guests at the swanky soiree, staffed by legions of waiters, bartenders and valets, included the Siebel Systems founder, YouTube founder Hurley, SAP Chief Technology Officer Vishal Sikka, and the academic Vivek Wadhwa .";
        
        System.out.println(new AprioriBasedDisambiguation("jdbc:mysql://139.18.2.235:5555/dbrecords", "liverdf", "_L1v3Rdf_").getUris(test, entities));
        System.out.println(new DefaultUriRetrieval().getUris(test, entities));
    }
}
