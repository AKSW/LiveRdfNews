/**
 * 
 */
package org.aksw.simba.rdflivenews.lucene;

import java.io.File;
import java.io.IOException;

import org.aksw.simba.rdflivenews.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
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
            
            doc = openIndexReader(index).document(number);
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

    public static void query(Directory index, Query query, TopScoreDocCollector collector) {

        IndexSearcher searcher = new IndexSearcher(LuceneManager.openIndexReader(index));
        LuceneManager.query(searcher, query, collector);
    }
}
