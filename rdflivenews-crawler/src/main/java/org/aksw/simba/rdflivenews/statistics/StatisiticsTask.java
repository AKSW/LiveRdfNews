/**
 * 
 */
package org.aksw.simba.rdflivenews.statistics;

import java.util.TimerTask;

import org.aksw.simba.rdflivenews.concurrency.QueueManager;
import org.aksw.simba.rdflivenews.index.IndexManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class StatisiticsTask  extends TimerTask {

    private int timeSliceId = 0;
    
    /**
     * 
     * @param timeSliceId
     */
    public StatisiticsTask(int timeSliceId) {
        
        this.timeSliceId = timeSliceId;
    }
    
    @Override
    public void run() {

        StringBuffer buffer = new StringBuffer();
        test(buffer);
        System.out.println(buffer);
    }

    public void printTimeSliceStatistics(int timeSliceId) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("Statistics for Time-Slice: " + timeSliceId);
    }
    
    private void test(StringBuffer buffer) {
        
        buffer.append(String.format("There are currently %s articles on the crawl stack!", 
                QueueManager.getInstance().getNumberOfQueuedArticles())).append("\n");
        
        buffer.append(String.format("There are currently %s sentences for time slice %s.",
                IndexManager.getInstance().getSentenceFromTimeSlice(this.timeSliceId).size(), this.timeSliceId)).append("\n");
        
//        IndexManager.getInstance().getArticlesFromTimeSlice(this.timeSliceId);
    }
}
