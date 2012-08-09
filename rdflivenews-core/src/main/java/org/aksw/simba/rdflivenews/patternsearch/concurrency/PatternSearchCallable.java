/**
 * 
 */
package org.aksw.simba.rdflivenews.patternsearch.concurrency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.crawler.NewsCrawler;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.patternsearch.PatternSearcher;
import org.aksw.simba.rdflivenews.patternsearch.impl.NamedEntityTagPatternSearcher;
import org.aksw.simba.rdflivenews.patternsearch.impl.PartOfSpeechTagPatternSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class PatternSearchCallable implements Callable<List<Pattern>> {

    private int progress                     = 0;
    private String name                      = null;
    private List<Integer> luceneDocumentsIds = null;
    private List<Pattern> foundPatterns      = null;
    
    // specific for different search methods
    private String luceneFieldName          = "";
    private PatternSearcher patternSearcher = null;
    
    /**
     * Creates a search threads which uses the pattern search method 
     * configured in the config.ini ([search].method)
     * 
     * @param luceneDocumentsIdsSubList
     */
    public PatternSearchCallable(List<Integer> luceneDocumentsIdsSubList, String name) {

        this.foundPatterns      = new ArrayList<Pattern>();
        this.luceneDocumentsIds = luceneDocumentsIdsSubList;
        this.name               = name;
        
        if ( NewsCrawler.CONFIG.getStringSetting("search", "method").equals("POS") ) {
            
            this.luceneFieldName = Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE;
            this.patternSearcher = new PartOfSpeechTagPatternSearcher();
        }
        else if ( NewsCrawler.CONFIG.getStringSetting("search", "method").equals("NER") ) {
            
            this.luceneFieldName = Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE;
            this.patternSearcher = new NamedEntityTagPatternSearcher();
        }
        else throw new RuntimeException(String.format("Supplied pattern search method '%s' not supported!", NewsCrawler.CONFIG.getStringSetting("search", "method")));
    }

    /**
     * 
     */
    public List<Pattern> call() throws Exception {

        IndexReader reader = IndexReader.open(IndexManager.INDEX);
        
        // go through all sentence ids
        for ( Integer luceneDocumentId : this.luceneDocumentsIds ) {
            
            // get the sentence from the index and try to extract patterns from it
            String taggedSentence = reader.document(luceneDocumentId).get(this.luceneFieldName);
            this.foundPatterns.addAll(this.patternSearcher.extractPatterns(taggedSentence,luceneDocumentId));
            
            this.progress++;
        }
        // finished extracting patterns so close everything
        reader.close();
        // and return what we found
        return this.foundPatterns;
    }
    
    /* ########################################################### */
    /* ################# Statistics and Monitoring ############### */
    /* ########################################################### */
    
    /**
     * @return the number of sentences this threads needs to process
     */
    public int getNumberTotal() {

        return this.luceneDocumentsIds.size();
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

        return (double) this.progress / this.luceneDocumentsIds.size();
    }

    /**
     * @return the name of this pattern searcher
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return the number of patterns the thread found so far
     */
    public int getNumberOfResultsSoFar() {

        return this.foundPatterns.size();
    }

}
