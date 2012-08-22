package org.aksw.simba.rdflivenews.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.lucene.LuceneManager;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class IndexManager {

    public static final int UNDEFINED_SENTENCE_ID = -1;
    
    /**
     * singleton
     */
    private static IndexManager INSTANCE;
    private Logger logger = Logger.getLogger(getClass());
    
    public static String INDEX_DIRECTORY = Config.RDF_LIVE_NEWS_DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "index");
    
    public static Directory INDEX;
    private IndexWriter writer;
    private final Analyzer analyzer = new LowerCaseWhitespaceAnalyzer();
    
    private int currentId = 0;

    /**
     * 
     * @return
     */
    public static synchronized IndexManager getInstance() {

        if ( IndexManager.INSTANCE == null ) IndexManager.INSTANCE = new IndexManager();
        return IndexManager.INSTANCE;
    }
    
    private IndexManager() {

        createIndex();
        this.currentId = getNumberOfDocuments();
    }

    /**
     * Opens and closes an index in the index directory
     */
    public void createIndex() {
        
        // create the index writer configuration and create a new index writer
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(LuceneManager.isIndexExisting(INDEX_DIRECTORY) ? OpenMode.APPEND : OpenMode.CREATE);
        writer = LuceneManager.openIndexWriter(INDEX_DIRECTORY, indexWriterConfig);
        INDEX = writer.getDirectory();
        closeLuceneIndex();
    }
    
    /**
     * 
     */
    public void openIndexWriter() {
        
        // create the index writer configuration and create a new index writer
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(OpenMode.APPEND);
        writer = LuceneManager.openIndexWriter(INDEX_DIRECTORY, indexWriterConfig);
    }
    
    /**
     * 
     */
    public void closeIndexWriter() {
        
        closeLuceneIndex();
    }
    
    /**
     * 
     * @param url 
     * @return
     */
    public synchronized boolean isNewArticle(String url) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
        LuceneManager.query(new IndexSearcher(LuceneManager.openIndexReader(INDEX)), new TermQuery(new Term(Constants.LUCENE_FIELD_URL, url)), collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        if ( hits != null && hits.length != 0 ) return false;
        
        return true;
    }
    
    /**
     * 
     * @param sentence
     */
    public synchronized void addSentence(Sentence sentence) {

        try {

            openIndexWriter();
            writer.addDocument(sentenceToDocument(sentence));
            closeIndexWriter();
        }
        catch (CorruptIndexException e) {

            e.printStackTrace();
            logger.error("Error writing articles to lucene database!", e);
        }
        catch (IOException e) {

            e.printStackTrace();
            logger.error("Error writing articles to lucene database!", e);
        }
    }
    
    /**
     * 
     * @param article
     */
    public synchronized void addSentences(List<Sentence> sentences) {
        
        try {
            
            openIndexWriter();
            
            for ( Sentence sentence : sentences ) 
                writer.addDocument(sentenceToDocument(sentence));
                    
            closeIndexWriter();
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param sentence
     * @return
     */
    private Document sentenceToDocument(Sentence sentence) {
        
        Document luceneDocument = new Document();
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(currentId++));
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(sentence.getExtractionDate().getTime()));
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(sentence.getTimeSliceID()));
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(0));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_TEXT, sentence.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, sentence.getPosTaggedSentence(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, sentence.getNerTaggedSentence(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_URL, sentence.getArticleUrl(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        
        return luceneDocument;
    }
    
    /**
     * 
     * @param article
     * @return
     */
    private Set<Document> sentencesToDocuments(Collection<Sentence> articles) {
        
        Set<Document> documents = new HashSet<Document>();
        
        for ( Sentence article : articles) 
            documents.add(sentenceToDocument(article));
        
        return documents;
    }
    
    /**
     * 
     * @param documentId
     * @param timeSlice
     */
    public void setDocumentDuplicateInTimeSlice(int documentId, int timeSlice) {
        
            Document oldDoc = LuceneManager.getDocumentByNumber(INDEX, documentId);
            
            Document newDoc = new Document();
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(NumericUtils.prefixCodedToInt(oldDoc.get(Constants.LUCENE_FIELD_ID))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(NumericUtils.prefixCodedToLong(oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(NumericUtils.prefixCodedToInt(oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(timeSlice));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            this.openIndexWriter();
            System.out.println("UPDATE: " + oldDoc.get(Constants.LUCENE_FIELD_ID));
            LuceneManager.updateDocument(this.writer, new Term(Constants.LUCENE_FIELD_ID, oldDoc.get(Constants.LUCENE_FIELD_ID)), newDoc);
            this.closeIndexWriter();
    }
    
    /**
     * 
     */
    public void closeLuceneIndex() {
        
        try {
            
            writer.close();
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void deleteIndex() {
        
        try {
            
            this.openIndexWriter();
            this.writer.deleteAll();
            this.closeIndexWriter();
            
            // reset the primary key
            this.currentId = getNumberOfDocuments();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    /**
     * @return the number of documents in the index
     */
    public int getNumberOfDocuments() {
        
        IndexReader reader;
        int numberOfDocuments = 0;
        
        reader = LuceneManager.openIndexReader(INDEX);
        numberOfDocuments = reader.maxDoc();
        LuceneManager.closeIndexReader(reader);
        
        return numberOfDocuments;
    }
    
    /**
     * 
     * @param documentId
     * @param field
     * @return
     */
    public String getStringValueFromDocument(int documentId, String field) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
        LuceneManager.query(INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(documentId))), collector);
        
        return LuceneManager.getDocumentByNumber(INDEX, collector.topDocs().scoreDocs[0].doc).get(field);
    }
    
    /**
     * 
     * @param documentId
     * @param field
     * @return
     */
    public String getStringValueFromDocument(IndexReader reader, int documentId, String field) {
        
        try {
            
            return reader.document(documentId).get(field);
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 
     * @param documentIds
     * @param field
     * @return
     */
    public Set<String> getStringValueFromDocuments(List<Integer> documentIds, String field) {

        Set<String> values = new HashSet<String>();
        
        try {
            
            IndexReader reader = IndexReader.open(INDEX);
            for ( Integer id : documentIds ) values.add(this.getStringValueFromDocument(reader, id, field));
            reader.close();
        }
        catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return values;
    }

    /**
     * Returns all documents (top 1000000) with the given timeslice id
     * from the underlying index. 
     * 
     * @param timeSlice
     * @return
     */
    public List<Integer> getSentenceIdsFromTimeSlice(int timeSlice) {

        List<Integer> documentIds = new ArrayList<Integer>();
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1000000, false);
        LuceneManager.query(INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs )
            documentIds.add(Integer.valueOf(LuceneManager.getDocumentByNumber(INDEX, hit.doc).get(Constants.LUCENE_FIELD_ID)));
              
        return documentIds;
    }
    
    public int getHighestTimeSliceId() throws CorruptIndexException, IOException {
        
        IndexReader reader = IndexReader.open(INDEX);
        IndexSearcher searcher = new IndexSearcher(reader);
        int maxTimeSliceId = 0;
        
        for ( int i = 0; i < reader.maxDoc() ; i++ ) 
            maxTimeSliceId = Math.max(Integer.valueOf(searcher.doc(i).get(Constants.LUCENE_FIELD_TIME_SLICE)), maxTimeSliceId);
        
        reader.close();
        searcher.close();
        
        return maxTimeSliceId;
    }
    
    public void getArticlesFromTimeSlice(int timeSliceId) {

//        TopScoreDocCollector collector = TopScoreDocCollector.create(10000000, false);
//        IndexReader reader = IndexReader.open(INDEX);
//        IndexSearcher searcher = new IndexSearcher(reader); 
//        searcher.search(new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
//        ScoreDoc[] hits = collector.topDocs().scoreDocs;
//        
//        for ( ScoreDoc hit : hits ) documentIds.add(hit.doc);
//                
//        searcher.close();
//        reader.close();
    }
}
