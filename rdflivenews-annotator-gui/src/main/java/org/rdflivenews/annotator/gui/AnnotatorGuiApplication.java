/* 
 * Copyright 2009 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.rdflivenews.annotator.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Demonstration application that shows how to use a simple custom client-side
 * GWT component, the ColorPicker.
 */
@SuppressWarnings("serial")
public class AnnotatorGuiApplication extends com.vaadin.Application {
    
//    static String dataPath = "/home/gerber/boa-data/rdflivenews/evaluation/";
    static String dataPath = "/Users/gerb/test/annotation/";
    
    private static List<Pattern> patterns;
    Window main = new Window("RdfLiveNews Annotator GUI");
    
    Label subject, object, patternLabel;
    Pattern pattern;
    
    private AutocompleteComboBox subjectCombobox, objectCombobox;
    private SolrIndex index = new SolrIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
    
    private HorizontalLayout mainLayout = new HorizontalLayout();

    @Override
    public void init() {
        setMainWindow(main);
        setTheme("mytheme");
        try {

            patterns = readPatterns();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if ( !patterns.isEmpty() ) {
         
            this.pattern = patterns.get(0);
            
            GridLayout grid = new GridLayout(3, 2);
            grid.setSpacing(true);
            
            subject = new Label(this.pattern.entityOne);
            subject.setContentMode(Label.CONTENT_XHTML);
            patternLabel = new Label("<b>" + this.pattern.nlr + "</b>");
            patternLabel.setContentMode(Label.CONTENT_XHTML);
            object = new Label(this.pattern.entityTwo);
            object.setContentMode(Label.CONTENT_XHTML);
            
            grid.addComponent(subject, 0, 0);
            grid.addComponent(patternLabel, 1, 0, 1, 1);
            grid.addComponent(object, 2, 0);
            
            subjectCombobox = new AutocompleteComboBox(index);
            grid.addComponent(subjectCombobox, 0, 1);
            objectCombobox = new AutocompleteComboBox(index);
            grid.addComponent(objectCombobox, 2, 1);
            
            subject.setWidth(null);
            grid.setComponentAlignment(subject, Alignment.MIDDLE_RIGHT);
            
            Panel panel = new Panel();
            
            
            mainLayout.addComponent(grid);
            mainLayout.addComponent(createButtonPanel());
            mainLayout.setWidth(null);
            mainLayout.setHeight("100%");
            panel.setWidth(null);
            
            panel.addComponent(mainLayout);
            
            main.removeAllComponents();
            main.addComponent(panel);
            ((VerticalLayout)main.getContent()).setHeight("100%");
            ((VerticalLayout)main.getContent()).setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        }
        else getMainWindow().showNotification("No patterns anymore...");
        
    }
    
    private Component createButtonPanel(){
    	VerticalLayout buttons = new VerticalLayout();
		buttons.setHeight("100%");
		buttons.addStyleName("buttons");
		Button nextButton = new Button();
		nextButton.setIcon(new ThemeResource("images/next.png"));
		nextButton.addStyleName(BaseTheme.BUTTON_LINK);
		nextButton.setDescription("Click to go to next pattern.");
		nextButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				fireGoodPatternDecision();
			}
		});
		buttons.addComponent(nextButton);
		buttons.setComponentAlignment(nextButton, Alignment.MIDDLE_CENTER);
		
		return buttons;
    }
    
    private static List<Pattern> readPatterns() throws IOException {
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        
//        for (String line : FileUtils.readLines(new File(dataPath +"patterns.txt")) ) {
//            
//            try {
//            
//                String[] lineParts = line.split("___");
//                Pattern defaultPattern = new Pattern(lineParts[0],lineParts[1],lineParts[2],Integer.valueOf(lineParts[3]));
//                patterns.add(defaultPattern);
//            }
//            catch (java.lang.NumberFormatException nfe) {
//                
//                System.out.println(line);
//            }
//        }
        
        patterns.add(new Pattern("Brad Pitt", "was born in", "Leipzig", 1));
        
        return patterns;
    }
    
    private void fireGoodPatternDecision(){
    	BufferedFileWriter writer = new BufferedFileWriter(dataPath + "good_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
        writer.write(patternToString(this.pattern));
        writer.close();
        
        patterns.remove(this.pattern);
        writeTodoPatterns();
        this.init();
    }
    
    private void fireBadPatternDecision(){
    	BufferedFileWriter writer = new BufferedFileWriter(dataPath + "bad_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
        writer.write(patternToString(this.pattern));
        writer.close();
        
        patterns.remove(this.pattern);
        writeTodoPatterns();
        this.init();
    }

    public static String patternToString(Pattern pattern) {
        
        return pattern.entityOne + "___" +
                pattern.nlr + "___" + 
                pattern.entityTwo + "___" +
                pattern.luceneId;
    }
    
    private synchronized void writeTodoPatterns() {
        
        BufferedFileWriter writer = new BufferedFileWriter(dataPath + "patterns.txt", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        for ( Pattern pattern : patterns) 
            writer.write(patternToString(pattern));
        writer.close();
    }
    
    private static class Pattern {
        
        public Pattern(String firstEntity, String nlr, String secondEntity, int luceneId) {

            this.entityOne = firstEntity;
            this.entityTwo = secondEntity;
            this.nlr = nlr;
            this.luceneId = luceneId;
        }
        String entityOne;
        String entityTwo;
        String nlr;
        int luceneId;
    }
}
