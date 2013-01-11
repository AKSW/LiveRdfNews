/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.refinement.concurrency.AprioriAndContextDisambiguationPatternRefinementCallable;
import org.aksw.simba.rdflivenews.pattern.refinement.concurrency.PatternRefinementPrintProgressTask;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternRefinementManager {

    private Logger logger = Logger.getLogger(PatternRefinementManager.class);
    
    public void startPatternRefinement(List<Pattern> patterns) {

        try {
            
            // create a thread pool and service for n threads/callable
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Pattern>> todo = new ArrayList<Callable<Pattern>>();
            
            // one thread for each luceneDocumentIds sublist
            for (int i = 0 ; i < patterns.size() ; i++) {
                todo.add(new AprioriAndContextDisambiguationPatternRefinementCallable(patterns.get(i), "PatternRefinementCallable-" + i));
                logger.info("Create refiner for pattern: " + patterns.get(i).getNaturalLanguageRepresentation());
            }
            
            logger.info("Created executorservice for pattern refinement with " + todo.size() + 
                    " threads and a thread pool of size " + Runtime.getRuntime().availableProcessors() + ".");
            
            // start the timer which prints every 30s the progress of the callables
            Timer timer = new Timer();
            timer.schedule(new PatternRefinementPrintProgressTask(todo), 0, 5000);
            
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
    }
}
