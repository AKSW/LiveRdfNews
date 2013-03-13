/**
 * 
 */
package org.aksw.simba.rdflivenews.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
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
        IndexWriter writer = LuceneManager.openIndexWriterAppend(LuceneManager.createIndexIfNotExists("/home/gerber/wiki/100percent", Version.LUCENE_40));
        
        StanfordNLPNamedEntityRecognition nerTagger = new StanfordNLPNamedEntityRecognition();
        StanfordNLPPartOfSpeechTagger posTagger = new StanfordNLPPartOfSpeechTagger();
        
//        IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
        Set<Integer> ids = getSentencesFromWikipediaIndex();
        System.out.println("Number of non duplicate sentences: " + ids.size());
        
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        
        List<Document> documents = new ArrayList<Document>();
        
        int j = 1;
        for ( Integer id : ids ) {
        	
//        	if ( j++ % 10 != 0 ) continue;

//            Document oldDoc = IndexManager.getInstance().getDocumentById(searcher, id);
            Document oldDoc = LuceneManager.getDocumentByNumber(searcher.getIndexReader(), id);
            
//            String pos = oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE);
            String ner = oldDoc.get("ner");
            if ( ner == null ) System.out.println("NER NULL for " +  j);
            
            Document newDoc = new Document();
            newDoc.add(new Field(Constants.LUCENE_FIELD_ID, j + "", stringType));
            newDoc.add(new LongField(Constants.LUCENE_FIELD_EXTRACTION_DATE, new Date().getTime(), Store.YES));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_TIME_SLICE, 0, Store.YES));
            newDoc.add(new IntField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Constants.NOT_DUPLICATE_SENTENCE, Store.YES));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get("sentence"), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, posTagger.getAnnotatedSentences(oldDoc.get("sentence")), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, ner == null ? nerTagger.getAnnotatedSentences(oldDoc.get("sentence")) : ner, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get("uri"), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
            
            documents.add(newDoc);
            
            if ( documents.size() > 100000 ) {
                
                System.out.println("writing batch ... " + j);
                writer.addDocuments(documents);
                documents = new ArrayList<Document>();
            }
        }
        // write the last ones
        writer.addDocuments(documents);
        
        writer.close();
        reader.close();
    }
    
    public static Set<Integer> getSentencesFromWikipediaIndex() {

    	IndexSearcher searcher = LuceneManager.openIndexSearcher(IndexManager.INDEX);    	
    	TopScoreDocCollector collector = TopScoreDocCollector.create(100_000_000, true);
        LuceneManager.query(searcher, new MatchAllDocsQuery(), collector);
    	
        Set<Integer> sentences = new HashSet<>();
        
        // add the primary key of each document to the list
        for ( ScoreDoc doc : collector.topDocs().scoreDocs )
            sentences.add(doc.doc);
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        
        return sentences;
    }
    
    public Document getDocumentById(IndexSearcher searcher, Integer id) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
//        LuceneManager.query(searcher, new TermQuery(new Term(Constants.LUCENE_FIELD_ID, String.valueOf(id))), collector);
        
        // TODO this needs to be like this if you have and old index
        LuceneManager.query(searcher, NumericRangeQuery.newIntRange(Constants.LUCENE_FIELD_ID, id, id, true, true), collector);
        return LuceneManager.getDocumentByNumber(searcher.getIndexReader(), collector.topDocs().scoreDocs[0].doc);
    }
}
