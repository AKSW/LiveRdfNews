/**
 * 
 */
package org.aksw.pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefaultPattern implements Pattern {

    private String naturalLanguageRepresentation;
    private String naturalLanguageRepresentationWithTags;
    private String argumentOne;
    private String argumentTwo;
    private String argumentOneType;
    private String argumentTwoType;
    
    public DefaultPattern(String patternString) {

        this.naturalLanguageRepresentation = patternString;
    }

    public DefaultPattern() {

        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("NLR: " +  naturalLanguageRepresentation);
        builder.append("\nNLR-T: " + naturalLanguageRepresentationWithTags);
        builder.append("\nARG1: " + argumentOne +"_"+ argumentOneType);
        builder.append("\nARG2: " + argumentTwo +"_"+ argumentTwoType);
        return builder.toString();
    }

    /**
     * 
     */
    public void setNaturalLanguageRepresentation(String patternString) {

        this.naturalLanguageRepresentation = patternString;
    }

    /**
     * 
     */
    public void setNaturalLanguageRepresentationWithTags(String patternStringWithTags) {

        this.naturalLanguageRepresentationWithTags = patternStringWithTags;
    }

    public void setArgumentOne(String argumentOne) {

        this.argumentOne = argumentOne;
    }

    public void setArgumentTwo(String argumentTwo) {

        this.argumentTwo = argumentTwo; 
    }

    public void setArgumentTwoType(String argumentTwoType) {

        this.argumentTwoType = argumentTwoType;
    }

    public void setArgumentOneType(String argumentOneType) {

        this.argumentOneType = argumentOneType;
    }

    public String getArgumentOne() {

        return this.argumentOne;
    }
    
    public String getArgumentTwo() {

        return this.argumentTwo;
    }

    public String getNaturalLanguageRepresentation() {

        return this.naturalLanguageRepresentation;
    }
}
