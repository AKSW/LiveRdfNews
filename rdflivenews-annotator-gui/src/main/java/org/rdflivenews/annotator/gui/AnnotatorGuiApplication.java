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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Demonstration application that shows how to use a simple custom client-side
 * GWT component, the ColorPicker.
 */
@SuppressWarnings("serial")
public class AnnotatorGuiApplication extends com.vaadin.Application implements ClickListener {
    
//    static String dataPath = "/home/gerber/boa-data/rdflivenews/evaluation/";
    static String dataPath = "/Users/gerb/test/annotation/";
    
    private final String RDFLIVENEWS_PREFIX = "http://rdflivenews.aksw.org/resource/";
    
    private static List<Pattern> patterns;
    Window main = new Window("RdfLiveNews Annotator GUI");
    
    Label subject, object, patternLabel, sentence;
    TextField subjectUri, objectUri;
    TextArea saidObject, comment;
    
    Pattern pattern;
    
    private AutocompleteComboBox subjectCombobox, objectCombobox;
    private SolrIndex index = new SolrIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
//    private SolrIndex index = new SolrIndex("http://[2001:638:902:2010:0:168:35:138]:8080/solr/#/dbpedia_resources/");
    
    private VerticalLayout mainLayout = new VerticalLayout();

    private NativeButton nextButton, trashButton;

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
            
            GridLayout grid = new GridLayout(3, 6);
            grid.setSpacing(true);
            
            subject = new Label(this.pattern.entityOne);
            subject.setContentMode(Label.CONTENT_XHTML);
            patternLabel = new Label("<b>" + this.pattern.nlr + "</b>");
            patternLabel.setContentMode(Label.CONTENT_XHTML);
            object = new Label(this.pattern.entityTwo);
            object.setContentMode(Label.CONTENT_XHTML);
            
            String sentenceLabel = this.pattern.sentence.
                    replace(this.pattern.entityOne, "<b>" + this.pattern.entityOne + "</b>").
                    replace(this.pattern.entityTwo, "<b>" + this.pattern.entityTwo + "</b>").
                    replace(this.pattern.nlr, "<span style=\"color:red\">" + this.pattern.nlr + "</span>");
            
            sentence =  new Label(sentenceLabel);
            sentence.setContentMode(Label.CONTENT_XHTML);
            
            grid.addComponent(sentence, 0, 0, 2, 0);
            grid.addComponent(subject, 0, 1);
            grid.addComponent(patternLabel, 1, 1, 1, 1);
            grid.addComponent(object, 2, 1);
            
            subjectCombobox = new AutocompleteComboBox("Subject URI", index);
            grid.addComponent(subjectCombobox, 0, 2);
            objectCombobox = new AutocompleteComboBox("Object URI", index);
            grid.addComponent(objectCombobox, 2, 2);
            
            subjectUri = new TextField("Subject URI");
            objectUri = new TextField("Object URI");
            subjectUri.setWidth("100%");
            objectUri.setWidth("100%");
            grid.addComponent(subjectUri, 0, 3);
            grid.addComponent(objectUri, 2, 3);

            saidObject = new TextArea("Say Cluster Object Value");
            saidObject.setWidth("100%");
            grid.addComponent(saidObject, 0, 4, 2, 4);
            
            comment = new TextArea("Comments");
            comment.setWidth("100%");
            grid.addComponent(comment, 0, 5, 2, 5);
            
            Panel panel = new Panel();
            mainLayout.addComponent(grid);
            
            HorizontalLayout submitButtonslayout = new HorizontalLayout();
            
            nextButton = new NativeButton("Next");
            trashButton = new NativeButton("Trash");
            nextButton.addListener(this);
            trashButton.addListener(this);
            submitButtonslayout.setSpacing(true);
            submitButtonslayout.setWidth("100%");
            submitButtonslayout.addComponent(trashButton);
            submitButtonslayout.addComponent(nextButton);
            submitButtonslayout.setComponentAlignment(nextButton, Alignment.MIDDLE_RIGHT);
            
            mainLayout.addComponent(submitButtonslayout);
            mainLayout.setSpacing(true);
            mainLayout.setWidth(null);
            mainLayout.setHeight("100%");
            panel.setWidth(null);
            
            panel.addComponent(mainLayout);
            
            getMainWindow().removeAllComponents();
            main.addComponent(panel);
            ((VerticalLayout)main.getContent()).setHeight("100%");
            ((VerticalLayout)main.getContent()).setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        }
        else getMainWindow().showNotification("No patterns anymore...");
        
    }
    
//    private Component createButtonPanel(){
//    	VerticalLayout buttons = new VerticalLayout();
//		buttons.setHeight("100%");
//		buttons.addStyleName("buttons");
//		Button posExampleButton = new Button();
//		posExampleButton.setIcon(new ThemeResource("images/thumb_up.png"));
//		posExampleButton.addStyleName(BaseTheme.BUTTON_LINK);
//		posExampleButton.setDescription("Click if this pattern is a good pattern.");
//		posExampleButton.addListener(new Button.ClickListener() {
//			
//			@Override
//			public void buttonClick(ClickEvent event) {
//				fireGoodPatternDecision();
//			}
//		});
//		buttons.addComponent(posExampleButton);
//		Button negExampleButton = new Button();
//		negExampleButton.setIcon(new ThemeResource("images/thumb_down.png"));
//		negExampleButton.addStyleName(BaseTheme.BUTTON_LINK);
//		negExampleButton.setDescription("Click if this pattern is a bad pattern.");
//		negExampleButton.addListener(new Button.ClickListener() {
//			
//			@Override
//			public void buttonClick(ClickEvent event) {
//				fireBadPatternDecision();
//			}
//		});
//		buttons.addComponent(negExampleButton);
//		buttons.setComponentAlignment(posExampleButton, Alignment.MIDDLE_CENTER);
//		buttons.setComponentAlignment(negExampleButton, Alignment.MIDDLE_CENTER);
//		
//		return buttons;
//    }
    
    private static List<Pattern> readPatterns() throws IOException {
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        
        for (String line : FileUtils.readLines(new File(dataPath + "/patterns.txt")) ) {
            
            try {
            
                String[] lineParts = line.split("___");
                Pattern defaultPattern = new Pattern(lineParts[0],lineParts[1],lineParts[2],Integer.valueOf(lineParts[3]), lineParts[4]);
                patterns.add(defaultPattern);
            }
            catch (java.lang.NumberFormatException nfe) {
                
                System.out.println(line);
            }
        }
        
        return patterns;
    }
    
