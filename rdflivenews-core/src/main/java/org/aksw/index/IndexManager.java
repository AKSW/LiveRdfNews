package org.aksw.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.Constants;
import org.aksw.NewsCrawler;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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
    
    public static String INDEX_DIRECTORY = NewsCrawler.CONFIG.getStringSetting("database", "directory");
    
    public static Directory INDEX;
    private IndexWriter writer;
    private final Analyzer analyzer = new LowerCaseWhitespaceAnalyzer();

    /**
     * 
     * @return
     */
    public static IndexManager getInstance() {

        if ( IndexManager.INSTANCE == null ) IndexManager.INSTANCE = new IndexManager();
        return IndexManager.INSTANCE;
    }
    
    private IndexManager() {

        createIndex();
    }

    /**
     * Opens and closes an index in the index directory
     */
    public void createIndex() {
        
        // create the index writer configuration and create a new index writer
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(isIndexExisting(INDEX_DIRECTORY) ? OpenMode.APPEND : OpenMode.CREATE);
        writer = createIndex(INDEX_DIRECTORY, indexWriterConfig);
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
        writer = createIndex(INDEX_DIRECTORY, indexWriterConfig);
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
        
        try {

            TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
            IndexReader reader = IndexReader.open(INDEX);
            IndexSearcher searcher = new IndexSearcher(reader); 
            searcher.search(new TermQuery(new Term(Constants.LUCENE_FIELD_URL, url)), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            searcher.close();
            reader.close();
            
            if ( hits != null && hits.length != 0 ) return false;
        }
        catch (IOException e) {
            
            logger.error("Could not execute exists query for uri: " + url, e);
            e.printStackTrace();
        }
        
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
    public void addSentences(List<Sentence> sentences) {
        
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
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(sentence.getExtractionDate().getTime()));
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(sentence.getTimeSliceID()));
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
     * Checks if an index exists at the given location.
     * 
     * @param indexDirectory - the directory of the index to be checked
     * @return true if the index exists, false otherwise
     */
    public boolean isIndexExisting(String indexDirectory) {
        
        try {
            
            return IndexReader.indexExists(FSDirectory.open(new File(indexDirectory)));
        }
        catch (IOException e) {
            
            e.printStackTrace();
            String error = "Check if index exists failed!";
            throw new RuntimeException(error, e);
        }
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
        
        try {
            
            reader = IndexReader.open(INDEX);
            numberOfDocuments = reader.maxDoc();
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
        return numberOfDocuments;
    }
    
    /**
     * Create a new filesystem lucene index
     * 
     * @param absoluteFilePath - the path where to create/append the index
     * @param indexWriterConfig - the index write configuration
     * @return
     */
    private IndexWriter createIndex(String absoluteFilePath, IndexWriterConfig indexWriterConfig) {

        try {
            
            INDEX = FSDirectory.open(new File(absoluteFilePath));
            return new IndexWriter(INDEX, indexWriterConfig);
        }
        catch (CorruptIndexException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
        catch (LockObtainFailedException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
        catch (IOException e) {
            
            e.printStackTrace();
            throw new RuntimeException("Could not create index", e);
        }
    }
    
    /**
     * 
     * @param documentId
     * @param field
     * @return
     */
    public String getStringValueFromDocument(int documentId, String field) {
        
        String fieldValue = null;
        
        try {
            
            IndexReader reader = IndexReader.open(INDEX);
            fieldValue = reader.document(documentId).get(field);
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
        
        return fieldValue;
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
     * Returns all documents (top 10000000) with the given timeslice id
     * from the underlying index. 
     * 
     * @param timeSlice
     * @return
     */
    public List<Integer> getSentenceFromTimeSlice(int timeSlice) {

        List<Integer> documentIds = new ArrayList<Integer>();
        
        try {
            
            TopScoreDocCollector collector = TopScoreDocCollector.create(10000000, false);
            IndexReader reader = IndexReader.open(INDEX);
            IndexSearcher searcher = new IndexSearcher(reader); 
            searcher.search(new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            for ( ScoreDoc hit : hits ) documentIds.add(hit.doc);
                    
            searcher.close();
            reader.close();
        }
        catch (CorruptIndexException e) {

            e.printStackTrace();
        }
        catch (IOException e) {
            
            e.printStackTrace();
        }
        return documentIds;
    }
}
