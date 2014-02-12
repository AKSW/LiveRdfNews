/**
 * 
 */
package org.aksw.simba.rdflivenews.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class JenaUtil {

    /**
     * 
     * @param filename
     * @return
     */
    public static OntModel loadModelFromFile(String filename) {

        try {

        	OntModel ontologyModel = ModelFactory.createOntologyModel();
            InputStream in = FileManager.get().open(filename);
            ontologyModel.read(in, "");
            in.close();

            return ontologyModel;
        }
        catch (IOException e) {

            throw new RuntimeException("Could not load model from file: " + filename, e);
        }
    }

    /**
     * 
     * @param pathToFile
     * @param syntax
     * @param base
     * @param model
     */
    public static void writeModelToFile(String pathToFile, String syntax, String base, OntModel model) {

        try {
            
            OutputStream out = new FileOutputStream(pathToFile);
            model.writeAll(out, syntax, base);
            out.close();
        }
        catch (Exception e) {
            
            throw new RuntimeException("Could not write model to file: " + pathToFile, e);
        }
    }

    public static void writeModelToRemoteStore() {

        // TODO Auto-generated method stub
        
    }
}
