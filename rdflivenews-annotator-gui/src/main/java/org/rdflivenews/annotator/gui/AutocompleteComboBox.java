package org.rdflivenews.annotator.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.ui.ComboBox;

public class AutocompleteComboBox extends ComboBox{
	
	private SolrIndex index;
	private static final int MIN_NUMBER_OF_CHARACTERS = 3;
	
	public AutocompleteComboBox(SolrIndex index) {
		super();
		this.index = index;
		setNewItemsAllowed(true);
	}
	
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		String filterString = (String) variables.get("filter");
		if (filterString != null && filterString.length() >= MIN_NUMBER_OF_CHARACTERS) {
			items.removeAllItems();
			setItems(filterString);
		}
		super.changeVariables(source, variables);
	}
	
	public void setItems(String filterString){
		System.out.println("Refreshing Combobox...");
		List<String> list = Arrays.asList(new String[]{"Wikipedia", "DBpedia", "DBpedia Live", "Brad Jolie", "Brad Pitt", "Steven Spielberg"});
		
//		for(String item : list){
//			if(item.toLowerCase().startsWith(filterString.toLowerCase())){
//				items.addItem(item);
//			}
//		}
		
		Set<SolrItem> searchResult = index.search(filterString);
		for(SolrItem item : searchResult){
			items.addItem(item.getLabel());
		}
		
	}
	
	

}
