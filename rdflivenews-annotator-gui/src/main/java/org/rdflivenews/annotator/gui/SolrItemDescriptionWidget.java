package org.rdflivenews.annotator.gui;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SolrItemDescriptionWidget extends VerticalLayout{
	
	private SolrItem item;
	
	public SolrItemDescriptionWidget(SolrItem item) {
		String description = "<div><b>" + item.getLabel() + "</b></div>";
		description += "<div>" + item.getDescription() + "</div>";
		Label uriLabel = new Label(description, Label.CONTENT_XHTML);
		uriLabel.setWidth("200px");
		uriLabel.setHeight(null);
		addComponent(uriLabel);
		setWidth("250px");
//		setWidth("300px");
		
	}
	
	public SolrItem getSolrItem(){
		return item;
	}

}
