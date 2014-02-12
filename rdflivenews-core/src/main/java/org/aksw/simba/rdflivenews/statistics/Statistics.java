/**
 * 
 */
package org.aksw.simba.rdflivenews.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.util.Frequency;

import edu.stanford.nlp.util.StringUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Statistics {
	
	public static Map<Integer, List<Long>> durationPerIteration = new HashMap<>();
	
	public Statistics(){
		
		for (int i = 0; i < 40; i++) durationPerIteration.put(i, new ArrayList<Long>());
	}

    public void createStatistics(List<Pattern> patterns) {

        String fileName = RdfLiveNews.DATA_DIRECTORY + "statistics/";
        fileName = fileName.endsWith("/") ? fileName : fileName + System.getProperty("file.separator");
        fileName += RdfLiveNews.CONFIG.getStringSetting("general", "index").replace("index/", "") + "-";
        fileName += "sim-" + RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").substring(RdfLiveNews.CONFIG.getStringSetting("classes", "similarity").lastIndexOf(".") + 1) + "-";
        fileName += RdfLiveNews.CONFIG.getDoubleSetting("similarity", "threshold");
        
        this.printPartOfSpeechTagDistribution(fileName, patterns);
        this.printStepDurations(fileName);
        this.printPatternNumber(fileName);
        this.printClusterNumber(fileName);
    }

    private void printClusterNumber(String fileName) {
    	
    	BufferedFileWriter writer = new BufferedFileWriter(fileName + "-CLUSTER_NUMBER1.txt", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
        int clusterAboveThreshold = 0;
    	for ( Cluster<Pattern> cluster : RdfLiveNews.clusters ){
        	if ( cluster.size() > 1 ) clusterAboveThreshold++;
        }
    	writer.write(RdfLiveNews.ITERATION + "\t" + RdfLiveNews.clusters.size() + "\t" + clusterAboveThreshold);
        writer.close();
	}

	private void printPatternNumber(String fileName) {
    	
    	BufferedFileWriter writer = new BufferedFileWriter(fileName + "-PATTERN_NUMBER1.txt", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
        int patternsAboveThreshold = 0;
    	for ( Pattern p : RdfLiveNews.patterns ){
        	if ( p.getTotalOccurrence() > 1 )patternsAboveThreshold++;
        }
    	writer.write(RdfLiveNews.ITERATION + "\t" + RdfLiveNews.patterns.size() + "\t" + patternsAboveThreshold);
        writer.close();
	}

	private void printStepDurations(String fileName) {
		
    	BufferedFileWriter writer = new BufferedFileWriter(fileName + "-RUNTIME1.txt", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
        for ( Map.Entry<Integer, List<Long>> entry : durationPerIteration.entrySet()) {
            
            writer.write(entry.getKey() + "\t" + StringUtils.join(entry.getValue(), "\t"));
        }
        
        writer.close();      
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
        
        BufferedFileWriter writer = new BufferedFileWriter(fileName + "-POS.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        for ( Map.Entry<Comparable<?>, Long> entry : frequency.sortByValue()) {
            
            writer.write(entry.getKey() + "\t\t" + entry.getValue());
        }
        
        writer.close();        
    }
}
