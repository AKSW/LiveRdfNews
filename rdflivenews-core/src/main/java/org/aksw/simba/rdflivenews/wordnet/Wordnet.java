/**
 * 
 */
package org.aksw.simba.rdflivenews.wordnet;

import cern.colt.Arrays;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.MatrixCalculator;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>, Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class Wordnet {

    private static Wordnet INSTANCE;
    
    private static ILexicalDatabase db = new NictWordNet();

    /**
     * 
     * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
     *
     */
    public enum WordnetSimilarity {
        
//        LEACOCK_CHODOROW (new LeacockChodorow(db)),
//        LEST             (new Lesk(db)),
//        RESNIK           (new Resnik(db)),  
//        JIANG_CONRATH    (new JiangConrath(db)),
    	
    	WU_PALMER        (new WuPalmer(db)), 
        LIN              (new Lin(db)), 
        PATH             (new Path(db));

        public final RelatednessCalculator relatednessCalculator;
        
        /**
         * 
         * @param rc
         */
        WordnetSimilarity(RelatednessCalculator rc) {
            this.relatednessCalculator = rc;
        }

        /**
         * 
         * @param word1
         * @param word2
         * @return
         */
        public double calcRelatednessOfWords(String word1, String word2) {

            return this.relatednessCalculator.calcRelatednessOfWords(word1, word2);
        }
    }
    
    private Wordnet() { /* singleton*/ }
    
    public static Wordnet getInstance() {
        
        if ( Wordnet.INSTANCE == null ) Wordnet.INSTANCE = new Wordnet();
        return Wordnet.INSTANCE;
    }
    
    public ILexicalDatabase getDatabase() {
        
        return db;
    }

    /**
     * 
     * @param c1
     * @param c2
     * @param similarityType
     * @return
     */
    public double getWordnetSimilarity(String word1, String word2, WordnetSimilarity similarity) {
    	    	
        WS4JConfiguration.getInstance().setMFS(true);
        if ( word1.equals(word2) ) return 1D;
        
        return similarity.calcRelatednessOfWords(word1, word2);
    }
    
    public static void main(String[] args) {

//      String[] one = new String[] {"dog","cat","company"};  
        String[] one = new String[] {"tall"};
        String[] two = new String[] {"height"};
//        String[] two = new String[] {"dog","cat","company","yellow","say", "firm", "brown"};
        
        Wordnet w = new Wordnet();
        
        WS4JConfiguration.getInstance().setMFS(true);
        for ( WordnetSimilarity rc : WordnetSimilarity.values() ) {

            long start = System.currentTimeMillis();
//          System.out.println(rc.getClass().getSimpleName() + ":" + w.getWordnetSimilarity("car", "car", rc));
//          System.out.println(rc.getClass().getSimpleName() + ":" + rc.calcRelatednessOfWords("man", "woman"));
//          System.out.println(rc.getClass().getSimpleName() + ":" + rc.calcRelatednessOfWords("tree", "company"));
            
            System.out.println(rc.relatednessCalculator.getClass().getSimpleName() + ":" + w.getWordnetSimilarity("tall", "height", rc));
            System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
            
//            for ( double[] arr : MatrixCalculator.getSimilarityMatrix(one, two, rc.relatednessCalculator)) {
//            	System.out.println(Arrays.toString(arr));
//            }
        }
    }
}
