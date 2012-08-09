package org.aksw.simba.rdflivenews.patternsearch.concurrency;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.apache.log4j.Logger;

/**
 * Class to monitor how many sentences each pattern search callable
 * has already analyzed 
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class PatternSearchPrintProgressTask extends TimerTask {

    private DecimalFormat format = new DecimalFormat("##");
    private List<Callable<List<Pattern>>> callableList;
    private final Logger logger = Logger.getLogger(PatternSearchPrintProgressTask.class);
    private int totalNumber = 0;

    public PatternSearchPrintProgressTask(List<Callable<List<Pattern>>> callableList) {

        this.callableList = callableList;

        // we need this to calculate the total number of done searches for all
        // callables
        for (Callable<List<Pattern>> callable : callableList)
            totalNumber += ((PatternSearchCallable) callable).getNumberTotal();
    }

    @Override
    public void run() {

        this.logger.info("########################################");
        int totalProgress = 0;

        for (Callable<List<Pattern>> patternSearchCallable : this.callableList) {

            PatternSearchCallable patternSearchThread = (PatternSearchCallable) patternSearchCallable;

            int progress = Integer.valueOf(format.format(patternSearchThread.getProgress() * 100));
            totalProgress += patternSearchThread.getNumberDone();

            if (progress != 100 && (patternSearchThread.getProgress() > 0 && patternSearchThread.getProgress() < 100)) {

                this.logger.info(patternSearchThread.getName() + ": " + progress + "%. Found: " + patternSearchThread.getNumberOfResultsSoFar() + "  (" + patternSearchThread.getNumberDone() + "/"
                        + patternSearchThread.getNumberTotal() + ")");
            }
        }
        this.logger.info(Integer.valueOf(format.format(((double) totalProgress / totalNumber) * 100)) + "% (" + totalProgress + "/" + totalNumber + ")");
        this.logger.info("########################################");
    }
}