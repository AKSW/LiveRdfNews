/**
 * 
 */
package org.aksw.simba.rdflivenews.wordnet;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>, Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Wordnet {

    private static Wordnet INSTANCE;
    
    private static ILexicalDatabase db = new NictWordNet();
    private static RelatednessCalculator[] rcs = {
                    new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
                    new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
                    };
    public static final int HSO_SIMILARITY = 0;
    public static final int LCH_SIMILARITY = 1;
    public static final int LESK_SIMILARITY = 2;
    public static final int WUP_SIMILARITY = 3;
    public static final int RES_SIMILARITY = 4;
    public static final int JCN_SIMILARITY = 5;
    public static final int LIN_SIMILARITY = 6;
    public static final int PATH_SIMILARITY = 7;
    
    private Wordnet() {
        
    }
    
    public static Wordnet getInstance() {
        
        if ( Wordnet.INSTANCE == null ) Wordnet.INSTANCE = new Wordnet();
        return Wordnet.INSTANCE;
    }

    /**
     * 
     * @param c1
     * @param c2
     * @param similarityType
     * @return
     */
    public double getWordnetSimilarity(String word1, String word2, int similarityType) {
    	    	
        WS4JConfiguration.getInstance().setMFS(true);
        
        if(similarityType < 0 || similarityType > 7)
        	return 0.0;
        
        RelatednessCalculator rc = rcs[similarityType];
        
        return rc.calcRelatednessOfWords(word1, word2);
    }
    
}
