package org.aksw.index;

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class IndexManager {

    private static IndexManager INSTANCE;
    
    private Set<String> urls = new HashSet<String>();

    /**
     * 
     * @return
     */
    public static IndexManager getInstance() {

        if ( IndexManager.INSTANCE == null ) IndexManager.INSTANCE = new IndexManager();
        return IndexManager.INSTANCE;
    }
    
    private IndexManager() {
        
        
    }

    /**
     * 
     */
    public void createIndex() {
        
    }
    
    /**
     * 
     */
    public void closeIndex() {
        
    }
    
    /**
     * 
     * @param uri 
     * @return
     */
    public boolean isNewArticle(String uri) {
     
        return true;
    }
    
    /**
     * 
     * @param article
     */
    public void addNewsArticle(NewsArticle article) {
        
        if ( !urls.contains(article.getArticleUrl() )) {
            
            System.out.println(article.getTitle());
            System.out.println(article.getArticleUrl());
            urls.add(article.getArticleUrl());
        }
    }
    
    /**
     * 
     * @param article
     */
    public void addNewsArticles(Set<NewsArticle> article) {
        
        
    }
    
    /**
     * 
     * @param article
     * @return
     */
    private Document articleToDocument(NewsArticle article) {
        
        return null;
    }
    
    /**
     * 
     * @param article
     * @return
     */
    private Set<Document> articlesToDocuments(Set<NewsArticle> articles) {
        
        Set<Document> documents = new HashSet<Document>();
        
        for ( NewsArticle article : articles) 
            documents.add(articleToDocument(article));
        
        return documents;
    }
}
