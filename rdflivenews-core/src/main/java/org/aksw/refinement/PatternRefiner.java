/**
 * 
 */
package org.aksw.refinement;

import java.util.Map;

import org.aksw.pair.EntityPair;
import org.aksw.pattern.Pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternRefiner {

    public void refine(Pattern pattern) {

        for ( EntityPair pair : pattern.getLearnedFromEntities() ) {
            
            pattern.setFavouriteTypeFirstEntity(this.generateFavouriteType(pattern.getTypesFirstEntity(), pair.getFirstEntity().getLabel()));
            pattern.setFavouriteTypeSecondEntity(this.generateFavouriteType(pattern.getTypesSecondEntity(), pair.getSecondEntity().getLabel()));
            pair.setNew(false);
        }
    }

    private String generateFavouriteType(Map<String,Integer> types, String label) {

        // TODO Auto-generated method stub
        return null;
    }
}
