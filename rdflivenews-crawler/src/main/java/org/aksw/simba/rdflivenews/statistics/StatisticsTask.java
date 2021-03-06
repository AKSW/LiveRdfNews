/**
 * 
 */
package org.aksw.simba.rdflivenews.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.TimerTask;

import org.aksw.simba.rdflivenews.RdfLiveNewsCrawler;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class StatisticsTask  extends TimerTask {
    
    private String splitter = "\t";
    private BufferedWriter writer;
    
    public StatisticsTask() {
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("time slice id").append(splitter);
        buffer.append("# urls on crawl stack").append(splitter);
        buffer.append("# sentences for current time slice id").append(splitter);
        buffer.append("total # of sentences").append(splitter);
        buffer.append("\n");
        
        this.write(buffer.toString());
    }

    @Override
    public void run() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(RdfLiveNewsCrawler.TIME_SLICE_ID).append(splitter);
        buffer.append(RdfLiveNewsCrawler.queue.size()).append(splitter);
        buffer.append(IndexManager.getInstance().getSentenceIdsFromTimeSlice(RdfLiveNewsCrawler.TIME_SLICE_ID).size()).append(splitter);
        buffer.append(IndexManager.getInstance().getNumberOfDocuments()).append(splitter);
        buffer.append("\n");
        this.write(buffer.toString());
    }

    private void write(String stuffToWrite) {

        try {
            
            File statisticsFolder = new File(RdfLiveNewsCrawler.CONFIG.getStringSetting("general", "data-directory") + RdfLiveNewsCrawler.CONFIG.getStringSetting("general", "statistics"));
            if ( !statisticsFolder.exists() )
                statisticsFolder.mkdir();
            
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statisticsFolder.getAbsolutePath() + "/statistics.tsv", true), "UTF-8"));
            this.writer.write(stuffToWrite);
            this.writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
