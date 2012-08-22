/**
 * 
 */
package org.aksw.simba.rdflivenews.lucene;

import java.io.File;
import java.io.IOException;

import org.aksw.simba.rdflivenews.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneManager {

    /**
     * Opens a lucene index at the specified location or throws a 
     * RuntimeException if something fails
     * 
     * @param absolutePathToLuceneIndex
     * @return a lucene index directory
     */
    public static Directory openLuceneIndex(String absolutePathToLuceneIndex) {
        
        try {
            
            return FSDirectory.open(new File(absolutePathToLuceneIndex));
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not open lucene index at: " + absolutePathToLuceneIndex, e);
        }
    }

    /**
     * 
     * @param index
     * @return
     */
    public static IndexReader openIndexReader(Directory index) {

        try {
            
            return IndexReader.open(index);
        }
        catch (CorruptIndexException e) {
            
            throw new RuntimeException("Could not open lucene index reader", e);
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not open lucene index reader", e);
        }
    }
    
    /**
     * 
     * @param index
     * @return
     */
    public static Document getDocumentByNumber(Directory index, int number) {

        Document doc = null;
        
        try {
            
            IndexReader reader = openIndexReader(index);
            doc = getDocument(reader, number);
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
        return doc;
    }
    
    /**
     * 
     * @param reader
     * @param number
     * @return
     */
    public static Document getDocumentByNumber(IndexReader reader, int number) {

        try {
            
            return reader.document(number);
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
    
    public static void updateDocument(IndexWriter writer, Term term, Document document) {
        
        try {
            
            writer.updateDocument(term, document);
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
     * @param searcher
     * @param query
     * @param collector
     */
    public static void query(IndexSearcher searcher, Query query, TopScoreDocCollector collector) {

        try {
            
            searcher.search(query, collector);
            searcher.getIndexReader().close();
            searcher.close();
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not query: \"" + query +  "\"", e);
        }
    }

    /**
     * 
     * @param reader
     * @param doc
     * @return
     */
    public static Document getDocument(IndexReader reader, int docId) {

        try {
            
            return reader.document(docId);
        }
        catch (CorruptIndexException e) {

            throw new RuntimeException("Could not get document: \"" + docId +  "\"", e);
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not get document: \"" + docId +  "\"", e);
        }
    }

    /**
     * 
     * @param searcher
     */
    public static void closeSearcher(IndexSearcher searcher) {

        try {
            searcher.close();
        }
        catch (IOException e) {

            throw new RuntimeException("Could not close searcher!", e);
        }
    }

    /**
     * 
     * @param reader
     */
    public static void closeIndexReader(IndexReader reader) {

        try {
            
            reader.close();
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not close reader!", e);
        }
    }

    /**
     * 
     * @param queryParser
     * @param queryString
     * @return
     */
    public static Query parse(QueryParser queryParser, String queryString) {

        try {
            
            return queryParser.parse(queryString);
        }
        catch (ParseException e) {
            
            throw new RuntimeException("Could not query: \""+ queryString +"\"!", e);
        }
    }

    /**
     * 
     * @param index
     * @param query
     * @param collector
     */
    public static void query(Directory index, Query query, TopScoreDocCollector collector) {

        IndexSearcher searcher = new IndexSearcher(LuceneManager.openIndexReader(index));
        LuceneManager.query(searcher, query, collector);
    }

    /**
     * 
     * @param indexDirectory
     */
    public static Directory createIndexIfNotExists(String indexDirectory) {

        Directory index = null;
        
        if ( !isIndexExisting(indexDirectory) ) {
            
            // create the index writer configuration and create a new index writer
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
            indexWriterConfig.setRAMBufferSizeMB(1024);
            indexWriterConfig.setOpenMode(OpenMode.CREATE);
            IndexWriter writer = openIndexWriter(indexDirectory, indexWriterConfig);
            index = writer.getDirectory();
            LuceneManager.closeIndexWriter(writer);
        }
        else index = openIndexDirectory(indexDirectory);
        
        return index;
    }
    
    /**
     * 
     * @param indexDirectory
     * @return
     */
    public static Directory openIndexDirectory(String indexDirectory) {
        
        try {
            
            return FSDirectory.open(new File(indexDirectory));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Closes a given lucene index writer
     * 
     * @param writer
     */
    public static void closeIndexWriter(IndexWriter writer) {

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
     * Checks if an index exists at the given location.
     * 
     * @param indexDirectory - the directory of the index to be checked
     * @return true if the index exists, false otherwise
     */
    public static boolean isIndexExisting(String indexDirectory) {
        
        try {
            
            return IndexReader.indexExists(FSDirectory.open(new File(indexDirectory)));
        }
        catch (IOException e) {
            
            e.printStackTrace();
            String error = "Check if index at: " + indexDirectory + " exists failed!";
            throw new RuntimeException(error, e);
        }
    }
    
    /**
     * Opens a lucene index writer for the specified index
     * 
     * @param absoluteFilePath - the path where to create/append the index
     * @param indexWriterConfig - the index write configuration
     * @return a indexwriter witht the given config for the given index or null if something fails
     */
    public static IndexWriter openIndexWriter(String absoluteFilePath, IndexWriterConfig indexWriterConfig) {

        try {
            
            return openIndexWriter(FSDirectory.open(new File(absoluteFilePath)), indexWriterConfig);
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 
     * @param index
     * @param indexWriterConfig
     * @return a indexwriter for the given index and config or null if something fails
     */
    public static IndexWriter openIndexWriter(Directory index, IndexWriterConfig indexWriterConfig) {
        
        try {
            
            return new IndexWriter(index, indexWriterConfig);
        }
        catch (CorruptIndexException e) {
            
            e.printStackTrace();
        }
        catch (LockObtainFailedException e) {
            
            e.printStackTrace();
        }
        catch (IOException e) {
            
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 
     * @param index
     * @return
     */
    public static IndexWriter openIndexWriterAppend(Directory index) {

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
        indexWriterConfig.setRAMBufferSizeMB(1024);
        indexWriterConfig.setOpenMode(OpenMode.APPEND);
        
        return openIndexWriter(index, indexWriterConfig);
    }
}
