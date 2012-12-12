/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.wordnet;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.apache.log4j.chainsaw.Main;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 *
 * @author ngonga
 */
public class JawsWordnet {

    static WordNetDatabase database;

    public JawsWordnet(String dictionary) {
        System.setProperty("wordnet.database.dir", dictionary);
        database = WordNetDatabase.getFileInstance();        
    }
    
    /** Expands a single keyword by retrieving all the elements of all its synsets
     * 
     * @param keyword Input token
     * @return All elements of all synsets of keyword
     */
    public Set<String> getSynset(String keyword) {
        Set<String> result = new HashSet<String>();
        Synset[] synsets = database.getSynsets(keyword);
        for (int i = 0; i < synsets.length; i++) {
            String[] s = synsets[i].getWordForms();
            for (int j = 0; j < s.length; j++) {
                result.add(s[j]);
            }
        }
        return result;
    }
    
    public static void main(String[] args) {

        System.out.println(RdfLiveNews.class.getClassLoader().getResource("wordnet-3.0").getFile());
        
        JawsWordnet wordnet = new JawsWordnet(RdfLiveNews.class.getClassLoader().getResource("wordnet-3.0").getFile());
        
//        's lawyer
//        , a professor at
//        , an attorney representing
//        's lawyer ,
//        professor
//        , a teacher at
//        , the attorney for
//        's attorney
//        lawyer
//        attorney
//        's attorney ,
        
        
        for (Synset synset : database.getSynsets("lawyer", SynsetType.NOUN)) {
            
            NounSynset nounSynset = (NounSynset)(synset);
            for ( NounSynset s : nounSynset.getHypernyms()) {
                
                System.out.println(s.toString());
            }
        }
    }
}
