/**
 * 
 */
package org.aksw.simba.rdflivenews.statistics;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.pattern.Pattern;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.math.Frequency;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Statistics {

    public void createStatistics(List<Pattern> patterns) {

        String fileName = RdfLiveNews.DATA_DIRECTORY + "statistics/";
        fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
        fileName += RdfLiveNews.CONFIG.getStringSetting("general", "index").replace("index/", "") + "-";
        fileName += "sim-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
        fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold");
        
        this.printPartOfSpeechTagDistribution(fileName, patterns);
        
    }

    private void printPartOfSpeechTagDistribution(String fileName, List<Pattern> patterns) {

        Frequency frequency = new Frequency();
        
        for ( Pattern pattern : patterns ) {
            
            String posTags = "";
            
            for ( String patternPart : pattern.getNaturalLanguageRepresentationWithTags().split(" ") ) {
                
                String posTag = patternPart.substring(patternPart.lastIndexOf("_") + 1) + " ";
                if ( !posTag.trim().equals(",") ) posTags += posTag;
            }
            
            frequency.addValue(posTags.trim());
        }
        
        BufferedFileWriter writer = new BufferedFileWriter(fileName + "-POS.txt", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        for ( Map.Entry<Comparable<?>, Long> entry : frequency.sortByValue()) {
            
            writer.write(entry.getKey() + "\t\t" + entry.getValue());
        }
        
        writer.close();        
    }
}
