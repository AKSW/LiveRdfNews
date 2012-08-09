package org.aksw.simba.rdflivenews.index;

import java.util.Date;
import java.util.Set;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Sentence {
    
    private Date extractionDate;
    private int timeSliceID;
    private String articleUrl;
    private String text = "";
    private String ner = "";
    private String pos = "";
    
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
     * @return the ner
     */
    public String getNerTaggedSentence() {
    
        return ner;
    }
    
    /**
     * @param ner the ner to set
     */
    public void setNerTaggedSentence(String nerTaggedSentence) {
    
        this.ner = nerTaggedSentence;
    }
    
    /**
     * @return the pos
     */
    public String getPosTaggedSentence() {
    
        return pos;
    }

    /**
     * @param pos the pos to set
     */
    public void setPosTaggedSentence(String posTaggedSentence) {
    
        this.pos = posTaggedSentence;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
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
        Sentence other = (Sentence) obj;
        if (text == null) {
            if (other.text != null)
                return false;
        }
        else
            if (!text.equals(other.text))
                return false;
        return true;
    }
}
