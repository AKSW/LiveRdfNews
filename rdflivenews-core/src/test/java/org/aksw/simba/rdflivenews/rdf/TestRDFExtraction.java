package org.aksw.simba.rdflivenews.rdf;

import org.aksw.simba.rdflivenews.cluster.Cluster;
import org.aksw.simba.rdflivenews.entity.Entity;
import org.aksw.simba.rdflivenews.pair.EntityPair;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.rdf.impl.NIFRdfExtraction;
import org.aksw.simba.rdflivenews.rdf.impl.SimpleRdfExtraction;
import org.aksw.simba.rdflivenews.rdf.triple.Triple;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: hellmann
 * Date: 06.05.13
 */
public class TestRDFExtraction {



    @Test
    public void testExtraction() {
        Set<Cluster<Pattern>> clusters = new HashSet<Cluster<Pattern>>();
        Cluster<Pattern> c1 = new Cluster<Pattern>();
        Pattern p1 = new DefaultPattern();

        EntityPair pair = new EntityPair(new Entity("Houston Airports", "http://dbpedia.org/ontology/Organisation"), new Entity("Marlene McClinton", "http://dbpedia.org/ontology/Person"), 82400)   ;
        pair.getFirstEntity().setUri("http://dbpedia.org/resource/George_Bush_Intercontinental_Airport");
        pair.getSecondEntity().setUri("http://rdflivenews.aksw.org/resource/Marlene_McClinton");

        p1.setExampleSentence("... costs of the Wi-Fi system , '' explains Houston Airports spokesperson Marlene McClinton , `` And charges ...");
        p1.setFavouriteTypeFirstEntity("http://dbpedia.org/ontology/Place");
        p1.setFavouriteTypeSecondEntity("http://dbpedia.org/ontology/Person");
        p1.setNaturalLanguageRepresentation(" spokesperson ");
        p1.setScore(0.8);
        p1.addLearnedFromEntities(pair);


        c1.setName("this Is A Name");
        c1.setRdfsDomain("http://dbpedia.org/ontology/Place");
        c1.setRdfsRange("http://dbpedia.org/ontology/Person");
        c1.setUri("http://rdflivenews.aksw.org/ontology/spokesperson");
        c1.add(p1);

        clusters.add(c1);

        NIFRdfExtraction extract = new NIFRdfExtraction();
        extract.testing = true;

        List<Triple> triples = extract.extractRdf(clusters);

        for (Triple t:triples){
            System.out.println(t.toString());
        }

    }
}
