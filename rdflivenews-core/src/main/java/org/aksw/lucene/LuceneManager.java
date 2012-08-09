/**
 * 
 */
package org.aksw.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


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
    public static IndexReader createIndexReader(Directory index) {

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
}
