/**
 * 
 */
package org.aksw.pattern;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface Pattern {

    public void setNaturalLanguageRepresentation(String patternString);

    public void setNaturalLanguageRepresentationWithTags(String patternStringWithTags);
 
    public void setArgumentOne(String string);
    
    public void setArgumentTwo(String string);

    public void setArgumentTwoType(String substring);

    public void setArgumentOneType(String substring);

    public String getArgumentTwo();

    public String getNaturalLanguageRepresentation();
}
