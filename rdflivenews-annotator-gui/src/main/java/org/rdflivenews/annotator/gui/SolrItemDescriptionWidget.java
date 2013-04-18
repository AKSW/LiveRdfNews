package org.rdflivenews.annotator.gui;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SolrItemDescriptionWidget extends VerticalLayout{
	
	private SolrItem item;
	
	public SolrItemDescriptionWidget(SolrItem item) {
		setWidth("370px");
		setHeight("60px");
		
		String description = "<div><b>" + item.getLabel() + "</b>  ("+item.getUri().replace("http://dbpedia.org/resource/", "dbr:")+")</div>";
		if(item.getImageURL() != null){
			description += "<div style='float: right; height: 40px; width: 60px'>" +
	    	 		"<div style='height: 100%;'><img style='height: 100%;' src=\"" + item.getImageURL() + "\"/></div>" +
	    	 		"</div>";
		}
		description += "<div style='white-space:normal;'>" + item.getDescription() + "</div>";
		Label uriLabel = new Label(description, Label.CONTENT_XHTML);
		uriLabel.setHeight("100%");
		addComponent(uriLabel);
	}
	
	public SolrItem getSolrItem(){
		return item;
	}

}
