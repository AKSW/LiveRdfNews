/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.*;
import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.deduplication.Deduplication;
import org.aksw.simba.rdflivenews.index.IndexManager;

/**
 *
 * @author ngonga
 */
public class FastDeduplication implements Deduplication {

    private Map<String, Integer> ids;
    private double threshold;
    private int window;

    /**
     * 
     */
    public FastDeduplication() {

        this.threshold = RdfLiveNews.CONFIG.getDoubleSetting("deduplication", "threshold");
        this.window =    RdfLiveNews.CONFIG.getIntegerSetting("deduplication", "window");
    }

    /**
     * Runs the deduplication process and marks duplicate documents as such
     *
     * @param fromTimeSlice
     * @param toTimeSlice
     */
    public void runDeduplication(int fromTimeSlice, int toTimeSlice) {
        //1. load index of all data before fromFrame
        ids = new HashMap<String, Integer>();
        Set<String> source = getSource(fromTimeSlice, window);
        Set<String> target = getTarget(fromTimeSlice, toTimeSlice);
        //2. deduplicate & delete duplicates for the current time slices, i.e., target
        Set<String> duplicates = deduplicate(target, target, fromTimeSlice);
        for (String duplicate : duplicates) {
            target.remove(duplicate);
        }
        //3. deduplicate & delete duplicates for the old and new data
        deduplicate(source, target, fromTimeSlice);
    }

    /**
     * Retrieves all the source documents for the deduplication
     *
     * @param fromTimeSlice Highest time slice
     * @param window Window for wish duplicates are to be considered
     */
    private Set<String> getSource(int fromTimeSlice, int window) {
        IndexManager manager = IndexManager.getInstance();
        Set<String> source = new HashSet<String>();
        for (int i = fromTimeSlice - window; i <= fromTimeSlice; i++) {
            List<Integer> currentIds = manager.getSentenceFromTimeSlice(i);
            for (int id : currentIds) {
                String doc = manager.getStringValueFromDocument(id, Constants.LUCENE_FIELD_TEXT);
                ids.put(doc, id);
                source.add(doc);
            }
        }
        return source;
    }

    /**
     * Retrieves all the target documents for the deduplication
     *
     * @param fromTimeSlice Lowest time slice id of target documents
     * @param toTimeSlice Highest time slice id of target documents
     */
    private Set<String> getTarget(int fromTimeSlice, int toTimeSlice) {
        Set<String> target = new HashSet<String>();
        
        IndexManager manager = IndexManager.getInstance();
        for (int i = fromTimeSlice; i <= toTimeSlice; i++) {
            List<Integer> currentIds = manager.getSentenceFromTimeSlice(i);
            for (int id : currentIds) {
                String doc = manager.getStringValueFromDocument(id, Constants.LUCENE_FIELD_TEXT);
                ids.put(doc, id);
                target.add(doc);
            }
        }
        return target;
    }

    /**
     * Returns the subset of target that are duplicates of elements of source
     *
     * @param source Set of source documents
     * @param target Set of target documents
     * @param fromTimeSlice Current time slice for marking deleted documents
     * @return Set of duplicates
     */
    private Set<String> deduplicate(Set<String> source, Set<String> target, int fromTimeSlice) {
        IndexManager manager = IndexManager.getInstance();
        Set<String> duplicates = new HashSet<String>();
        Map<String, Map<String, Double>> result = FastNGram.compute(source, target, 0, threshold);
        for (String key : result.keySet()) {
            for (String doc : result.get(key).keySet()) {
                int id = ids.get(doc);
                duplicates.add(doc);
                manager.setDocumentDuplicateInTimeSlice(id, fromTimeSlice);
            }
        }
        return duplicates;
    }
}