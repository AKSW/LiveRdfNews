package org.rdflivenews.annotator.gui;

import java.util.HashSet;
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
	
	public Set<SolrItem> search(String searchTerm){
		Set<SolrItem> result = new HashSet<SolrIndex.SolrItem>();
		
		SolrQuery q = new SolrQuery(searchField + "(" + searchTerm  + "*)");
		try {
			QueryResponse rsp = server.query(q);
			SolrDocumentList docs = rsp.getResults();
			String uri = null;
			String label = null;
			for(SolrDocument doc : docs){
				uri = (String) doc.get("uri");
				label = (String) doc.get("label");
				System.out.println(uri + ": " + label);
				result.add(new SolrItem(uri, label));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public class SolrItem {
		private String label;
		private String uri;
		
		public SolrItem(String uri, String label) {
			this.label = label;
			this.uri = uri;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getUri() {
			return uri;
		}
	}

}
