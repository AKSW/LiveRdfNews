package org.rdflivenews.annotator.gui;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SolrItemDescriptionWidget extends VerticalLayout{
	
	private SolrItem item;
	
	public SolrItemDescriptionWidget(SolrItem item) {
		Label uriLabel = new Label("<b>" + item.getLabel() + "</b>", Label.CONTENT_XHTML);
		addComponent(uriLabel);
		
		Label descriptionLabel = new Label(item.getDescription(), Label.CONTENT_XHTML);
		addComponent(descriptionLabel);
		
		setExpandRatio(descriptionLabel, 1f);
		setSpacing(true);
		
	}
	
	public SolrItem getSolrItem(){
		return item;
	}

}
