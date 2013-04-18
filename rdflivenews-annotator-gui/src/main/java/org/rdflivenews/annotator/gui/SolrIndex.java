package org.rdflivenews.annotator.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;


public class SolrIndex {

	private HttpSolrServer server;
	private final String searchField = "label";
	private final String sortField = "pagerank";
	private final int maxNrOfItems = 25;
	
	public static void main(String[] args) {

        SolrIndex index = new SolrIndex("http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_resources/");
//        SolrIndex index = new SolrIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
//        System.out.println(index.search("Mississippi").size());
//        System.out.println(index.search("Leip").size());
//        System.out.println(index.search("Brad Pitt").size());
        
        System.out.println("Query for: 'Peyton Manning'");
        for ( SolrItem item : index.search("Peyton Manning")) {
            
            System.out.println(item.getUri());
        }
//        System.out.println();
//        for ( SolrItem item : index.search("Leip")) {
//            
//            System.out.println(item.getUri());
//        }
//        System.out.println();
//        for ( SolrItem item : index.search("Brad Pitt")) {
//            
//            System.out.println(item.getUri());
//        }
    }

	public SolrIndex(String serverURL) {
		server = new HttpSolrServer(serverURL);

		server.setSoTimeout(1000); // socket read timeout
		server.setConnectionTimeout(100);
		server.setDefaultMaxConnectionsPerHost(100);
		server.setMaxTotalConnections(100);
		server.setFollowRedirects(false); // defaults to false
		// allowCompression defaults to false.
		// Server side must support gzip or deflate for this to have any effect.
		server.setAllowCompression(true);
		server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
	}
	
	public Collection<SolrItem> search(String searchTerm){
		
		List<SolrItem> resources = new ArrayList<SolrItem>();
		
		SolrQuery query = new SolrQuery();
		query.setQuery("surfaceForms:\""+ searchTerm +"\"");
		query.addSortField("disambiguationScore", ORDER.desc);
		query.addField("uri");
		query.addField("label");
		query.addField("comment");
		query.addField("imageUrl");
		query.setRows(100);
		
		try {

			for ( SolrDocument doc : server.query(query).getResults()) {
				
				String uri = (String) doc.getFieldValue("uri");
				String label = (String) doc.getFieldValue("label");
				String comment = (String) doc.getFieldValue("comment");
				String imageURL = (String) doc.getFieldValue("imageUrl");
				
				System.out.println(doc);
				
				SolrItem res = new SolrItem(uri, label, comment, imageURL);
				
				System.out.println(res.getUri());
				resources.add(res);
			}
		}
		catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resources;
	}
	
	public static class SolrItem implements Comparable<SolrItem> {

        private String label;
		private String uri;
		private String description;
		private String imageURL;
		
		public SolrItem(String uri, String label, String description, String imageURL) {
			this.label = label;
			this.uri = uri;
			this.description = description;
			this.imageURL = imageURL;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getUri() {
			return uri;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getImageURL() {
			return imageURL;
		}

        @Override
        public int compareTo(SolrItem o) {

            return Integer.valueOf(this.uri.length()).compareTo(Integer.valueOf(o.uri.length()));
        }
	}

}
