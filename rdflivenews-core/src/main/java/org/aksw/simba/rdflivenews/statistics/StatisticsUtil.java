/**
 * 
 */
package org.aksw.simba.rdflivenews.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.util.ReflectionManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class StatisticsUtil {

    private static BufferedFileWriter writer;
    private static int highestTimeSlice = -1;
    private static Map<String,List<String>> statistics;
    
    // how many pairs per sentence per iteration
    
    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

    	RdfLiveNews.init();
        writer = new BufferedFileWriter(RdfLiveNews.DATA_DIRECTORY + "statistics/deduplication.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        
        highestTimeSlice    = IndexManager.getInstance().getHighestTimeSliceId();
        statistics          = new LinkedHashMap<String,List<String>>();
        
        System.out.println("getNumberOfSentencesPerTimeSlice");
        getNumberOfSentencesPerTimeSlice();
        System.out.println("getNumberOfNonDuplicateSentencesPerTimeSlice");
        getNumberOfNonDuplicateSentencesPerTimeSlice(Arrays.asList(1.0, 0.95, 0.9, 0.85, 0.8), Arrays.asList(1, 10, 100));
        System.out.println("getNumberOfArticles");
        getNumberOfArticles();

        // write the header
        writer.writeLineNoNewLine("iteration\t");
        writer.write(StringUtils.join(statistics.keySet(), "\t"));
        
        // write every iteration
        for ( int i = 0 ; i <= highestTimeSlice ; i++ ) {

            writer.writeLineNoNewLine(String.valueOf(i) + "\t");
            for ( Map.Entry<String, List<String>> entry : statistics.entrySet()) {
                
                writer.writeLineNoNewLine(entry.getValue().get(i) + "\t");
            }
            writer.writeLineNoNewLine("\n");
        }
        
        writer.close();
    }
    
    private static void getNumberOfArticles() {
        
        List<String> results = new ArrayList<String>();
        
        for (int iteration = 0; iteration <= highestTimeSlice ; iteration++) 
            results.add(String.valueOf(IndexManager.getInstance().getNumberOfArticlesInTimeSlice(iteration).size()));
        
        statistics.put("article", results);
    }
    
    public static Map<String,Integer> getTokenDistributionForPatterns(List<Pattern> patterns) {
        
        Map<String,Integer> distribution = new HashMap<>();
        for ( Pattern pattern : patterns ) {
            for (String token : pattern.getNaturalLanguageRepresentation().split(" ") ) {

                if ( distribution.containsKey(token) ) distribution.put(token, distribution.get(token) + 1);
                else distribution.put(token, 1);
            }
        }
        
        return distribution;
    }

    /**
     * creates a statisitcs of how many sentences per timeslice were found for crawling
     * @param normalTripleWriter 
     */
    private static void getNumberOfSentencesPerTimeSlice() {
        
        List<String> results = new ArrayList<String>();
        
        for (int iteration = 0; iteration <= highestTimeSlice ; iteration++) 
            results.add(String.valueOf(IndexManager.getInstance().getSentenceIdsFromTimeSlice(iteration).size()));
        
        statistics.put("raw", results);
    }
    
    /**
     * creates a statisitcs of how many non duplicate sentences per timeslice were found for crawling
     * @param normalTripleWriter 
     */
    private static void getNumberOfNonDuplicateSentencesPerTimeSlice(List<Double> thresholds, List<Integer> windowSizes) {
        
        for ( Integer windowSize : windowSizes) {

            for ( Double threshold : thresholds ) {
                
                System.out.println(String.format("\t%s/%s", threshold, windowSize));
                
                RdfLiveNews.CONFIG.setStringSetting("deduplication", "threshold", String.valueOf(threshold));
                RdfLiveNews.CONFIG.setStringSetting("deduplication", "window", String.valueOf(windowSize));
                
                List<String> results = new ArrayList<String>();

                for ( int iteration = 0 ; iteration <= highestTimeSlice ; iteration++ ) {
                    
                    Deduplication deduplication = (Deduplication) ReflectionManager.newInstance(RdfLiveNews.CONFIG.getStringSetting("classes", "deduplication"));
                    deduplication.runDeduplication(iteration, iteration + 1, RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window"));
                }
                
                System.out.println("\tdeduplication done");
                
                for (int i = 0; i <= highestTimeSlice ; i++) 
                    results.add(String.valueOf(IndexManager.getInstance().getNonDuplicateSentenceIdsForIteration(i).size()));
                
                statistics.put("non-duplicate@" + 
                        RdfLiveNews.CONFIG.getStringSetting("deduplication", "threshold") + "/" +  
                        RdfLiveNews.CONFIG.getStringSetting("deduplication", "window"), results);
                
                IndexManager.getInstance().setDocumentsToNonDuplicateSentences();
                
                System.out.println("\tresetting index done");
            }
        }
    }
}
