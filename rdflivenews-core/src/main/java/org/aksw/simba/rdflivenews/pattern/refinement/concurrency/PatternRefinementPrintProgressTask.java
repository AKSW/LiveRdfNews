/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.refinement.concurrency;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.Similarity;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency.CachedSimilarityGeneratorCallable;
import org.aksw.simba.rdflivenews.pattern.similarity.generator.concurrency.SimilarityGeneratorPrintProgressTask;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternRefinementPrintProgressTask extends TimerTask {

    private final Logger logger = Logger.getLogger(PatternRefinementPrintProgressTask.class);
    private List<Callable<Pattern>> todo;
    private DecimalFormat format = new DecimalFormat("##");
    private int totalNumber = 0;
    
    public PatternRefinementPrintProgressTask(List<Callable<Pattern>> todo) {

        this.todo = todo;
        
        // we need this to calculate the total number of done searches for all callables
        for (Callable<Pattern> callable : todo) totalNumber  += ((PatternRefinementCallable) callable).getNumberTotal();
    }

    /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {

        this.logger.info("########################################");
        int totalProgress = 0;

        for (Callable<Pattern> callable : this.todo) {

            PatternRefinementCallable refinementCallable = (PatternRefinementCallable) callable;

            int progress = Integer.valueOf(format.format(refinementCallable.getProgress() * 100));
            totalProgress += refinementCallable.getNumberDone();

            if (progress != 100 && (refinementCallable.getProgress() > 0 && refinementCallable.getProgress() < 100)) {

                this.logger.info(refinementCallable.getName() + ": " + progress + "%. (" + refinementCallable.getNumberDone() + "/"
                        + refinementCallable.getNumberTotal() + ")");
            }
        }

        String percent = totalNumber > 0 && totalProgress > 0 ? format.format(((double) totalProgress / totalNumber) * 100) : "0";
        this.logger.info(Integer.valueOf(percent) + "% (" + totalProgress + "/" + totalNumber + ")");
        this.logger.info("########################################");
    }
}
