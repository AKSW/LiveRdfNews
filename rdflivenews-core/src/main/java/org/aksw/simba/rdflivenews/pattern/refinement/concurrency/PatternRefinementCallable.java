/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.concurrency;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.impl.AprioriDisambiguationPatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.label.impl.EntityLabelRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.TypeDeterminer;
import org.aksw.simba.rdflivenews.pattern.refinement.type.DefaultTypeDeterminer.DETERMINER_TYPE;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;
import org.aksw.simba.rdflivenews.rdf.uri.impl.AprioriBasedDisambiguation;

import com.github.gerbsen.math.Frequency;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternRefinementCallable implements Callable<Pattern> {

    private Pattern pattern;
    private String name;
    private LuceneRefinementManager luceneRefinementManager = null;
    private UriRetrieval uriRetrieval                       = null;
    private LabelRefiner labelRefiner                       = null;
    private TypeDeterminer typer                            = null;
    private String url;
    private String username;
    private String password;
    private int progress = 0;

    public PatternRefinementCallable(Pattern pattern, String name) {

        this.pattern = pattern;
        this.name = name;
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
    
    private void destroy() {
        
        try {
            ((AprioriBasedDisambiguation)this.uriRetrieval).con.close();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.uriRetrieval = null;
        this.typer = null;
        this.labelRefiner = null;
        this.luceneRefinementManager.close();
        this.luceneRefinementManager = null;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Pattern call() throws Exception {

        this.init();
        
        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            if ( pair.isNew() ) {
                
                // mark the pair as not new, so that we dont process it again in subsequent iterations
                pair.setNew(false);
                this.progress++;
                
                Integer sentenceId = pair.getLuceneSentenceIds().iterator().next();
                
                String labelOne = pair.getFirstEntity().getLabel();
                String labelTwo = pair.getSecondEntity().getLabel();
                        
                if ( RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("PERSON") || 
                        RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("ALL") ) {
                    
                    labelOne = labelRefiner.refineLabel(labelOne, sentenceId);
                    labelTwo = labelRefiner.refineLabel(labelTwo, sentenceId);
                    
//                    if (!pair.getFirstEntity().getLabel().equals(labelOne) )  
//                        System.out.println("\tReplaced: " + pair.getFirstEntity().getLabel() + " with: " + labelOne);
//                    if (!pair.getSecondEntity().getLabel().equals(labelTwo) )
//                        System.out.println("\tReplaced: " + pair.getSecondEntity().getLabel() + " with: " + labelTwo);
                    
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
        destroy();
        
        pattern.setFavouriteTypeFirstEntity(generateFavouriteType(pattern.getTypesFirstEntity()));
        pattern.setFavouriteTypeSecondEntity(generateFavouriteType(pattern.getTypesSecondEntity()));
        
        return this.pattern;
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
    
    /* ########################################################### */
    /* ################# Statistics and Monitoring ############### */
    /* ########################################################### */
    
    /**
     * @return the number of sentences this threads needs to process
     */
    public int getNumberTotal() {
        
        int count = 0; 
        for (EntityPair pair : this.pattern.getLearnedFromEntities()) if ( pair.isNew() ) count++;
        return count;
    }

    /**
     * @return how many sentence have been processed already
     */
    public int getNumberDone() {
        
        return this.progress;
    }

    /**
     * @return the progress as a value between 0 and 1
     */
    public double getProgress() {
        
        double progress = (double) this.progress / getNumberTotal();
        return Double.isNaN(progress) || Double.isInfinite(progress) ? 0 : progress;
    }

    /**
     * @return the name of this pattern searcher
     */
    public String getName() {

        return this.name;
    }
}
