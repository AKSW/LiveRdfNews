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
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.rdflivenews.util.BufferedFileWriter;
import org.aksw.simba.rdflivenews.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.apache.commons.io.FileUtils;
import org.rdflivenews.annotator.gui.AutocompleteWidget.SelectionListener;
import org.rdflivenews.annotator.gui.SolrIndex.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.vaadin.ui.Alignment;
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
    
    private final String RDFLIVENEWS_PREFIX = "http://rdflivenews.aksw.org/resource/";
    
    private static Ini config = initConfig();
    private static List<Pattern> patterns = readPatterns();
    Window main = new Window("RdfLiveNews Annotator GUI");
    
    Label subject, object, patternLabel, sentence;
    TextField subjectUri, objectUri;
    TextArea saidObject, comment;
    
    Pattern pattern;
    
    private SolrIndex index = new SolrIndex(config.get("general", "luceneIndex"));
    
    private VerticalLayout mainLayout = new VerticalLayout();

    private NativeButton nextButton, trashButton, stopButton;
    
    private ComboBox clusterCategoriesBox;

    public static Ini initConfig() {
        
        try {
            
            return new Ini(AnnotatorGuiApplication.class.getClassLoader().getResourceAsStream("annotator.ini"));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    

    @Override
    public void init() {
        setMainWindow(main);
        setTheme("mytheme");
        
        if ( !patterns.isEmpty() ) {
            
            mainLayout.removeAllComponents();
            main.removeAllComponents();
         
            this.pattern = patterns.remove(0);
            this.writeTodoPatterns();
            
            GridLayout grid = new GridLayout(4, 6);
            grid.setSpacing(true);
            
            HorizontalLayout labels = new HorizontalLayout();
            labels.setSizeFull();
            
            subject = new Label("<span style=\"font-size:130%\">" + this.pattern.entityOne + "</span>");
            subject.setContentMode(Label.CONTENT_XHTML);
            subject.setSizeFull();
            patternLabel = new Label("<span style=\"font-size:130%;color:red;\">" + this.pattern.nlr + "</span>");
            patternLabel.setContentMode(Label.CONTENT_XHTML);
            patternLabel.setSizeFull();
            patternLabel.setStyleName("center");
            object = new Label("<span style=\"font-size:130%;text-align:right;\">" + this.pattern.entityTwo + "</span>");
            object.setContentMode(Label.CONTENT_XHTML);
            object.setSizeFull();
            object.setStyleName("right");
            
            String sentenceLabel = "<a href=\""+this.pattern.url+"\">"+ "<span style=\"font-size:130%\">" + this.pattern.sentence.
                    replace(this.pattern.entityOne, "<span style=\"font-weight:bold\">" + this.pattern.entityOne + "</span>").
                    replace(this.pattern.entityTwo, "<span style=\"font-weight:bold\">" + this.pattern.entityTwo + "</span>").
                    replace(this.pattern.nlr, "<span style=\"color:red\">" + this.pattern.nlr + "</span>") + "</span></a>";
            
            sentence =  new Label(sentenceLabel);
            sentence.setContentMode(Label.CONTENT_XHTML);
            
            grid.addComponent(sentence, 0, 0, 3, 0);
            labels.addComponent(subject);
            labels.addComponent(patternLabel);
            labels.addComponent(object);
//            grid.addComponent(subject, 0, 1);
//            grid.addComponent(patternLabel, 1, 1, 2, 1);
//            grid.addComponent(object, 3, 1);
            labels.setComponentAlignment(subject, Alignment.MIDDLE_LEFT);
            labels.setComponentAlignment(patternLabel, Alignment.MIDDLE_CENTER);
            labels.setComponentAlignment(object, Alignment.MIDDLE_RIGHT);
            grid.addComponent(labels, 0, 1, 3, 1);
            
            AutocompleteWidget subject = new AutocompleteWidget(index);
            subject.addSelectionListener(new SelectionListener() {
				
				@Override
				public void itemSelected(SolrItem item) {
					subjectUri.setValue(item.getUri());
				}
			});
            grid.addComponent(subject, 0, 2, 1, 2);
            AutocompleteWidget object = new AutocompleteWidget(index);
            object.addSelectionListener(new SelectionListener() {
				
				@Override
				public void itemSelected(SolrItem item) {
					objectUri.setValue(item.getUri());
				}
			});
            grid.addComponent(object, 2, 2 , 3, 2);
            
            saidObject = new TextArea("Say Cluster Object Value");
            saidObject.setWidth("100%");
            grid.addComponent(saidObject, 0, 5, 1, 5);
            
            comment = new TextArea("Comments");
            comment.setWidth("100%");
            grid.addComponent(comment, 2, 5, 3, 5);
            
            HorizontalLayout urisAndCluster = new HorizontalLayout();
            subjectUri = new TextField("Subject URI");
            objectUri = new TextField("Object URI");
            subjectUri.setSizeFull();
            objectUri.setSizeFull();
            
            //cluster category combobox
            clusterCategoriesBox = new ComboBox();
            clusterCategoriesBox.setWidth("40%");
            clusterCategoriesBox.setCaption("Cluster Category");
            clusterCategoriesBox.addContainerProperty("name", String.class, null);
            clusterCategoriesBox.setItemCaptionMode(ComboBox.ITEM_CAPTION_MODE_ITEM);
            for(ClusterCategory cat : ClusterCategory.values()){
                clusterCategoriesBox.addItem(cat).getItemProperty("name").setValue(cat.getName());
            }
            clusterCategoriesBox.setImmediate(true);
            clusterCategoriesBox.setValue(ClusterCategory.UNKNOWN);
            clusterCategoriesBox.setNullSelectionAllowed(false);
            
            urisAndCluster.setSizeFull();
            urisAndCluster.addComponent(subjectUri);
            urisAndCluster.addComponent(clusterCategoriesBox);
            urisAndCluster.addComponent(objectUri);
            urisAndCluster.setComponentAlignment(subjectUri, Alignment.MIDDLE_LEFT);
            urisAndCluster.setComponentAlignment(clusterCategoriesBox, Alignment.MIDDLE_CENTER);
            urisAndCluster.setComponentAlignment(objectUri, Alignment.MIDDLE_RIGHT);
            
            grid.addComponent(urisAndCluster, 0, 3, 3, 3);
            
//            grid.addComponent(clusterCategoriesBox, 1, 3, 2, 3);
//            grid.setComponentAlignment(clusterCategoriesBox, Alignment.BOTTOM_CENTER);
            
            mainLayout.addComponent(grid);
            
            HorizontalLayout submitButtonslayout = new HorizontalLayout();
            
            nextButton = new NativeButton("Next");
            trashButton = new NativeButton("Trash");
            stopButton = new NativeButton("Stop");
            nextButton.addListener(this);
            trashButton.addListener(this);
            stopButton.addListener(this);
            submitButtonslayout.setSpacing(true);
            submitButtonslayout.setWidth("100%");
            submitButtonslayout.addComponent(trashButton);
            submitButtonslayout.addComponent(stopButton);
            submitButtonslayout.addComponent(nextButton);
            submitButtonslayout.setComponentAlignment(nextButton, Alignment.MIDDLE_RIGHT);
            submitButtonslayout.setComponentAlignment(stopButton, Alignment.MIDDLE_RIGHT);
            
            mainLayout.addComponent(submitButtonslayout);
            mainLayout.setSpacing(true);
            mainLayout.setWidth(null);
            mainLayout.setHeight("100%");
            Panel panel = new Panel();
            panel.setWidth(null);
            panel.addComponent(mainLayout);
            
            main.addComponent(panel);
            ((VerticalLayout)main.getContent()).setHeight("100%");
            ((VerticalLayout)main.getContent()).setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
            
            //force URI search with given labels
            subject.setSearchTerm(this.pattern.entityOne);
            object.setSearchTerm(this.pattern.entityTwo);
        }
        else getMainWindow().showNotification("No patterns anymore...");
        
    }
    
    /**
     * 
     * @return
     * @throws IOException
     */
    private static synchronized List<Pattern> readPatterns() {
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        
        // copy the file so that we dont need to recopy this over and over again
        File file = new File(config.get("general", "dataDirectory") + config.get("general", "patternFileName"));
        File newFile = new File(config.get("general", "dataDirectory") + config.get("general", "patternFileName").replace(".txt", "_todo.txt"));
        if (!newFile.exists())
            try {
                FileUtils.copyFile(file, newFile);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        int i = 1;
		try {
            for (String line : FileUtils.readLines(newFile, "UTF-8") ) {
                
                i++;
                try {
                
                    String[] lineParts = line.split("___");
                    Pattern defaultPattern = new Pattern(lineParts[0],lineParts[1],lineParts[2],Integer.valueOf(lineParts[3]), lineParts[4], lineParts[5]);
                    patterns.add(defaultPattern);
                }
                catch (java.lang.ArrayIndexOutOfBoundsException nfe) {
                    
                    System.out.println(i + ": " + line);
                }
                catch (java.lang.NumberFormatException nfe) {
                    
                    System.out.println(line);
                }
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return patterns;
    }

    /**
     * 
     * @param pattern
     * @return
     */
    public static String patternToString(Pattern pattern) {
        
        return pattern.entityOne + "___" +
                pattern.nlr + "___" + 
                pattern.entityTwo + "___" +
                pattern.luceneId + "___" +
                pattern.sentence + "___" +
                pattern.url;
    }
    
    /**
     * writes not yet annotated patterns to a text file so, that if you
     * want to annotate later again you dont need to annotate all from the start
     */
    private synchronized void writeTodoPatterns() {
        
        BufferedFileWriter writer = new BufferedFileWriter(config.get("general", "dataDirectory") + config.get("general", "patternFileName").replace(".txt", "_todo.txt"), "UTF-8", WRITER_WRITE_MODE.OVERRIDE);
        for ( Pattern pattern : patterns) 
            writer.write(patternToString(pattern));
        writer.close();
    }
    
    private static class Pattern {
        
        public Pattern(String firstEntity, String nlr, String secondEntity, int luceneId, String sentence, String url) {

            this.entityOne = firstEntity;
            this.entityTwo = secondEntity;
            this.nlr = nlr;
            this.luceneId = luceneId;
            this.sentence = sentence;
            this.url       = url;
        }
        String sentence;
        String entityOne;
        String entityTwo;
        String nlr;
        String url;
        int luceneId;
    }

    @Override
    public synchronized void buttonClick(ClickEvent event) {

        if ( event.getSource().equals(nextButton) || event.getSource().equals(stopButton) ) {
            
            String subjectUri   = "";
            String objectUri    = "";
            
            // no subject uri at all -> need to generate one
            if ( this.subjectUri.getValue() == null || ((String) this.subjectUri.getValue()).isEmpty() ) {
            	subjectUri = generateUri(this.pattern.entityOne);
            } else {
            	subjectUri = (String) this.subjectUri.getValue();
            	if (!subjectUri.startsWith("http://")) subjectUri = generateUri(subjectUri);
            	subjectUri = subjectUri.replace("http://en.wikipedia.org/wiki/", RDFLIVENEWS_PREFIX);
            }
            
            // no object uri at all -> need to generate one
            if ( this.objectUri.getValue() == null || ((String) this.objectUri.getValue()).isEmpty() ) {
            	objectUri = generateUri(this.pattern.entityTwo);
            } else {
            	objectUri = (String) this.objectUri.getValue();
            	if (!objectUri.startsWith("http://")) objectUri = generateUri(objectUri);
            	objectUri = objectUri.replace("http://en.wikipedia.org/wiki/", RDFLIVENEWS_PREFIX);
            }
            
            String comment  = ((String) this.comment.getValue()).isEmpty() ? "" : (String) this.comment.getValue();
            String said     = ((String) this.saidObject.getValue()).isEmpty() ? "" : (String) this.saidObject.getValue();
            
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
            output.add(this.clusterCategoriesBox.getValue().toString());
            output.add(this.pattern.url);
            
            BufferedFileWriter writer = new BufferedFileWriter(config.get("general", "dataDirectory")  + "patterns_annotated.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
            writer.write(StringUtils.join(output, "___"));
            writer.close();
            
            if ( !event.getSource().equals(stopButton) ) this.init();
            else this.getMainWindow().showNotification("Thank You! Session ended!");
        }
        else if (event.getSource().equals(trashButton)) {
            
            BufferedFileWriter writer = new BufferedFileWriter(config.get("general", "dataDirectory")  + "patterns_trash.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
            writer.write(patternToString(this.pattern));
            writer.close();
            this.init();
        }
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
