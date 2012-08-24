/**
 * 
 */
package org.aksw.simba.rdflivenews.nlp.impl;

import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.aksw.simba.rdflivenews.nlp.NaturalLanguageTagger;
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.NumericUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NamedEntityAndPartOfSpeechNaturalLanguageTagger implements NaturalLanguageTagger {

    public void annotateSentencesInIndex(Set<Integer> newFoundNonDuplicateIds) {

        StanfordNLPNamedEntityRecognition nerTagger = new StanfordNLPNamedEntityRecognition();
        StanfordNLPPartOfSpeechTagger posTagger     = new StanfordNLPPartOfSpeechTagger();
        
        IndexWriter writer      = LuceneManager.openIndexWriterAppend(IndexManager.INDEX);
        IndexSearcher searcher  = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        
        // TODO remove the document from ids if the text does not contain NNP or PERSON/PLACE/ORG 
        
        for ( Integer sentenceId : newFoundNonDuplicateIds ) {

            Document oldDoc = IndexManager.getInstance().getDocumentById(searcher, new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(sentenceId))));
            String text     = oldDoc.get(Constants.LUCENE_FIELD_TEXT);
            
            Document newDoc = new Document();
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE))));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, posTagger.getAnnotatedSentence(text), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, nerTagger.getAnnotatedSentence(text), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            LuceneManager.updateDocument(writer, new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(sentenceId)), newDoc);
        }
        
        LuceneManager.closeIndexWriter(writer);
        LuceneManager.closeIndexSearcher(searcher);
    }
}
