package org.aksw.simba.rdflivenews.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.aksw.simba.rdflivenews.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.aksw.simba.rdflivenews.pattern.search.impl.PartOfSpeechTagPatternSearcher;

import de.jetwick.snacktory.HtmlFetcher;



public class ArticleTextPreprocessing {

    public static void main(String[] args) throws Exception {

        HtmlFetcher fetcher         = new HtmlFetcher();
        String text = fetcher.fetchAndExtract("http://www.abc24.com/news/local/story/Drought-Taking-a-Toll-On-Mid-South-Fish/uDZFt5elJEa9OIywFu8NRA.cspx", 10000, true).getText();
        List<String> parsedSentences = StanfordNLPSentenceBoundaryDisambiguation.getSentences(text);
        StanfordNLPPartOfSpeechTagger pos = new StanfordNLPPartOfSpeechTagger();
        PartOfSpeechTagPatternSearcher searcher = new PartOfSpeechTagPatternSearcher();
        Map<String,List<String>> namesToTypes = new HashMap<String,List<String>>();
        
        Set<String> entities = new HashSet<String>();
        for ( String s : parsedSentences) {
            
            String postagged = pos.getAnnotatedSentence(s);
            entities.addAll(getEntities(searcher.mergeTagsInSentences(postagged)));
        }
        
        Set<String> newList = new HashSet<String>();
        
        for ( String entity1 : entities) {
            
            boolean entity1WasSubString = false; 
            
            for ( String entity2 : entities ) {
                
                if ( !entity1.equals(entity2) ) {

                    if ( entity2.contains(entity1) ) {
                        
                        System.out.println("\tNEW " + entity1 + " " + entity2);
                        entity1WasSubString = true;
                        newList.add(entity2);
                    }
                }
            }
            
            if ( !entity1WasSubString ) newList.add(entity1);
        }
        for ( String entity : entities ) {
            System.out.println(entity);
        }
        System.out.println();
        System.out.println();
        
        for ( String entity : newList ) {
            System.out.println(entity);
        }
        
        
//for ( Map.Entry<String, List<String>> entry : namesToTypes.entrySet()) {
//            
//            System.out.println(entry.getKey() + " -- " + entry.getValue());
//        }
    }
    
    private static List<String> getEntities(List<String> listMergedEntities){
        
        List<String> entities = new ArrayList<String>();
        for (String entity :  listMergedEntities) {
            
            if ( entity.endsWith("_NNP") ) entities.add(entity.replace("_NNP", ""));
        }
        return entities;
    }
}
