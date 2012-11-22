package org.rdflivenews.annotator.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrIndex {

	private HttpSolrServer server;
	private final String searchField = "label";
	private final int maxNrOfItems = 25;
	
	public static void main(String[] args) {

        SolrIndex index = new SolrIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
        for ( SolrItem item : index.search("Mississippi")) {
            
            System.out.println(item.getUri());
        }
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
		List<SolrItem> result = new ArrayList<SolrIndex.SolrItem>();
		
		SolrQuery q;
		
		if ( searchTerm.contains(" ") ) 
		    q = new SolrQuery(searchField + ":(" + searchTerm  + ")");
		else
		    q = new SolrQuery(searchField + ":\"" + searchTerm  + "\"");
		 
		q.setRows(maxNrOfItems);
		System.out.println(q);
		try {
			QueryResponse rsp = server.query(q);
			SolrDocumentList docs = rsp.getResults();
			String uri = null;
			String label = null;
			String description = null;
			String imageURL = null;
			for(SolrDocument doc : docs){
				uri = (String) doc.get("uri");
				label = (String) doc.get("label");
				description = (String) doc.get("comment");
				imageURL = (String) doc.get("imageURL");
				result.add(new SolrItem(uri, label, description, imageURL));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static class SolrItem {
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
	}

}
