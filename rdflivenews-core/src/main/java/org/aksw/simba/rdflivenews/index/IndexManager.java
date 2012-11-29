package org.aksw.simba.rdflivenews.index;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.github.gerbsen.lucene.LuceneManager;

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
        LuceneManager.closeIndexWriter(this.writer);
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
     * @param url 
     * @return
     */
    public synchronized boolean isNewArticle(String url) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
        IndexSearcher searcher = new IndexSearcher(LuceneManager.openIndexReader(INDEX));
        LuceneManager.query(searcher, new TermQuery(new Term(Constants.LUCENE_FIELD_URL, url)), collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        
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
            LuceneManager.closeIndexWriter(this.writer);
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
                    
            LuceneManager.closeIndexWriter(this.writer);
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
        luceneDocument.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Constants.NOT_DUPLICATE_SENTENCE));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_TEXT, sentence.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, sentence.getPosTaggedSentence(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, sentence.getNerTaggedSentence(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field(Constants.LUCENE_FIELD_URL, sentence.getArticleUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        
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
        
        TermQuery query     = new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(documentId)));
        IndexSearcher searcher = LuceneManager.openIndexSearcher(INDEX);
        Document oldDoc = IndexManager.getInstance().getDocumentByQuery(searcher, query);
            
        Integer oldDuplicateTimeSlice = Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)));
        
        Document newDoc = new Document();
        newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID))));
        newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf((oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)))));
        newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)))));
        newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(oldDuplicateTimeSlice.equals(Constants.NOT_DUPLICATE_SENTENCE) ? timeSlice : oldDuplicateTimeSlice));
        newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        
        this.openIndexWriter();
        LuceneManager.updateDocument(this.writer, query.getTerm(), newDoc);
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        LuceneManager.closeIndexWriter(this.writer);
    }
    
    /**
     * Sets a bunch of sentences as duplicate in the index. The timeSlice id in which
     * the sentence was found is used to mark it
     * 
     * @param duplicateIds
     * @param timeSlice
     */
    public void setDocumentsDuplicateInTimeSlice(Set<Integer> duplicateIds, int timeSlice) {
        
        System.out.println("Setting " + duplicateIds.size()  + " sentences duplicate!");

        IndexSearcher searcher  = LuceneManager.openIndexSearcher(INDEX);
        IndexWriter writer      = LuceneManager.openIndexWriterAppend(INDEX);
        
        for ( Integer id : duplicateIds ) {
            
            TermQuery query     = new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(id)));
            Document oldDoc = IndexManager.getInstance().getDocumentByQuery(searcher, query);
                
            Document newDoc = new Document();
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf((oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)))));
            newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(timeSlice));
            newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            
            LuceneManager.updateDocument(writer, query.getTerm(), newDoc);
        }
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        LuceneManager.closeIndexWriter(writer);
    }
    
    /**
     * Resets all documents in the index as NON-duplicate.
     * 
     * @param duplicateIds
     * @param timeSlice
     */
    public void setDocumentsToNonDuplicateSentences() {

        IndexSearcher searcher  = LuceneManager.openIndexSearcher(INDEX);
        IndexWriter writer      = LuceneManager.openIndexWriterAppend(INDEX);
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(10_000_000, true);
        LuceneManager.query(searcher, NumericRangeQuery.newIntRange(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, 0, 1000, true, true), collector);

        System.out.println(" found " + collector.getTotalHits());
        int i = 0;
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs ) {
            
            if ( i++ % 1000 == 0 ) System.out.print("\r" + NumberFormat.getPercentInstance().format((double) i / collector.getTotalHits()));

            Document oldDoc = LuceneManager.getDocumentByNumber(searcher.getIndexReader(), hit.doc);

            if ( oldDoc != null ) {
                
                Document newDoc = new Document();
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_ID, Field.Store.YES, true).setIntValue(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_EXTRACTION_DATE, Field.Store.YES, true).setLongValue(Long.valueOf((oldDoc.get(Constants.LUCENE_FIELD_EXTRACTION_DATE)))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_TIME_SLICE, Field.Store.YES, true).setIntValue(Integer.valueOf((oldDoc.get(Constants.LUCENE_FIELD_TIME_SLICE)))));
                newDoc.add(new NumericField(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, Field.Store.YES, true).setIntValue(Constants.NOT_DUPLICATE_SENTENCE));
                newDoc.add(new Field(Constants.LUCENE_FIELD_TEXT, oldDoc.get(Constants.LUCENE_FIELD_TEXT), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_POS_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE, oldDoc.get(Constants.LUCENE_FIELD_NER_TAGGED_SENTENCE), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                newDoc.add(new Field(Constants.LUCENE_FIELD_URL, oldDoc.get(Constants.LUCENE_FIELD_URL), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
                
                LuceneManager.updateDocument(writer, new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(Integer.valueOf(oldDoc.get(Constants.LUCENE_FIELD_ID)))), newDoc);
            }
        }
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        LuceneManager.closeIndexWriter(writer);
    }
    
    /**
     * 
     */
    public void deleteIndex() {
        
        try {
            
            this.openIndexWriter();
            this.writer.deleteAll();
            LuceneManager.closeIndexWriter(this.writer);
            
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
    public Set<Integer> getSentenceIdsFromTimeSlice(int timeSlice) {

        Set<Integer> documentIds = new HashSet<Integer>();
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1000000, false);
        LuceneManager.query(INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
        
        IndexReader reader = LuceneManager.openIndexReader(INDEX);
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs ) 
            documentIds.add(Integer.valueOf(LuceneManager.getDocumentByNumber(reader, hit.doc).get(Constants.LUCENE_FIELD_ID)));
                
        LuceneManager.closeIndexReader(reader);
              
        return documentIds;
    }
    
    public Set<String> getNumberOfArticlesInTimeSlice(int timeSlice) {

        Set<String> articleUrls = new HashSet<String>();
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1000000, false);
        LuceneManager.query(INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
        
        IndexReader reader = LuceneManager.openIndexReader(INDEX);
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs ) 
            articleUrls.add(LuceneManager.getDocumentByNumber(reader, hit.doc).get(Constants.LUCENE_FIELD_URL));
                
        LuceneManager.closeIndexReader(reader);
              
        return articleUrls;
    }
    
    public Map<Integer,String> getIdsAndTextFromTimeSlice(int timeSlice) {

        Map<Integer,String> idsToSentence = new HashMap<>();
        
        // get all sentences from the current time slice
        TopScoreDocCollector collector = TopScoreDocCollector.create(2_000_000, false);
        LuceneManager.query(INDEX, new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(timeSlice))), collector);
        
        IndexReader reader = LuceneManager.openIndexReader(INDEX);
        System.out.println(String.format("Found %s sentences with time slice id %s!", collector.getTotalHits(), timeSlice));
        
        for ( ScoreDoc hit : collector.topDocs().scoreDocs ) {
            
            Document document = LuceneManager.getDocumentByNumber(reader, hit.doc);
            idsToSentence.put(Integer.valueOf(document.get(Constants.LUCENE_FIELD_ID)), document.get(Constants.LUCENE_FIELD_TEXT));
        }
        
        LuceneManager.closeIndexReader(reader);
              
        return idsToSentence;
    }
    
    /**
     * 
     * @return
     * @throws CorruptIndexException
     * @throws IOException
     */
    public int getHighestTimeSliceId() {
        
        IndexReader reader = LuceneManager.openIndexReader(INDEX);
        IndexSearcher searcher = new IndexSearcher(reader);
        int maxTimeSliceId = 0;
        
        for ( int i = 0; i < reader.maxDoc() ; i++ ) 
            maxTimeSliceId = Math.max(Integer.valueOf(LuceneManager.getDocumentByNumber(reader, i).get(Constants.LUCENE_FIELD_TIME_SLICE)), maxTimeSliceId);
        
        LuceneManager.closeIndexReader(reader);
        LuceneManager.closeIndexSearcher(searcher);
        
        return maxTimeSliceId;
    }
    
    /**
     * 
     * @param searcher
     * @param query
     * @return
     */
    public Document getDocumentByQuery(IndexSearcher searcher, Query query) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
        LuceneManager.query(searcher, query, collector);
        return LuceneManager.getDocumentByNumber(searcher.getIndexReader(), collector.topDocs().scoreDocs[0].doc);
    }

    /**
     * 
     * @param searcher
     * @param id
     * @return
     */
    public Document getDocumentById(IndexSearcher searcher, Integer id) {
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
        LuceneManager.query(searcher, new TermQuery(new Term(Constants.LUCENE_FIELD_ID, NumericUtils.intToPrefixCoded(id))), collector);
        return LuceneManager.getDocumentByNumber(searcher.getIndexReader(), collector.topDocs().scoreDocs[0].doc);
    }

    /**
     * 
     * @param iteration
     * @return
     */
    public Set<Integer> getNonDuplicateSentenceIdsForIteration(int iteration) {

        Query timeSlice = new TermQuery(new Term(Constants.LUCENE_FIELD_TIME_SLICE, NumericUtils.intToPrefixCoded(iteration)));
        Query duplicate = new TermQuery(new Term(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, NumericUtils.intToPrefixCoded(Constants.NOT_DUPLICATE_SENTENCE)));
        
        BooleanQuery query = new BooleanQuery();
        query.add(new BooleanClause(timeSlice, Occur.MUST));
        query.add(new BooleanClause(duplicate, Occur.MUST));
        
        IndexSearcher searcher = LuceneManager.openIndexSearcher(INDEX);
        TopScoreDocCollector collector = TopScoreDocCollector.create(2_000_000, false);
        LuceneManager.query(searcher, query, collector);
        
        Set<Integer> nonDuplicateSentencesUntilIteration = new HashSet<>();
        
        // add the primary key of each document to the list
        for ( ScoreDoc doc : collector.topDocs().scoreDocs )
            nonDuplicateSentencesUntilIteration.add(
                    Integer.valueOf(LuceneManager.getDocumentByNumber(searcher.getIndexReader(), doc.doc).get(Constants.LUCENE_FIELD_ID)));
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        
        return nonDuplicateSentencesUntilIteration;
    }
    
    /**
     * 
     * @return
     */
    public Set<Integer> getNonDuplicateSentences() {

        Query duplicate = new TermQuery(new Term(Constants.LUCENE_FIELD_DUPLICATE_IN_TIME_SLICE, NumericUtils.intToPrefixCoded(Constants.NOT_DUPLICATE_SENTENCE)));
        
        IndexSearcher searcher = LuceneManager.openIndexSearcher(INDEX);
        TopScoreDocCollector collector = TopScoreDocCollector.create(2_000_000, false);
        LuceneManager.query(searcher, duplicate, collector);
        
        Set<Integer> nonDuplicateSentences = new HashSet<>();
        
        // add the primary key of each document to the list
        for ( ScoreDoc doc : collector.topDocs().scoreDocs )
            nonDuplicateSentences.add(
                    Integer.valueOf(LuceneManager.getDocumentByNumber(searcher.getIndexReader(), doc.doc).get(Constants.LUCENE_FIELD_ID)));
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        
        return nonDuplicateSentences;
    }
    
    /**
     * 
     * @return
     */
    public Set<String> getSentencesFromArticle(String articleUrl) {

        Query articles = new TermQuery(new Term(Constants.LUCENE_FIELD_URL, articleUrl));
        
        IndexSearcher searcher = LuceneManager.openIndexSearcher(INDEX);
        TopScoreDocCollector collector = TopScoreDocCollector.create(1000, false);
        LuceneManager.query(searcher, articles, collector);
        System.out.println(articles);
        Set<String> sentences = new HashSet<>();
        
        // add the primary key of each document to the list
        for ( ScoreDoc doc : collector.topDocs().scoreDocs )
            sentences.add(
                    LuceneManager.getDocumentByNumber(searcher.getIndexReader(), doc.doc).get(Constants.LUCENE_FIELD_TEXT));
        
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
        
        return sentences;
    }

    public void closeIndexWriter() {

        LuceneManager.closeIndexWriter(this.writer);
    }
}
