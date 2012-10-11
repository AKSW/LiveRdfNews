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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.maven.MavenUtil;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

/**
 * Demonstration application that shows how to use a simple custom client-side
 * GWT component, the ColorPicker.
 */
@SuppressWarnings("serial")
public class AnnotatorGuiApplication extends com.vaadin.Application implements Button.ClickListener {
    
    static String dataPath = "/home/gerber/boa-data/rdflivenews/evaluation/";
//    static String dataPath = "/Users/gerb/test/annotation/evaluation/";
    
    private static List<Pattern> patterns;
    Window main = new Window("RdfLiveNews Annotator GUI");
    
    Label subject, object, patternLabel;
    Pattern pattern;
    Button goodPatternButton = new NativeButton("Good");
    Button badPatternButton = new NativeButton("Bad");
    
    HorizontalLayout layoutPattern, layoutButtons;

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
            
            subject = new Label(this.pattern.entityOne);
            subject.setContentMode(Label.CONTENT_XHTML);
            patternLabel = new Label("<b>" + this.pattern.nlr + "</b>");
            object = new Label(this.pattern.entityTwo);
            object.setContentMode(Label.CONTENT_XHTML);
            
            layoutPattern = new HorizontalLayout();
            layoutPattern.setSpacing(true);
            layoutPattern.addComponent(subject);
            layoutPattern.addComponent(patternLabel);
            layoutPattern.addComponent(object);
            
            Panel panel = new Panel();
            
            layoutButtons = new HorizontalLayout();
            layoutButtons.setSpacing(true);
            goodPatternButton.addListener(this);
            badPatternButton.addListener(this);
            layoutButtons.addComponent(goodPatternButton);
            layoutButtons.addComponent(badPatternButton);
            
            panel.addComponent(layoutPattern);
            panel.addComponent(layoutButtons);
            
            main.addComponent(panel);
        }
        else getMainWindow().showNotification("No patterns anymore...");
        
    }
    
    private static List<Pattern> readPatterns() throws IOException {
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        
        for (String line : FileUtils.readLines(new File(dataPath +"patterns.txt")) ) {
            
            try {
            
                String[] lineParts = line.split("___");
                Pattern defaultPattern = new Pattern(lineParts[0],lineParts[1],lineParts[2],Integer.valueOf(lineParts[3]));
                patterns.add(defaultPattern);
            }
            catch (java.lang.NumberFormatException nfe) {
                
                System.out.println(line);
            }
        }
        
        return patterns;
    }

    public void buttonClick(ClickEvent event) {

        if ( event.getButton().equals(badPatternButton) ) {
            
            BufferedFileWriter writer = new BufferedFileWriter(dataPath + "bad_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
            writer.write(patternToString(this.pattern));
            writer.close();
            
            patterns.remove(this.pattern);
            writeTodoPatterns();
            this.init();
        }
        if ( event.getButton().equals(goodPatternButton) ) {
            
            BufferedFileWriter writer = new BufferedFileWriter(dataPath + "good_patterns.txt", "UTF-8", WRITER_WRITE_MODE.APPEND);
            writer.write(patternToString(this.pattern));
            writer.close();
            
            patterns.remove(this.pattern);
            writeTodoPatterns();
            this.init();
        }
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
