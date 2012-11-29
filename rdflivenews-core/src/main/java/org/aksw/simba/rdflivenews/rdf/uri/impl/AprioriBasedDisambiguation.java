/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.rdflivenews.rdf.uri.impl;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.rdf.uri.UriRetrieval;

import com.github.gerbsen.encoding.Encoder;
import com.github.gerbsen.encoding.Encoder.Encoding;

/**
 *
 * @author ngonga
 */
public class AprioriBasedDisambiguation implements UriRetrieval {

    Connection con;
    public static Logger logger = java.util.logging.Logger.getLogger(AprioriBasedDisambiguation.class.getName());

    /**
     * Constructor
     *
     * @param ip
     * @param user
     * @param pwd
     */
    public AprioriBasedDisambiguation(String ip, String user, String pwd) {
        con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = (Connection) DriverManager.getConnection(ip, user, pwd);
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Check for URIs that contain entry as substring
     *
     * @param label Label of entity
     * @return List of mapping URIs. Score is 1 (perfect match)
     */
    public List<String> getUriCandidates(String label) {
        List<String> result = new ArrayList<>();
        String resource;

        try {
            Statement stmt = con.createStatement();
            String sql = "SELECT distinct uriname FROM words_uris WHERE words = \'" + label + "\'";
            ResultSet rset = stmt.executeQuery(sql);
            while (rset.next()) {
                resource = rset.getString("uriname");
                result.add(resource);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Computes to a-priori score based on a single uri
     *
     * @param uri URI of the resource
     * @return A-priori score
     */
    public double getAprioriScore(String uri) {
        // ResultSet rset = null;
        double nbedges = 0.0;

        try {
            Statement stmt = con.createStatement();
            String sql = "SELECT ingoing_edges FROM uri_aprioriscores WHERE uri_name='" + uri + "'";
            ResultSet rset = stmt.executeQuery(sql);
            while (rset.next()) {
                nbedges = rset.getDouble("ingoing_edges");
                //System.out.println(uri +"\t"+nbedges);
            }
            stmt.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, "SQL Fehler " + ex.getMessage());
        }
        return nbedges;
    }

    public Map<String, String> getUris(String text, List<String> entityLabels) {
        
        Map<String, String> result = new HashMap<>();
        
        for (String e : entityLabels) {
            
            List<String> uris = getUriCandidates(e);
            
            // if we dont find uri candidates, we need to generate our own based on dbpedia style
            if (uris.isEmpty()) 
                result.put(e, Constants.RDF_LIVE_NEWS_RESOURCE_PREFIX + Encoder.urlEncode(e.replace(" ", "_"), Encoding.UTF_8));
            else {
                
                double max = 0, score;
                String uri = "";
                
                for (String u : uris) {
                    
                    score = getAprioriScore(u);
                    if (score > max) {
                        max = score;
                        uri = u;
                    }
                }
                result.put(e, uri);
            }
        }
        return result;
    }

    @Override
    public String getUri(String label) {
    
        throw new RuntimeException("This method is not supported for Apriori based disambiguation!");
    }
}
