/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency.CachedSimilarityGeneratorCallable;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency.SimilarityGeneratorPrintProgressTask;
import org.apache.log4j.Logger;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityGeneratorManager {

    private String similarityMetric;
    
    private Set<Similarity> results;
    private Logger logger = Logger.getLogger(SimilarityGeneratorManager.class);

    public SimilarityGeneratorManager(String similarityMetric) {

        this.similarityMetric           = similarityMetric;
    }

    /**
     * 
     * @param patterns
     * @return
     */
    public Set<Similarity> startSimilarityGeneratorThreads(List<Pattern> patterns, Set<Similarity> results) {
        
        try {
            
            // create a thread pool and service for n threads/callable
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Set<Similarity>>> todo = new ArrayList<Callable<Set<Similarity>>>();
            
            // one thread for each luceneDocumentIds sublist
            for (int i = 0 ; i < patterns.size() ; i++) {
                
                Pattern pattern = patterns.get(i);
//                if ( pattern.isAboveThresholds() ) {
                    
                    todo.add(new CachedSimilarityGeneratorCallable(patterns.subList(i + 1, patterns.size()), pattern, this.similarityMetric, results, "CachedSimilarityGeneratorCallable-" + i));
                    logger.info("Create thread for pattern: " + pattern.getNaturalLanguageRepresentation());
//                }
            }
            
            logger.info("Created executorservice for similarity calculation with " + todo.size() + 
                    " threads and a thread pool of size " + Runtime.getRuntime().availableProcessors() + ".");
            
            // start the timer which prints every 30s the progress of the callables
            Timer timer = new Timer();
            timer.schedule(new SimilarityGeneratorPrintProgressTask(todo), 0, 10000);
            
            // invoke all waits until all threads are finished
            executorService.invokeAll(todo);
            
            // all threads have finished so we can shut down the progess printing
            timer.cancel();
            
            // shut down the service and all threads
            executorService.shutdown();
        }
        catch (InterruptedException e) {
            
            e.printStackTrace();
            String error = "Threads got interrupted!";
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        
        if ( RdfLiveNews.CONFIG.getBooleanSetting("similarity", "writeFile") ) this.logSimilarities(results);
        return results; 
    }

    /**
     * 
     * @param patterns
     */
    private void logSimilarities(Set<Similarity> results) {

        String fileName = RdfLiveNews.DATA_DIRECTORY + RdfLiveNews.CONFIG.getStringSetting("general", "similarity");
        fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
        fileName += "iter-#" + RdfLiveNews.ITERATION + "-";
        fileName += "sim-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
        fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold") + ".tsv";
                
        List<String> lines = new ArrayList<String>();
        for ( Similarity sim : results ) lines.add(sim.toString());

        Collections.sort(lines);
        BufferedFileWriter writer = new BufferedFileWriter(fileName, Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for (String line : lines ) writer.write(line); 
        writer.close();        
    }
}
