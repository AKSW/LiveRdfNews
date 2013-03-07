/**
 * 
 */
package org.aksw.simba.rdflivenews.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Version;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.lucene.LuceneManager;
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

        generatePartialDataset(0);
    }
    
    public static void generatePartialDataset(int percent) throws InvalidFileFormatException, IOException {
        
        // load the config, we dont need to configure logging because the log4j config is on the classpath
        RdfLiveNews.CONFIG = new Config(new Ini(RdfLiveNews.class.getClassLoader().getResourceAsStream("rdflivenews-config.ini")));
        RdfLiveNews.DATA_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY;
        IndexManager.getInstance();
        
        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
        IndexSearcher searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/tmp/100percent", Version.LUCENE_40));
        
        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        Set<Integer> ids = IndexManager.getInstance().getNonDuplicateSentences();
        System.out.println("Number of non duplicate sentences: " + ids.size());
        
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        
        List<Document> documents = new ArrayList<Document>();
        
        int j = 1;
        for ( Integer id : ids ) {

            Document oldDoc = IndexManager.getInstance().getDocumentById(searcher, id);
            
            String pos = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
            String ner = oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE);
            
            Document newDoc = new Document();
            newDoc.add(new Field(Constants.LUCENE_FIELD_ID, oldDoc.get(Constants.LUCENE_FIELD_ID), stringType));
            newDoc.add(new LongField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)), Store.YES));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_TIME_SLICE, Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))), Store.YES));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Constants.NOT_DUPLICATE_SENTENCE, Store.YES));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, pos == null ? "" : pos, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, ner == null ? "" : ner, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            
            documents.add(newDoc);
            
            if ( documents.size() > 50000 ) {
                
                System.out.println("writing batch...");
                writer.addDocuments(documents);
                documents = new ArrayList<Document>();
            }
        }
        // write the last ones
        writer.addDocuments(documents);
        
        writer.close();
        reader.close();
    }
    
//public static void generateFixedSizeRandomSentencesDataset() throws InvalidFileFormatException, IOException {
//        
//        Set<Integer> randomLuceneDocumentIds = MathUtil.getFixedSetOfFixedNumbers(10000, Integer.class, 0, 11699327);
//        
//        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
//        IndexManager.getInstance();
//        
//        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
//        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/tmp/1percent", Version.LUCENE_40));
//        
//        FieldType stringType = new FieldType(StringField.TYPE_STORED);
//        stringType.setStoreTermVectors(false);
//        
//        int i = 0, j = 0;
//        
//        System.out.println(randomLuceneDocumentIds.size());
//        
//        for ( Integer id : randomLuceneDocumentIds ) {
//        	
//        	// we would need to make a very long boolean query here to just use the index searcher methods
//        	// TODO make the query
//            
//            System.out.println("Sentence: " + i++);
//
//            Document oldDoc = LuceneManager.getDocumentByNumber(reader, id);
//            
//            String pos = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
//            String ner = oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE);
//            
//            Document newDoc = new Document();
//            newDoc.add(new Field(Constants.LUCENE_FIELD_ID, String.valueOf(j++), stringType));
//            newDoc.add(new IntField(Constants.LUCENE_FIELD_ID, j++, Store.YES));
//            newDoc.add(new LongField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)), Store.YES));
//            newDoc.add(new IntField(Constants.LUCENE_FIELD_TIME_SLICE, Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))), Store.YES));
//            newDoc.add(new IntField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Constants.NOT_DUPLICATE_SENTENCE, Store.YES));
//            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
//            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, pos == null ? "" : pos, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
//            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, ner == null ? "" : ner, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
//            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
//            
//            writer.addDocument(newDoc);
//        }
//        
//        writer.close();
//        reader.close();
//    }
}
