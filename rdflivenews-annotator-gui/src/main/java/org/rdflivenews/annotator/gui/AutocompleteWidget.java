package org.rdflivenews.annotator.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class AutocompleteWidget extends VerticalLayout{
	
	interface SelectionListener{
		void itemSelected(SolrItem item);
	}
	
	private List<SelectionListener> listeners = new ArrayList<SelectionListener>();
	private TextField textField;
	private Table table;
	private SolrIndex index;
	
	public AutocompleteWidget(final SolrIndex index) {
		this.index = index;
		
		setWidth("400px");
		
		textField = new TextField();
		textField.setWidth("100%");
		textField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		textField.setTextChangeTimeout(200);
		addComponent(textField);
		
		table = new Table();
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.addContainerProperty("item", SolrItemDescriptionWidget.class, null);
		table.setImmediate(true);
		table.setWidth("100%");
		table.setHeight("350px");
		table.setMultiSelect(false);
		table.setSelectable(true);
		addComponent(table);
		
		textField.addListener(new TextChangeListener() {

		    public void textChange(TextChangeEvent event) {
		        if(event.getText().length() > 2){
		        	onRefreshTable(event.getText());
		        }
		    }
		});
		
		setExpandRatio(table, 1f);
		setSpacing(true);
	}
	
	public void setSearchTerm(String searchTerm){
		textField.setValue(searchTerm);
		onRefreshTable(searchTerm);
	}
	
	private void onRefreshTable(String searchTerm){
		table.removeAllItems();
    	Collection<SolrItem> newItems = index.search(searchTerm);//getDummyItems();
    	SolrItemDescriptionWidget widget;
    	for(final SolrItem item : newItems){
    		widget = new SolrItemDescriptionWidget(item);
    		widget.addListener(new LayoutClickListener() {
				
				@Override
				public void layoutClick(LayoutClickEvent event) {
					if (table.isSelected(item)) {
						table.unselect(item);
					} else {
						table.select(item);
						fireItemSelectionChanged(item);
					}
				}
			});
    		table.addItem(item).getItemProperty("item").setValue(widget);
    	}
	}
	
	private void fireItemSelectionChanged(SolrItem item){
		for(SelectionListener l : listeners){
			l.itemSelected(item);
		}
	}
	
	public void addSelectionListener(SelectionListener l){
		listeners.add(l);
	}
	
	public void removeSelectionListener(SelectionListener l){
		listeners.remove(l);
	}
	
	private Set<SolrItem> getDummyItems(){
		Set<SolrItem> items = new HashSet<SolrItem>();
		items.add(new SolrItem("a", "Leipzig", "Leipzig is a city in Saxony, Germany. It has b la bkanklalamkalkam klamlkakamkmaka", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png"));
		items.add(new SolrItem("b", "Dresden", "Leipzig is a city in Saxony, Germany. It has b la bkank lalamka lkamkla mlkakam kmaka", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png"));
		items.add(new SolrItem("b", "Dresden", "Leipzig is a city in Saxony, Germany. It has b la bkanklalamkalkamklamlkakamkmaka ssssssss ", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png"));
		items.add(new SolrItem("b", "Dresden", "Leipzig is a city in Saxony, Germany. It has b la bkanklalamkalkamklamlkakamkmaka gggg", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png"));
		items.add(new SolrItem("b", "Dresden", "Leipzig is a city in Saxony, Germany. It has b la bkanklalamkalkamklamlkakamkmaka", "http://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Coat_of_arms_of_Leipzig.svg/200px-Coat_of_arms_of_Leipzig.svg.png"));
		return items;
	}
	
	

}
