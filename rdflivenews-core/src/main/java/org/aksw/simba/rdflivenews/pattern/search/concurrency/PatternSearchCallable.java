/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.search.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.search.PatternSearcher;
import org.aksw.simba.rdflivenews.pattern.search.impl.NamedEntityTagPatternSearcher;
import org.aksw.simba.rdflivenews.pattern.search.impl.PartOfSpeechTagPatternSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.NumericUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class PatternSearchCallable implements Callable<List<Pattern>> {

    private int progress                     = 0;
    private String name                      = null;
    private List<Integer> sentenceIds = null;
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
        this.sentenceIds = luceneDocumentsIdsSubList;
        this.name               = name;
        
        if ( RdfLiveNews.CONFIG.getStringSetting("search", "method").equals("POS") ) {
            
            this.luceneFieldName = Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE;
            this.patternSearcher = new PartOfSpeechTagPatternSearcher();
        }
        else if ( RdfLiveNews.CONFIG.getStringSetting("search", "method").equals("NER") ) {
            
            this.luceneFieldName = Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE;
            this.patternSearcher = new NamedEntityTagPatternSearcher();
        }
        else throw new RuntimeException(String.format("Supplied pattern search method '%s' not supported!", RdfLiveNews.CONFIG.getStringSetting("search", "method")));
    }

    /**
     * 
     */
    public List<Pattern> call() throws Exception {

        IndexSearcher searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        
        // go through all sentence ids
        for ( Integer sentenceId : this.sentenceIds ) {
            
            // get the sentence from the index and try to extract patterns from it
            Document document = IndexManager.getInstance().getDocumentById(searcher, new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(sentenceId))));
            String taggedSentence = document.get(this.luceneFieldName);
            System.out.println(document);
            System.out.println(taggedSentence);
            this.foundPatterns.addAll(this.patternSearcher.extractPatterns(taggedSentence,sentenceId));
            
            this.progress++;
        }
        // finished extracting patterns so close everything
        LuceneManager.closeIndexSearcher(searcher);
        
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

        return this.sentenceIds.size();
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

        return (double) this.progress / this.sentenceIds.size();
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
