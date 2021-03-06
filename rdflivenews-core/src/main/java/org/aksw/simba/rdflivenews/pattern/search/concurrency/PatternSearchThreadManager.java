/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.search.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.util.ListUtil;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternSearchThreadManager {
    
    private Logger logger = Logger.getLogger(PatternSearchThreadManager.class);

    /**
     * This methods searches for patterns in index documents. This methods 
     * starts numberOfTotalSearchThreads Threads and supplies each thread
     * with a sublist of luceneDocumentIds, the ids of all sentences which
     * should be used to find patterns in.
     * 
     * @param luceneDocumentIds
     * @param numberOfTotalSearchThreads
     * @return
     */
    public List<Pattern> startPatternSearchCallables(List<Integer> luceneDocumentIds) {

        List<Pattern> results = new ArrayList<Pattern>();
        
        try {
            
            // we create numberOfTotalSearchThreads threads, so we need to split the sentences 
            List<List<Integer>> luceneDocumentsIdsSubLists = ListUtil.split(luceneDocumentIds, (luceneDocumentIds.size() / Runtime.getRuntime().availableProcessors()) + 1);

            // create a thread pool and service for n threads/callable
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            logger.info("Created executorservice for pattern search with " + Runtime.getRuntime().availableProcessors() + 
                    " threads and a thread pool of size " + Runtime.getRuntime().availableProcessors() + ".");
            
            List<Callable<List<Pattern>>> todo = new ArrayList<Callable<List<Pattern>>>();
            
            int i = 1;
            // one thread for each luceneDocumentIds sublist
            for (List<Integer> luceneDocumentsIdsSubList : luceneDocumentsIdsSubLists ) {
                
                todo.add(new PatternSearchCallable(luceneDocumentsIdsSubList, "PatternSearchCallable-" + i++));
                logger.info("Create thread for " + luceneDocumentsIdsSubList.size() + " sentences.");
            }
            
            // start the timer which prints every 30s the progress of the callables
            Timer timer = new Timer();
            timer.schedule(new PatternSearchPrintProgressTask(todo), 0, 1000);
            
            // invoke all waits until all threads are finished
            List<Future<List<Pattern>>> answers = executorService.invokeAll(todo);
            
            // all threads have finished so we can shut down the progess printing
            timer.cancel();
            
            // collect all the results
            for (Future<List<Pattern>> future : answers) {
                
                results.addAll(future.get());
            }
            
            // shut down the service and all threads
            executorService.shutdown();
        }
        catch (ExecutionException e) {
            
            e.printStackTrace();
            String error = "Could not execute callables!";
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        catch (InterruptedException e) {
            
            e.printStackTrace();
            String error = "Threads got interrupted!";
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        
        return results;
    }

    /**
     * 
     * @param newFoundPatternsLists
     * @return
     */
    public List<Pattern> mergeNewFoundPatterns(List<Pattern> newFoundPatternsLists) {
        
        return this.mergePatterns(new HashMap<Integer,Pattern>(), newFoundPatternsLists);
    }
    
    /**
     * TODO change type of first argument to map<integer,pattern> if more appropriate
     * 
     * @param oldPatternList
     * @param newPatterns
     * @return
     */
    public List<Pattern> mergeNewFoundAndOldPattern(List<Pattern> oldPatternList, List<Pattern> newPatterns){
        
        Map<Integer,Pattern> oldPatterns = new HashMap<Integer,Pattern>();
        for ( Pattern pattern : oldPatternList ) oldPatterns.put(pattern.hashCode(), pattern);
        
        return this.mergePatterns(oldPatterns, newPatterns);
    }
    
    /**
     * 
     * @param foundPatterns
     * @return
     */
    private List<Pattern> mergePatterns(Map<Integer,Pattern> oldPatterns, List<Pattern> newFoundPatternsLists) {
        
        for ( Pattern currentPattern : newFoundPatternsLists ) {
            
            // we found the same pattern again
            if ( oldPatterns.containsKey(currentPattern.hashCode())) {
                
                Pattern oldPattern = oldPatterns.get(currentPattern.hashCode());
                oldPattern.increaseOccurrence(currentPattern.getTotalOccurrence());
                oldPattern.addManyLearnedFromEntities(currentPattern.getLearnedFromEntities());
            }
            // a new pattern was found
            else {
                
                oldPatterns.put(currentPattern.hashCode(), currentPattern);
            }
        }
        
        return new ArrayList<Pattern>(oldPatterns.values());
    }

    public void logPatterns(List<Pattern> patterns) {

        String fileName = RdfLiveNews.DATA_DIRECTORY + "patterns/iter-#" + RdfLiveNews.ITERATION + "-";
        fileName += RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
        fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold");
        
        BufferedFileWriter shortWriter  = new BufferedFileWriter(fileName + "-short.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        BufferedFileWriter longWriter   = new BufferedFileWriter(fileName + "-long.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        
        for (Pattern p : patterns) {
            
//            if ( p.getScore() > RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") ) {

                longWriter.write(p.toString());
                shortWriter.write(p.getNaturalLanguageRepresentation() + "\t" +p.getLearnedFromEntities().size() + "\t" + p.getNaturalLanguageRepresentationWithTags());
//            }
        }
        shortWriter.close();
        longWriter.close();
    }
}
