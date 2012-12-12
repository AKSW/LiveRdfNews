/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.label.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner;
import org.aksw.simba.rdflivenews.pattern.search.impl.NamedEntityTagPatternSearcher;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EntityLabelRefiner implements LabelRefiner {

    /* (non-Javadoc)
     * @see org.aksw.simba.rdflivenews.pattern.refinement.label.LabelRefiner#refineLabel(java.lang.String, java.lang.Integer)
     */
    @Override
    public String refineLabel(String label, Integer sentenceId) {
        
        // grab the article url for the current sentence and get all other sentences ner tagged
        String url = IndexManager.getInstance().getStringValueFromDocument(sentenceId, Constants.LUCENE_FIELD_URL);
        Set<String> nerTaggedSentences = IndexManager.getInstance().getAllSentenceIdsFromArticle(url);
        if ( nerTaggedSentences.isEmpty() ) System.err.println("We did not find any NER tagged sentences for this article: " + url);
        
        Set<String> persons = new HashSet<String>();
        NamedEntityTagPatternSearcher searcher = new NamedEntityTagPatternSearcher();
        
        for ( String taggedSentence : nerTaggedSentences) persons.addAll(getEntities(searcher.mergeTagsInSentences(taggedSentence)));
        
        return this.findLongestMatch(label, persons);
    }
    
    /**
     * 
     * @param label
     * @param persons
     * @return
     */
    private String findLongestMatch(String label, Set<String> persons) {

        String match = label;
        
        for ( String personOne : persons) 
            for ( String personTwo : persons ) // compare ever "person" with each other
                if ( !personOne.equals(personTwo) ) // avoid comparing the same "person"
                    if ( personTwo.contains(personOne) && personTwo.contains(label) && personTwo.contains(" ") ) // make sure we dont just add a suffix
                        // personTwo is a super-string of personOne and contains the label we search
                        match = personTwo;
        
        // Hong Kong with: Hong Kongers
        
        match = match.replaceAll("`", "");
        for ( String word : Arrays.asList("Col.", "Army General", "Army Gen.", "Sen.", "Sgt.", "Lt.", "the ", "The ", "Dr.")) match = match.replace(word, "");
        // match.replace("'s", "s");
        
        // fail safe
        return match.trim();
    }

    /**
     * 
     * @param mergedTaggedSentence
     * @return
     */
    private List<String> getEntities(List<String> mergedTaggedSentence){
        
        List<String> entities = new ArrayList<String>();
        for (String entity :  mergedTaggedSentence) {

            if ( RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("ALL") ) {
                
                if (entity.endsWith("_PERSON") ) entities.add(entity.replace("_PERSON", ""));
                if (entity.endsWith("_MISC")) entities.add(entity.replace("_MISC", ""));
                if (entity.endsWith("_PLACE")) entities.add(entity.replace("_PLACE", ""));
                if (entity.endsWith("_ORGANIZATION")) entities.add(entity.replace("_ORGANIZATION", ""));
            }
            else if (RdfLiveNews.CONFIG.getStringSetting("refiner", "refineLabel").equals("PERSON")) {
                
                if (entity.endsWith("_PERSON") ) entities.add(entity.replace("_PERSON", ""));
            }
        }
        
        return entities;
    }
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        
//        Hernandez pitched Seattle Mariners 49016
        LabelRefiner refiner = new EntityLabelRefiner();
        System.out.println(refiner.refineLabel("Hernandez", 49016));
    }
}