//    private void fireGoodPatternDecision(){
//    	BufferedFileWriter writer = new BufferedFileWriter(dataPath + "good_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
//        writer.write(patternToString(this.pattern));
//        writer.close();
//        
//        patterns.remove(this.pattern);
//        writeTodoPatterns();
//        this.init();
//    }
//    
//    private void fireBadPatternDecision(){
//    	BufferedFileWriter writer = new BufferedFileWriter(dataPath + "bad_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
//        writer.write(patternToString(this.pattern));
//        writer.close();
//        
//        patterns.remove(this.pattern);
//        writeTodoPatterns();
//        this.init();
//    }

    public static String patternToString(Pattern pattern) {
        
        return pattern.entityOne + "___" +
                pattern.nlr + "___" + 
                pattern.entityTwo + "___" +
                pattern.luceneId + "___" +
                pattern.sentence;
    }
    
    /**
     * writes not yet annotated patterns to a text file so, that if you
     * want to annotate later again you dont need to annotate all from the start
     */
    private synchronized void writeTodoPatterns() {
        
        BufferedFileWriter writer = new BufferedFileWriter(dataPath + "patterns.txt", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        for ( Pattern pattern : patterns) 
            writer.write(patternToString(pattern));
        writer.close();
    }
    
    private static class Pattern {
        
        public Pattern(String firstEntity, String nlr, String secondEntity, int luceneId, String sentence) {

            this.entityOne = firstEntity;
            this.entityTwo = secondEntity;
            this.nlr = nlr;
            this.luceneId = luceneId;
            this.sentence = sentence;
        }
        String sentence;
        String entityOne;
        String entityTwo;
        String nlr;
        int luceneId;
    }

    @Override
    public void buttonClick(ClickEvent event) {

        if ( event.getSource().equals(nextButton) ) {
            
            String subjectUri   = "";
            String objectUri    = "";
            
            // no subject uri at all -> need to generate one
            if ( this.getComboBoxUri(subjectCombobox).isEmpty() && ((String) this.subjectUri.getValue()).isEmpty() ) subjectUri = generateUri(this.pattern.entityOne);
            if ( !this.getComboBoxUri(subjectCombobox).isEmpty() && ((String) this.subjectUri.getValue()).isEmpty() ) subjectUri = this.getComboBoxUri(subjectCombobox);
            if ( this.getComboBoxUri(subjectCombobox).isEmpty() && !((String) this.subjectUri.getValue()).isEmpty() ) subjectUri = (String) this.subjectUri.getValue();
            if ( !this.getComboBoxUri(subjectCombobox).isEmpty() && !((String) this.subjectUri.getValue()).isEmpty() ) subjectUri = (String) this.subjectUri.getValue();
            
            String comment  = ((String) this.comment.getValue()).isEmpty() ? "" : (String) this.comment.getValue();
            String said     = ((String) this.saidObject.getValue()).isEmpty() ? "" : (String) this.saidObject.getValue();
            
             // no subject uri at all -> need to generate one
            if ( this.getComboBoxUri(objectCombobox).isEmpty() && ((String) this.objectUri.getValue()).isEmpty() ) objectUri = generateUri(this.pattern.entityTwo);
            if ( !this.getComboBoxUri(objectCombobox).isEmpty() && ((String) this.objectUri.getValue()).isEmpty() ) objectUri = this.getComboBoxUri(objectCombobox);
            if ( this.getComboBoxUri(objectCombobox).isEmpty() && !((String) this.objectUri.getValue()).isEmpty() ) objectUri = (String) this.objectUri.getValue();
            if ( !this.getComboBoxUri(objectCombobox).isEmpty() && !((String) this.objectUri.getValue()).isEmpty() ) objectUri = (String) this.objectUri.getValue();
            
            List<String> output = new ArrayList<String>();
            output.add(said.isEmpty() ? "NORMAL" : "SAY");
            output.add(this.pattern.entityOne);
            output.add(subjectUri);
            output.add(this.pattern.nlr);
            output.add(this.pattern.entityTwo);
            output.add(said.isEmpty() ? objectUri : said);
            output.add(comment);
            output.add(this.pattern.luceneId + "");
            output.add(this.pattern.sentence);
            
            BufferedFileWriter writer = new BufferedFileWriter(dataPath + "patterns_annotated.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
            writer.write(StringUtils.join(output, "___"));
            writer.close();
            
            patterns.remove(this.pattern);
            writeTodoPatterns();
            this.init();
        }
        else if (event.getSource().equals(trashButton)) {
            
            getMainWindow().showNotification("trash");
        }
    }
    
    private String getComboBoxUri(ComboBox combobox) {

        return "";
    }

    private String generateUri(String label){
        
        try {
            
            return RDFLIVENEWS_PREFIX + URLEncoder.encode(label.replace(" ", "_"), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
