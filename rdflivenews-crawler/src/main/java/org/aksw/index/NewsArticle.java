package org.aksw.index;

import java.util.Date;
import java.util.Set;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NewsArticle {
    
    private Date extractionDate;
    private int timeSliceID;
    private String imageUrl;
    private String articleUrl;
    private String html;
    private String title;
    private String text;
    private Set<String> keywords;
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((articleUrl == null) ? 0 : articleUrl.hashCode());
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
        NewsArticle other = (NewsArticle) obj;
        if (articleUrl == null) {
            if (other.articleUrl != null)
                return false;
        }
        else
            if (!articleUrl.equals(other.articleUrl))
                return false;
        return true;
    }

    /**
     * @return the extractionDate
     */
    public Date getExtractionDate() {
    
        return extractionDate;
    }

    
    /**
     * @param extractionDate the extractionDate to set
     */
    public void setExtractionDate(Date extractionDate) {
    
        this.extractionDate = extractionDate;
    }

    
    /**
     * @return the timeSliceID
     */
    public int getTimeSliceID() {
    
        return timeSliceID;
    }

    
    /**
     * @param timeSliceID the timeSliceID to set
     */
    public void setTimeSliceID(int timeSliceID) {
    
        this.timeSliceID = timeSliceID;
    }

    
    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
    
        return imageUrl;
    }

    
    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
    
        this.imageUrl = imageUrl;
    }

    
    /**
     * @return the articleUrl
     */
    public String getArticleUrl() {
    
        return articleUrl;
    }

    
    /**
     * @param articleUrl the articleUrl to set
     */
    public void setArticleUrl(String articleUrl) {
    
        this.articleUrl = articleUrl;
    }

    
    /**
     * @return the html
     */
    public String getHtml() {
    
        return html;
    }

    
    /**
     * @param html the html to set
     */
    public void setHtml(String html) {
    
        this.html = html;
    }

    
    /**
     * @return the text
     */
    public String getText() {
    
        return text;
    }

    
    /**
     * @param text the text to set
     */
    public void setText(String text) {
    
        this.text = text;
    }


    /**
     * @return the title
     */
    public String getTitle() {

        return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {

        this.title = title;
    }


    /**
     * @return the keywords
     */
    public Set<String> getKeywords() {

        return keywords;
    }


    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(Set<String> keywords) {

        this.keywords = keywords;
    }
}
