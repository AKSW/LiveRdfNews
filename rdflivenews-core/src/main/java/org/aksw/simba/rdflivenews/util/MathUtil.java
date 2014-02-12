package org.aksw.simba.rdflivenews.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MathUtil {

	/**
	 * Returns the average value of all entries in the provided list.
	 * The value is not rounded. If the list is empty or null the 
	 * returned value is null.
	 * 
	 * @param list - the list to search for an average
	 * @return the average value
	 */
	public static <T extends Number> Double getAverage(Collection<T> list) {
		
		// we can't create an average in those cases
		if ( list == null || list.size() == 0 ) return -1D;
		
		// add all up
		double average = 0;
		for ( T listEntry : list ) average += listEntry.doubleValue(); 
		// and divide them by the list size
		return average / (double) list.size();
	}
	
	/**
	 * Returns the maximum value of all entries in the provided list.
	 * If the list is empty or null the returned value is null.
	 * 
	 * @param list - the list to search for an maximum
	 * @return the maximum value
	 */
	public static <T extends Number> Double getMax(Collection<T> list) {
		
		// we can't create a maximum in those cases
		if ( list == null || list.size() == 0 ) return -1D;
		
		Double maximum = 0D;
		for ( T listEntry : list ) maximum = Math.max(maximum, listEntry.doubleValue()); 
		return maximum;
	}
	
	/**
	 * Returns the minimum value of all entries in the provided list.
	 * If the list is empty or null the returned value is null.
	 * 
	 * @param list - the list to search for an minimum
	 * @return the minimum value
	 */
	public static <T extends Number> Double getMin(Collection<T> list) {
		
		// we can't create a minimum in those cases
		if ( list == null || list.size() == 0 ) return -1D;
		
		Double minimum = 0D;
		for ( T listEntry : list ) minimum = Math.min(minimum, listEntry.doubleValue()); 
		return minimum;
	}
	
	/**
	 * Generates a set of random numbers in a given interval.
	 * The interval is for the minimum as well as the maximum
	 * always inclusive. The generation only works for Double.class
	 * and Integer.class. Other Numbers will throw a RuntimeException.
	 * If rangeMax - rangeMin < size and the type is Integer
	 * an IllegalArgumentException is thrown.
	 * 
	 * @param size - the size of the returned random number set
	 * @param intervalStart - the start of range 
	 * @param intervalEnd - the end of the range
	 * @return returns a set 
	 */
	public static <T extends Number> Set<T> getFixedSetOfFixedNumbers(int size, Class<T> clazz, int rangeMin, int rangeMax){
	    
	    if ( rangeMax - rangeMin < size && clazz == Integer.class ) throw new IllegalArgumentException("Can not generate more random integers then the range is big!");
	    
	    Set<T> randomNumbers = new HashSet<T>();
	    Random random = new Random();
	    
	    while ( randomNumbers.size() < size ) {
	        
	        if ( clazz == Double.class ) {
	            
	            randomNumbers.add((T) new Double(rangeMin + (rangeMax - rangeMin) * random.nextDouble()));
	        }
	        else if ( clazz == Integer.class ) {
	            
	            randomNumbers.add((T) new Integer(rangeMin + (int)((rangeMax - rangeMin + 1) * Math.random())));
	        }
	        else {

	            throw new RuntimeException("The given class " + clazz.getSimpleName() + " is not supported!");
	        }
	    }
	    
	    return randomNumbers;
	}
	
	/**
	 * Generates a set of random numbers in a given interval.
	 * The interval is for the minimum as well as the maximum
	 * always inclusive. The generation only works for Double.class
	 * and Integer.class. Other Numbers will throw a RuntimeException.
	 * If rangeMax - rangeMin < size and the type is Integer
	 * an IllegalArgumentException is thrown.
	 * 
	 * @param size - the size of the returned random number set
	 * @param intervalStart - the start of range 
	 * @param intervalEnd - the end of the range
	 * @return returns a set 
	 */
	public static <T extends Number> List<T> getFixedSetOfRandomNumbers(int size, Class<T> clazz, int rangeMin, int rangeMax){
	    
	    List<T> randomNumbers = new ArrayList<T>();
	    Random random = new Random();
	    
	    while ( randomNumbers.size() < size ) {
	        
	        if ( clazz == Double.class ) {
	            
	            randomNumbers.add((T) new Double(rangeMin + (rangeMax - rangeMin) * random.nextDouble()));
	        }
	        else if ( clazz == Integer.class ) {
	            
	            randomNumbers.add((T) new Integer(rangeMin + (int)((rangeMax - rangeMin + 1) * Math.random())));
	        }
	        else {

	            throw new RuntimeException("The given class " + clazz.getSimpleName() + " is not supported!");
	        }
	    }
	    
	    return randomNumbers;
	}
	
	public static void main(String[] args) {

        System.out.println(getFixedSetOfFixedNumbers(10, Integer.class, 0, 10));
        System.out.println(getFixedSetOfFixedNumbers(10, Double.class, 0, 10));
    }
}
