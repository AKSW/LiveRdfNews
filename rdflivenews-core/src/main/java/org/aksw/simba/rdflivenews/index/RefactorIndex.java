/**
 * 
 */
package org.aksw.simba.rdflivenews.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.lucene.LuceneManager;
import com.github.gerbsen.math.Frequency;
import com.github.gerbsen.math.MathUtil;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RefactorIndex {

    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException  {

//        generateFixedSizeRandomSentencesDataset();
        generatePartialDataset(0);
    }
    
    public static void generateFixedSizeRandomSentencesDataset() throws InvalidFileFormatException, IOException {
        
        Set<Integer> randomLuceneDocumentIds = MathUtil.getFixedSetOfFixedNumbers(10000, Integer.class, 0, 11699327);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.getInstance();
        
        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/test/1percent"));
        
        int i = 0, j = 0;
        
        System.out.println(randomLuceneDocumentIds.size());
        for ( Integer id : randomLuceneDocumentIds ) {
            if (reader.isDeleted(id)) continue;
            
            System.out.println("Sentence: " + i++);

            Document oldDoc = LuceneManager.getDocumentByNumber(reader, id);
            
            String pos = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
            String ner = oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE);
            
            Document newDoc = new Document();
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(j++));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))) - 1));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Constants.NOT_DUPLICATE_SENTENCE));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, pos == null ? "" : pos, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, ner == null ? "" : ner, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            
            writer.addDocument(newDoc);
        }
        
        writer.close();
        reader.close();
    }
    
    public static void generatePartialDataset(int percent) throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.getInstance();
        
        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
        IndexSearcher searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/test/1percent/index"));
        
        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        Set<Integer> ids = IndexManager.getInstance().getNonDuplicateSentences();
        System.out.println("Number of non duplicate sentences: " + ids.size());
        
        Frequency f = new Frequency();
        
        List<Document> documents = new ArrayList<Document>();
        
//        Set<Integer> goodIds = new HashSet<>();
        
//        int count = 0;
//        for ( int i = 0; i <= IndexManager.getInstance().getHighestTimeSliceId() ; i++ ) {
//            
//            Set<Integer> s = IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(i);
//            goodIds.addAll(s);
//            int now = s.size();
//            count += now;
//            System.out.println(now);
//        }
//        System.out.println(goodIds.size() + " " +  ids.size());
//        System.out.println();
//        System.out.println("docids: " + goodIds);
//        
//        System.out.println(count);
//        System.exit(0);
        
        int j = 1;
        for ( Integer id : ids ) {
//            if ( i % percent == 0 ) {

                Document oldDoc = IndexManager.getInstance().getDocumentById(searcher, id);
                
                f.addValue(oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE));
                
                String pos = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
                String ner = oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE);
                
                Document newDoc = new Document();
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(j++));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Constants.NOT_DUPLICATE_SENTENCE));
                newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, pos == null ? "" : pos, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, ner == null ? "" : ner, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                
//                writer.addDocument(newDoc);

                documents.add(newDoc);
                
                if ( documents.size() > 50000 ) {
                    
                    System.out.println("writing batch...");
                    writer.addDocuments(documents);
                    documents = new ArrayList<Document>();
                }
//            }
        }
        // write the last ones
        writer.addDocuments(documents);
        
        writer.close();
        reader.close();
    }
}
