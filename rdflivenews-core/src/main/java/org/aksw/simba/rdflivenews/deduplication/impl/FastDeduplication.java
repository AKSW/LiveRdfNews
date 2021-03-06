/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.deduplication.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.deduplication.tokenization.impl.WordTokenizer;
import org.aksw.simba.rdflivenews.index.IndexManager;

/**
 *
 * @author ngonga
 */
public class FastDeduplication extends DefaultDeduplication {

    private Map<String, Integer> ids;
    private double threshold;
    private Map<Integer, Set<Integer>> clones;

    /**
     *
     */
    public FastDeduplication() {

        this.threshold = RdfLiveNews.CONFIG.getDoubleSetting("deduplication", "threshold");
        this.ids = new HashMap<String, Integer>();
        this.clones = new HashMap<Integer, Set<Integer>>();
    }

    /**
     * Retrieves all the source documents for the deduplication
     *
     * @param fromTimeSlice Highest time slice
     * @param window Window for wish duplicates are to be considered
     */
    public Set<String> getSource() {
        if (this.windowSize <= 0)   throw new IllegalArgumentException("Time Slice Window cant be less then 1: " + this.windowSize);
        if (this.fromTimeSlice < 0) throw new IllegalArgumentException("From Time Slice needs to be greater or equal than 0: " + fromTimeSlice);

        IndexManager manager = IndexManager.getInstance();
        Set<String> source = new HashSet<String>();

        for (int timeslice = this.fromTimeSlice - this.windowSize; timeslice <= this.fromTimeSlice; timeslice++) {
         
            // non duplicate sentences are marked with -1,
            // so if we allow timeslice ids smaller than 0 
            // we will mix them up with the normal sentences
            if ( timeslice < 0 ) continue; 
            
            for ( Map.Entry<Integer, String> entry : manager.getIdsAndTextFromTimeSlice(timeslice).entrySet() ) {
             
                Integer id  = entry.getKey();
                String text = entry.getValue();
                
                if (ids.keySet().contains(text)) {
                    
                    int masterId = ids.get(text);
                    if (!clones.containsKey(masterId)) clones.put(masterId, new HashSet<Integer>());
                    clones.get(masterId).add(id);
                }
                else {
                    
                    ids.put(text, id);
                    source.add(text);
                }
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
    public Set<String> getTarget() {
        if (fromTimeSlice < 0) throw new IllegalArgumentException("From Time Slice cant be less then 0: " + fromTimeSlice);
        if (fromTimeSlice > toTimeSlice) throw new IllegalArgumentException("To Time Slice " + toTimeSlice + " needs to be bigger than From Time Slice " + fromTimeSlice);

        Set<String> target = new HashSet<String>();
        IndexManager manager = IndexManager.getInstance();

        // target should not include current source
        for (int timeSlice = this.fromTimeSlice + 1; timeSlice <= toTimeSlice; timeSlice++) { 
            // get all ids and sentences in batch mode
            for ( Map.Entry<Integer, String> entry : manager.getIdsAndTextFromTimeSlice(timeSlice).entrySet() ) { 
                
                Integer id  = entry.getKey();
                String text = entry.getValue();
                
                if (ids.keySet().contains(text)) {
                    
                    int masterId = ids.get(text);
                    if (!clones.containsKey(masterId)) clones.put(masterId, new HashSet<Integer>());
                    clones.get(masterId).add(id);
                } 
                else {
                    
                    ids.put(text, id);
                    target.add(text);
                }
            }
        }

        return target;
    }

    /**
     * Returns the subset of target that are duplicates of elements of source.
     * Note that trivial duplicates are not returned. If they were then
     * deduplicating identical source and target would lead to all documents
     * from these data sets being marked as duplicates
     *
     * @param source Set of source documents
     * @param target Set of target documents
     * @param timeSlice Current time slice for marking deleted documents
     * @return Set of duplicates
     */
    @Override
    public Set<String> deduplicate(Set<String> source, Set<String> target) {

        Set<String> duplicates = new HashSet<String>();
        Map<String, Map<String, Double>> result = FastNGram.compute(source, target, new WordTokenizer(), threshold);
        result = removeSymmetry(result);
        int sourceDocID, targetDocID;
        for (String sourceDoc : result.keySet()) {
            sourceDocID = ids.get(sourceDoc);
            // tag very similarity documents as being duplicates
            for (String targetDoc : result.get(sourceDoc).keySet()) {
                targetDocID = ids.get(targetDoc);
                if (sourceDocID != targetDocID) {
                    duplicates.add(targetDoc);
                    this.duplicateIds.add(targetDocID);
                }
            }
        }
        return duplicates;
    }

    /**
     * Ensure that symmetric results are removed from deduplication. Ergo, if a
     * deduplication contains a -> b and b -> a, then b -> a is removed. Needed
     * to ensure that only b is marked as duplicate (and not a and b)
     *
     * @param map
     * @return
     */
    private static Map<String, Map<String, Double>> removeSymmetry(Map<String, Map<String, Double>> map) {

        Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();

        for (String sourceDoc : map.keySet()) {
            for (String targetDoc : map.get(sourceDoc).keySet()) {
                if (!sourceDoc.equals(targetDoc)) {
                    if (result.containsKey(targetDoc)) {
                        if (!result.get(targetDoc).containsKey(sourceDoc)) {
                            //found no b -> a to a -> b in result with a!=b
                            if (!result.containsKey(sourceDoc)) {
                                result.put(sourceDoc, new HashMap<String, Double>());
                            }
                            result.get(sourceDoc).put(targetDoc, map.get(sourceDoc).get(targetDoc));
                        }
                    } else {
                        if (!result.containsKey(sourceDoc)) {
                            result.put(sourceDoc, new HashMap<String, Double>());
                        }
                        result.get(sourceDoc).put(targetDoc, map.get(sourceDoc).get(targetDoc));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 
     */
    public void deduplicateClones() {
        
        for (Integer masterId : clones.keySet()) 
            for (Integer clone : clones.get(masterId)) 
                this.duplicateIds.add(clone.intValue());
        
        IndexManager.getInstance().setDocumentsDuplicateInTimeSlice(this.duplicateIds, this.toTimeSlice);
    }

    public static void testRemoveSymmetry() {
        Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();
        Map<String, Double> value;
        value = new HashMap<String, Double>();
        value.put("B", 1d);
        value.put("C", 1d);
        value.put("D", 1d);
        map.put("A", value);
        value = new HashMap<String, Double>();
        value.put("A", 1d);
        value.put("C", 1d);
        value.put("D", 1d);
        map.put("B", value);

        System.out.println(map);
        System.out.println(removeSymmetry(map));
    }

    public static void main(String args[]) {
        testRemoveSymmetry();
    }
}
