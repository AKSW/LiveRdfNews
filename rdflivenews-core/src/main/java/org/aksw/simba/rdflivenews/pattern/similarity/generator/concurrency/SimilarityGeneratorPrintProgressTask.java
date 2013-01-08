/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SimilarityGeneratorPrintProgressTask extends TimerTask {

    private final Logger logger = Logger.getLogger(SimilarityGeneratorPrintProgressTask.class);
    private List<Callable<Set<Similarity>>> todo;
    private DecimalFormat format = new DecimalFormat("##");
    private int totalNumber = 0;
    
    public SimilarityGeneratorPrintProgressTask(List<Callable<Set<Similarity>>> todo) {

        this.todo = todo;
        
        // we need this to calculate the total number of done searches for all callables
        for (Callable<Set<Similarity>> callable : todo) totalNumber  += ((CachedSimilarityGeneratorCallable) callable).getNumberTotal();
    }

    /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {

        this.logger.info("########################################");
        int totalProgress = 0;

        for (Callable<Set<Similarity>> callable : this.todo) {

            CachedSimilarityGeneratorCallable similarityGeneratorCallable = (CachedSimilarityGeneratorCallable) callable;

            int progress = Integer.valueOf(format.format(similarityGeneratorCallable.getProgress() * 100));
            totalProgress += similarityGeneratorCallable.getNumberDone();

            if (progress != 100 && (similarityGeneratorCallable.getProgress() > 0 && similarityGeneratorCallable.getProgress() < 100)) {

                this.logger.info(similarityGeneratorCallable.getName() + ": " + progress + "%. (" + similarityGeneratorCallable.getNumberDone() + "/"
                        + similarityGeneratorCallable.getNumberTotal() + ")");
            }
        }

        String percent = totalNumber > 0 && totalProgress > 0 ? format.format(((double) totalProgress / totalNumber) * 100) : "0";
        this.logger.info(Integer.valueOf(percent) + "% (" + totalProgress + "/" + totalNumber + ")");
        this.logger.info("########################################");
    }

}
