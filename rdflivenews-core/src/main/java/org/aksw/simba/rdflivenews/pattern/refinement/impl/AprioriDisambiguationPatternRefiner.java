/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.io.IOException;
import java.io.PrintStream;
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
import org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.label.impl.EntityLabelRefiner;
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

    private LuceneRefinementManager luceneRefinementManager = null;
    private UriRetrieval uriRetrieval                             = null;
    private LabelRefiner labelRefiner                             = null;
    private TypeDeterminer typer                                  = null;
    private String url;
    private String username;
    private String password;
    
    public AprioriDisambiguationPatternRefiner() {
        
        this.url = RdfLiveNews.CONFIG.getStringSetting("refiner", "url");
        this.username = RdfLiveNews.CONFIG.getStringSetting("refiner", "username");
        this.password = RdfLiveNews.CONFIG.getStringSetting("refiner", "password");
    }
    
    private void init() {
        
        this.uriRetrieval = new AprioriBasedDisambiguation(url, username, password);
        this.typer        = new DefaultTypeDeterminer();
        this.labelRefiner = new EntityLabelRefiner();
        this.luceneRefinementManager = new LuceneRefinementManager();
    }

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern) {
        
        this.init();
        
        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            if ( pair.isNew() ) {
                
                // mark the pair as not new, so that we dont process it again in subsequent iterations
                pair.setNew(false);
                
                Integer sentenceId = pair.getLuceneSentenceIds().iterator().next();
                
                String labelOne = pair.getFirstEntity().getLabel();
                String labelTwo = pair.getSecondEntity().getLabel();
                        
                if ( RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("PERSON") || 
                        RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("ALL") ) {
                    
                    labelOne = labelRefiner.refineLabel(labelOne, sentenceId);
                    labelTwo = labelRefiner.refineLabel(labelTwo, sentenceId);
                    
                    if (!pair.getFirstEntity().getLabel().equals(labelOne) )  
                        System.out.println("\tReplaced: " + pair.getFirstEntity().getLabel() + " with: " + labelOne);
                    if (!pair.getSecondEntity().getLabel().equals(labelTwo) )
                        System.out.println("\tReplaced: " + pair.getSecondEntity().getLabel() + " with: " + labelTwo);
                    
                    pair.getFirstEntity().setRefinedLabel(labelOne);
                    pair.getSecondEntity().setRefinedLabel(labelTwo);
                }
                
                // get both uris for both labels at the same time
                Map<String, String> labelToUriMapping = this.uriRetrieval.getUris("", Arrays.asList(labelOne, labelTwo));
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getFirstEntity().setUri(labelToUriMapping.get(labelOne));
                
                // we can only find types if we have a uri from dbpedia
                if (   pair.getFirstEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX) 
                    || pair.getFirstEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getFirstEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getFirstEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refiner", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUPER_CLASS) : 
                                typer.getTypeClass(types, DETERMINER_TYPE.SUB_CLASS));
                }
                
                // find a suitable uri for the given subject and get the deepest (in ontology hierachy) types of this uri
                pair.getSecondEntity().setUri(labelToUriMapping.get(labelTwo));
                
                // we can only find types if we have a uri from dbpedia
                if (    pair.getSecondEntity().getUri().startsWith(Constants.DBPEDIA_RESOURCE_PREFIX)
                   ||   pair.getSecondEntity().getUri().startsWith(Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX) ) {
                    
                    Set<String> types = luceneRefinementManager.getTypesOfResource(pair.getSecondEntity().getUri());
                    
                    if ( !types.isEmpty() )
                        pair.getSecondEntity().setType(
                                RdfLiveNews.CONFIG.getStringSetting("refiner", "typing").equals(DETERMINER_TYPE.SUPER_CLASS.toString()) ? 
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
        
//        int count = 0;
//        for ( Pattern pattern : patterns ) if ( pattern.isAboveThresholds() ) count++;
        
        System.out.println("Starting to refine " + patterns.size() + " patterns!");
        
        long startAll = System.currentTimeMillis();
        for ( Pattern pattern : patterns ) {
            
//            if ( pattern.isAboveThresholds() ) {
                
                long start = System.currentTimeMillis();
                this.refinePattern(pattern);
                System.out.println("Refining Pattern: " + pattern.getNaturalLanguageRepresentation() + " (Support: "+ pattern.getTotalOccurrence()+") took " + (System.currentTimeMillis() - start) + "ms.");
//            }
        }
        System.out.println("Refining " + patterns.size() + " patterns took: " + TimeUtil.convertMilliSeconds(System.currentTimeMillis() - startAll) + ".");
        
        luceneRefinementManager.close();
    }
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;

        List<String> entities = new ArrayList<String>(Arrays.asList("Homeland Security"));
        
        System.out.println(new AprioriBasedDisambiguation("jdbc:mysql://139.18.2.235:5555/dbrecords", "liverdf", "_L1v3Rdf_").getUris("", entities));
        System.out.println(new DefaultUriRetrieval().getUris("", entities));
    }

    /**
     * 
     * @return
     */
    public void closeLuceneRefinementManager() {

        this.luceneRefinementManager.close();
    }
}
