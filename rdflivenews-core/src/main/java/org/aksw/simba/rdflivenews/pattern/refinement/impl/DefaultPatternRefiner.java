/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.PatternRefiner;
import org.aksw.simba.rdflivenews.pattern.refinement.lucene.LuceneRefinementManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPatternRefiner implements PatternRefiner {
    
    private LuceneRefinementManager luceneRefinementManager = new LuceneRefinementManager(); 

    /**
     * 
     * @param pattern
     */
    public void refinePattern(Pattern pattern) {

        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            pattern.setFavouriteTypeFirstEntity(this.generateFavouriteType(pattern.getTypesFirstEntity(), pair.getFirstEntity().getLabel()));
            pattern.setFavouriteTypeSecondEntity(this.generateFavouriteType(pattern.getTypesSecondEntity(), pair.getSecondEntity().getLabel()));
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
