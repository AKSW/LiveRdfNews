/**
 * 
 */
package org.aksw.simba.rdflivenews.nlp.impl;

import java.io.IOException;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.nlp.NaturalLanguageTagger;
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;

import com.github.gerbsen.lucene.LuceneManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class NamedEntityAndOrPartOfSpeechNaturalLanguageTagger implements NaturalLanguageTagger {

    /**
     * 
     */
    @Override 
    public void annotateSentencesInIndex(Set<Integer> newFoundNonDuplicateIds) {

        StanfordNLPNamedEntityRecognition nerTagger = new StanfordNLPNamedEntityRecognition();
        StanfordNLPPartOfSpeechTagger posTagger = new StanfordNLPPartOfSpeechTagger();
        
        IndexWriter writer      = LuceneManager.openIndexWriterAppend(IndexManager.INDEX);
        IndexSearcher searcher  = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        
        // TODO remove the document from ids if the text does not contain NNP or PERSON/PLACE/ORG
        int i = 0;
        for ( Integer sentenceId : newFoundNonDuplicateIds ) {
        	
        	if ( i++ % 100 == 0 )System.out.println(i);

            Document oldDoc = IndexManager.getInstance().getDocumentByQuery(searcher, NumericRangeQuery.newIntRange(Constants.LUCENE_FIELD_ID, sentenceId, sentenceId, true, true));
            String text     = oldDoc.get(Constants.LUCENE_FIELD_TEXT);
            
            Document newDoc = new Document();
            newDoc.add(new IntField(Constants.LUCENE_FIELD_ID, Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID)), IntField.TYPE_STORED));
            newDoc.add(new LongField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)), LongField.TYPE_STORED));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_TIME_SLICE, Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)), IntField.TYPE_STORED));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE)), IntField.TYPE_STORED));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            
            String posTagged = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
            String nerTagged = oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE);
            
            // we dont need to update 
//            if ( posTagged != null && !posTagged.isEmpty() && nerTagged != null && !nerTagged.isEmpty() ) continue;
            
            // if we already have a ner or pos tagged sentence we dont need to tag it again
            if ( posTagged == null || posTagged.isEmpty() )
                newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, posTagger.getAnnotatedSentence(text), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            else 
                newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, posTagged, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            if ( nerTagged == null || nerTagged.isEmpty() ) 
                newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, nerTagger.getAnnotatedSentence(text), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            else 
                newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, nerTagged, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            try {
            	
				writer.tryDeleteDocument(searcher.getIndexReader(), sentenceId);
				writer.addDocument(newDoc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
//            LuceneManager.updateDocument(writer, new Term(Constants.LUCENE_FIELD_ID, new IntField(Constants.LUCENE_FIELD_ID, sentenceId, Store.NO).stringValue()), newDoc);
        }
        LuceneManager.closeIndexWriter(writer);
        LuceneManager.closeIndexSearcher(searcher);
    }
}
