///**
// * 
// */
//package org.aksw.simba.rdflivenews.wordnet;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import com.github.gerbsen.maven.MavenUtil;
//
//import edu.smu.tspell.wordnet.Synset;
//import edu.smu.tspell.wordnet.SynsetType;
//import edu.smu.tspell.wordnet.WordNetDatabase;
//
//
///**
// * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
// *
// */
//public class Wordnet {
//
//    private static Wordnet INSTANCE;
//    private WordNetDatabase database;
//    
//    private Wordnet() {
//        
//        System.setProperty("wordnet.database.dir", MavenUtil.loadFile("/wordnet-3.0").getAbsolutePath());
//        database = WordNetDatabase.getFileInstance();        
//    }
//    
//    public static Wordnet getInstance() {
//        
//        if ( Wordnet.INSTANCE == null ) Wordnet.INSTANCE = new Wordnet();
//        return Wordnet.INSTANCE;
//    }
//    
//    /**
//     * 
//     * @param word
//     * @return
//     */
//    public Set<Synset> getSynsets(String word){
//        
//        Set<Synset> synsets = new HashSet<>();
//        synsets.addAll(Arrays.asList(this.database.getSynsets(word)));
//        
//        return synsets;
//    }
//    
//    /**
//     * 
//     * @param word
//     * @return
//     */
//    public Set<String> getWordnetExpansion(String word) {
//        
//        return getWordnetExpansion(word, SynsetType.ALL_TYPES);
//    }
//    
//    /**
//     * 
//     * @param word
//     * @param type
//     * @return
//     */
//    public Set<String> getWordnetExpansion(String word, SynsetType ... types) {
//        
//        Set<String> wordForms = new HashSet<>();
//        
//        for ( SynsetType type : types ) 
//            for ( Synset synset : this.database.getSynsets(word, type) )
//                for (String wordForm : synset.getWordForms())
//                    wordForms.add(wordForm);
//        
//        return wordForms;
//    }
//}
