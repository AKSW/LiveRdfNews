package org.rdflivenews.annotator.gui;

import java.util.Set;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AutocompleteWidget extends VerticalLayout{
	
	private SolrIndex index;
	
	public AutocompleteWidget(SolrIndex solrIndex) {
		this.index = solrIndex;
		setWidth("300px");
		
		TextField textField = new TextField();
		textField.setWidth("100%");
		textField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		textField.setTextChangeTimeout(200);
		addComponent(textField);
		
		final Table table = new Table();
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.addContainerProperty("item", SolrItemDescriptionWidget.class, null);
		table.setImmediate(true);
		table.setWidth("100%");
		table.setHeight("300px");
		table.setMultiSelect(false);
		table.setSelectable(true);
		addComponent(table);
		
		textField.addListener(new TextChangeListener() {

		    public void textChange(TextChangeEvent event) {
		    	System.out.println(event);
		        if(event.getText().length() > 2){
		        	table.removeAllItems();
		        	Set<SolrItem> newItems = index.search(event.getText());
		        	for(SolrItem item : newItems){
		        		table.addItem(item).getItemProperty("item").setValue(new SolrItemDescriptionWidget(item));
		        	}
		        }
		    	
		    	
		    }
		});
		
		setExpandRatio(table, 1f);
		setSpacing(true);
	}
	
	

}
