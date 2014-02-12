/**
 * 
 */
package org.aksw.simba.rdflivenews.util;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
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
     * @param lucene40 
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
            
            return DirectoryReader.open(index);
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
     * Do not use this method if you plan to use it thousands of times.
     * Better use the method with the reader as first parameter.
     * 
     * @param index
     * @return
     */
    public static Document getDocumentByNumber(Directory index, int number) {

        Document doc = null;
        
        try {
            
            IndexReader reader = openIndexReader(index);
            doc = getDocumentByNumber(reader, number);
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
     * @param doc
     * @return
     */
    public static Document getDocumentByNumber(IndexReader reader, int docNumber) {

        try {
            
            return reader.document(docNumber);
        }
        catch (CorruptIndexException e) {

            throw new RuntimeException("Could not get document: \"" + docNumber +  "\"", e);
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not get document: \"" + docNumber +  "\"", e);
        }
    }
    
    /**
     * Adds the given document to the index specified by the given index writer.
     * The writer is not closed!
     * 
     * @param writer
     * @param document
     */
    public static void addDocument(IndexWriter writer, Document document) {
        
        try {
            
            writer.addDocument(document);
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
     * Updates documents in the index specified by the index writer which match the term.
     * If you want to update a specific document the term needs to select a unique field.
     * The old document(s), if found, are deleted from the index and will be replaced by
     * the given document.
     * This index witer is not closed after this operation!  
     * 
     * @param writer - the index writer 
     * @param term - to select the document(s)
     * @param document - the new document
     */
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
     * Searcher is not closed
     * 
     * @param searcher
     * @param query
     * @param collector
     */
    public static void query(IndexSearcher searcher, Query query, TopScoreDocCollector collector) {

        try {
            
            searcher.search(query, collector);
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not query: \"" + query +  "\"", e);
        }
    }
    
    /**
     * 
     * @param searcher
     * @param query
     * @param sort
     * @param topNDocuments
     * @return
     */
    public static TopFieldDocs query(IndexSearcher searcher, Query query, Sort sort, int topNDocuments) {

        try {
            
            return searcher.search(query, topNDocuments, sort);
        }
        catch (IOException e) {
            
            throw new RuntimeException("Could not query: \"" + query +  "\"", e);
        }
    }

    /**
     * This method closes the index searcher as well as the index reader!
     * 
     * @param searcher
     */
    public static void closeIndexSearcher(IndexSearcher searcher) {

        try {
            
            searcher.getIndexReader().close();
//            ((Closeable) searcher).close();
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
     * Queries a given index with the given query. Every found document is stored in the
     * TopScoreDocCollector. 
     * 
     * @param index
     * @param query
     * @param collector
     */
    public static void query(Directory index, Query query, TopScoreDocCollector collector) {

        IndexSearcher searcher = new IndexSearcher(LuceneManager.openIndexReader(index));
        LuceneManager.query(searcher, query, collector);
        LuceneManager.closeIndexReader(searcher.getIndexReader());
        LuceneManager.closeIndexSearcher(searcher);
    }
    
    /**
     * 
     * @param indexDirectory
     */
    public static Directory createIndexIfNotExists(String indexDirectory, Version version) {

        Directory index = null;
        
        if ( !isIndexExisting(indexDirectory) ) {
            
            // create the index writer configuration and create a new index writer
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(version, new StandardAnalyzer(version));
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
            
            return DirectoryReader.indexExists(FSDirectory.open(new File(indexDirectory)));
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

    /**
     * 
     * @param index
     * @return
     */
    public static IndexSearcher openIndexSearcher(Directory index) {

        return new IndexSearcher(LuceneManager.openIndexReader(index));
    }
}
