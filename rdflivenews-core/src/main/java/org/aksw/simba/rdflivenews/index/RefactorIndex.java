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
import org.aksw.simba.rdflivenews.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.simba.rdflivenews.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
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

//        generateFixedSizeRandomSentencesDataset();
        generatePartialDataset(1);
    }
    
    public static void generateFixedSizeRandomSentencesDataset() throws InvalidFileFormatException, IOException {
        
        Set<Integer> randomLuceneDocumentIds = MathUtil.getFixedSetOfFixedNumbers(10000, Integer.class, 0, 11699327);
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.getInstance();
        
        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/test/10000sentences"));
        
        int i = 0, j = 0;
        
        System.out.println(randomLuceneDocumentIds.size());
        for ( Integer id : randomLuceneDocumentIds ) {
            
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
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            writer.addDocument(newDoc);
        }
        
        writer.close();
        reader.close();
    }
    
    public static void generatePartialDataset(int percent) throws InvalidFileFormatException, IOException {
        
        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/rdflivenews-config.ini")));
        IndexManager.getInstance();
        
        IndexReader reader = LuceneManager.openIndexReader(IndexManager.INDEX);
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/Users/gerb/test/100percent"));
        
        List<Document> documents = new ArrayList<Document>();
        
        for ( int i = 1, j = 0; i < reader.maxDoc() ; i++ ) {

            if ( i % percent == 0 ) {

                Document oldDoc = LuceneManager.getDocumentByNumber(reader, i);
                
                Document newDoc = new Document();
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(j++));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))) - 1));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Constants.NOT_DUPLICATE_SENTENCE));
                newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, "", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, "", Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                
                System.out.println("UPDATE DOC #" + i + " with id: " + j +" of " + reader.maxDoc());
                documents.add(newDoc);
                
                if ( documents.size() > 50000 ) {
                    
                    System.out.println("writing batch...");
                    writer.addDocuments(documents);
                    documents = new ArrayList<Document>();
                }
            }
        }
        // write the last ones
        writer.addDocuments(documents);
        
        writer.close();
        reader.close();
    }
}
