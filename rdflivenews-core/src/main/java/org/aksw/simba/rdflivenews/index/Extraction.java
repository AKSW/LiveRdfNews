package org.aksw.simba.rdflivenews.index;

public class Extraction {

	public String text = "";
	public String date = "";
	public String url = "";

	public Extraction(String url, String text, String date) {
		
		this.url =  url;
		this.text = text;
		this.date = date;
	}

	public Extraction() {}
}
