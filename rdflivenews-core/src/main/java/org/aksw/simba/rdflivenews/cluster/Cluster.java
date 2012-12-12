/**
 * 
 */
package org.aksw.simba.rdflivenews.cluster;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Cluster<T> implements Iterable<T>, Set<T> {

    private Set<T> cluster;
    private String name = "";
    private String uri = "";
    private String rdfsRange = "";
    private String rdfsDomain = "";
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cluster other = (Cluster) obj;
        if (cluster == null) {
            if (other.cluster != null)
                return false;
        }
        else
            if (!cluster.equals(other.cluster))
                return false;
        return true;
    }


    public Cluster(){
        
        this.cluster = new HashSet<T>();
    }
    
    
    /**
     * @return the name
     */
    public String getName() {
    
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
    
        this.name = name;
    }

    
    /**
     * @return the uri
     */
    public String getUri() {
    
        return uri;
    }

    
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
    
        this.uri = uri;
    }

    @Override
    public boolean add(T entry) {

        return this.cluster.add(entry);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {

        return this.cluster.addAll(c);
    }

    @Override
    public void clear() {

        this.cluster.clear();
    }

    @Override
    public boolean contains(Object o) {

        return this.cluster.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {

        return this.cluster.containsAll(c);
    }

    @Override
    public boolean isEmpty() {

        return this.cluster.isEmpty();
    }

    @Override
    public boolean remove(Object o) {

        return this.cluster.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {

        return this.cluster.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {

        return this.cluster.retainAll(c);
    }

    @Override
    public int size() {

        return this.cluster.size();
    }

    @Override
    public Object[] toArray() {

        return this.cluster.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        return this.cluster.toArray(a);
    }

    @Override
    public Iterator<T> iterator() {

        return this.cluster.iterator();
    }


    public void setRdfsRange(String range) {

        this.rdfsRange = range; 
    }


    public void setRdfsDomain(String domain) {

        this.rdfsDomain = domain;
    }
    
    /**
     * @return the rdfsRange
     */
    public String getRdfsRange() {
    
        return rdfsRange;
    }
    
    /**
     * @return the rdfsDomain
     */
    public String getRdfsDomain() {
    
        return rdfsDomain;
    }
}
