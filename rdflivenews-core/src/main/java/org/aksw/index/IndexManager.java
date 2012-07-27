package org.aksw.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class IndexManager {

    /**
     * singleton
     */
    private static IndexManager INSTANCE;
    private Logger logger = Logger.getLogger(getClass());
    
    private final String INDEX_DIRECTORY = NewsCrawler.CONFIG.getStringSetting("database", "directory");
    
    private FSDirectory index;
    private static IndexWriter writer;
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
     * @param uri 
     * @return
     */
    public synchronized boolean isNewArticle(String uri) {
        
        try {

            TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
            IndexReader reader = IndexReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader); 
            searcher.search(new WildcardQuery(new Term("articleURL", uri)), collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            searcher.close();
            reader.close();
            
            if ( hits != null && hits.length != 0 ) return false;
        }
        catch (IOException e) {
            
            logger.error("Could not execute exists query for uri: " + uri, e);
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * 
     * @param article
     */
    public synchronized void addNewsArticle(NewsArticle article) {

        try {

            openIndexWriter();
            writer.addDocument(articleToDocument(article));
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
    public void addNewsArticles(Set<NewsArticle> articles) {
        
        for ( NewsArticle article : articles )
            addNewsArticle(article);
    }
    
    /**
     * 
     * @param article
     * @return
     */
    private Document articleToDocument(NewsArticle article) {
        
        Document luceneDocument = new Document();
        luceneDocument.add(new NumericField("extractionDate", Field.Store.YES, true).setLongValue(article.getExtractionDate().getTime()));
        luceneDocument.add(new NumericField("timeSliceID", Field.Store.YES, true).setIntValue(article.getTimeSliceID()));
        luceneDocument.add(new Field("imageURL", article.getImageUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field("articleURL", article.getArticleUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field("title", article.getTitle(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field("text", article.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field("posTaggedText", article.getPosTaggedText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        luceneDocument.add(new Field("nerTaggedText", article.getNerTaggedText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        
        for ( String keyword : article.getKeywords() )
            luceneDocument.add(new Field("keywords", keyword, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
                
        if ( article.getHtml() != null && !article.getHtml().isEmpty() )
            luceneDocument.add(new Field("html", article.getHtml(), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        
        return luceneDocument;
    }
    
    /**
     * 
     * @param article
     * @return
     */
    private Set<Document> articlesToDocuments(Collection<NewsArticle> articles) {
        
        Set<Document> documents = new HashSet<Document>();
        
        for ( NewsArticle article : articles) 
            documents.add(articleToDocument(article));
        
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
     * Create a new filesystem lucene index
     * 
     * @param absoluteFilePath - the path where to create/append the index
     * @param indexWriterConfig - the index write configuration
     * @return
     */
    private IndexWriter createIndex(String absoluteFilePath, IndexWriterConfig indexWriterConfig) {

        try {
            
            index = FSDirectory.open(new File(absoluteFilePath));
            return new IndexWriter(index, indexWriterConfig);
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
}
