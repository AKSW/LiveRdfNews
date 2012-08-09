/**
 * 
 */
package org.aksw;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.ini4j.InvalidFileFormatException;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CrawlAndQuerySimultaniouslyTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public CrawlAndQuerySimultaniouslyTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
//    public static Test suite() {
//
//        return new TestSuite(CrawlAndQuerySimultaniouslyTest.class);
//    }

    /**
     * Rigourous Test :-)
     * @throws ParseException 
     * 
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public void testQuerying() throws ParseException, IOException {
        
        while ( true ) {
            
            System.out.print("Please enter query: ");
            Scanner scanner = new Scanner(new BufferedInputStream(System.in), "UTF-8");
            String keyphrase = scanner.nextLine();
            String indexPath = "/Users/gerb/Development/workspaces/experimental/rdflivenews";
            
            List<String> sentences = new ArrayList<String>(this.getExactMatchSentences(indexPath, keyphrase, 1000));
            if ( sentences.isEmpty() ) System.out.println("No hits found ...");
            for (String sentence : sentences) {
                
                System.out.println(sentence);
            }
        }
    }
    
    public Set<String> getExactMatchSentences(String indexPath, String keyphrase, int maxNumberOfDocuments) throws ParseException, IOException {

        // create index searcher in read only mode
        IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File(indexPath))));
        Query query = new WildcardQuery(new Term("nerTaggedText", "*_" + keyphrase));
        System.out.println(query);
        ScoreDoc[] hits = indexSearcher.search(query, null, maxNumberOfDocuments).scoreDocs;
        TreeSet<String> list = new TreeSet<String>();

        // reverse order because longer sentences come last, longer sentences
        // most likely contain less it,he,she
        for (int i = hits.length - 1; i >= 0; i--) {

            // get the indexed string and put it in the result
            list.add(indexSearcher.doc(hits[i].doc).get("nerTaggedText"));
        }
        return list;
    }
}
