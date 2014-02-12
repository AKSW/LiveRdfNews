/**
 * 
 */
package org.aksw.simba.rdflivenews;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.simba.rdflivenews.util.Encoder.Encoding;
import org.aksw.simba.rdflivenews.util.MavenUtil;
import org.aksw.simba.rdflivenews.util.PageRank;
import org.apache.commons.io.FileUtils;

/**
 * @author gerb
 *
 */
public class UrlPageRankChecker {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		Map<Integer,Set<String>> pageRankToWebsites = new HashMap<>();
		List<String> urlSet = FileUtils.readLines(MavenUtil.loadFile("/rss-list.txt"), "UTF-8"); 
		Collections.shuffle(urlSet);
		
		for ( String urlString : urlSet){

			URL url = new URL(urlString);
			String toRank = url.getProtocol() +  "://" + url.getHost();
			Integer pageRank = PageRank.getPR(toRank);
			
			System.out.println(pageRank +  " : " + toRank + " \t" + urlString);
			
			if ( !pageRankToWebsites.containsKey(pageRank) ) {
				
				Set<String> urls = new HashSet<>();
				urls.add(urlString);
				pageRankToWebsites.put(pageRank, urls);
			}
			else {
				
				pageRankToWebsites.get(pageRank).add(urlString);
			}
			
			BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/urls_pagerank.txt", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
			
			for ( Map.Entry<Integer, Set<String>> entries : pageRankToWebsites.entrySet() ) {
				
//				System.out.println(entries.getKey());
				writer.write(entries.getKey() + "");
				for ( String urlRanked : entries.getValue() ) {
					writer.write(urlRanked);
				}
				
				writer.write("\n\n");
			}
			writer.close();
			
			Thread.sleep(2500);
		}
	}
}
