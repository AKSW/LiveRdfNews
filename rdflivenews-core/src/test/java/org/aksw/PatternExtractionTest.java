/**
 * 
 */
package org.aksw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aksw.simba.rdflivenews.RdfLiveNews;
import org.aksw.simba.rdflivenews.config.Config;
import org.aksw.simba.rdflivenews.index.IndexManager;
import org.aksw.simba.rdflivenews.index.Sentence;
import org.aksw.simba.rdflivenews.patternsearch.concurrency.PatternSearchThreadManager;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class PatternExtractionTest extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *            name of the test case
     */
    public PatternExtractionTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(PatternExtractionTest.class);
    }

    public void testPatternExtraction() throws InvalidFileFormatException, IOException {

        RdfLiveNews.CONFIG = new Config(new Ini(File.class.getResourceAsStream("/config.ini")));
        IndexManager.INDEX_DIRECTORY = RdfLiveNews.CONFIG.getStringSetting("database", "test-directory");
        IndexManager.getInstance().deleteIndex();
        
        List<Sentence> sentences = new ArrayList<Sentence>();
        for ( String sent : this.createSentences() ) {
            
            Sentence sentence = new Sentence();
            sentence.setArticleUrl("http://article.com/number1");
            sentence.setText(sent.replaceAll("_[A-Z]*", ""));
            sentence.setNerTaggedSentence(sent);
            sentence.setTimeSliceID(1);
            sentence.setExtractionDate(new Date());
            sentences.add(sentence);
        }
        
        IndexManager.getInstance().addSentences(sentences);
        
        assertEquals(216, IndexManager.getInstance().getNumberOfDocuments());
        
        List<Integer> sentenceFromFirstIterationIds = IndexManager.getInstance().getSentenceFromTimeSlice(1);
        assertEquals(216, sentenceFromFirstIterationIds.size());
        
        PatternSearchThreadManager patternSearchManager = new PatternSearchThreadManager();
        List<org.aksw.simba.rdflivenews.pattern.Pattern> patterns = patternSearchManager.startPatternSearchCallables(sentenceFromFirstIterationIds, 3);
        
        // the patterns are not merged so we should have 
        // extracted as much patterns as we do have sentences
        assertEquals(216, patterns.size()); 
        
        List<org.aksw.simba.rdflivenews.pattern.Pattern> mergedPatterns = patternSearchManager.mergeNewFoundPatterns(patterns);
        assertEquals(35, mergedPatterns.size());
        
        for ( org.aksw.simba.rdflivenews.pattern.Pattern pattern : mergedPatterns ) {
            
            if ( pattern.getNaturalLanguageRepresentation().equals("is a subsidiary of") ) assertEquals(9, pattern.getTotalOccurrence());
            if ( pattern.getNaturalLanguageRepresentation().equals("is smaller in size than") ) assertEquals(5, pattern.getTotalOccurrence());
            if ( pattern.getNaturalLanguageRepresentation().equals("has fired") ) assertEquals(2, pattern.getTotalOccurrence());
            if ( pattern.getNaturalLanguageRepresentation().equals("has meet") ) assertEquals(2, pattern.getLearnedFromEntities().get(0).getOccurrence());
        }
        
        // just to have a feeling what a pattern actually is 
        System.out.println(mergedPatterns.get(23));
        System.out.println(mergedPatterns.get(8));
        System.out.println(mergedPatterns.get(19));
    }
    
    private List<String> createSentences(){
        
        List<String> sentences = new ArrayList<String>();
        
        List<String> personNames     = this.generatePersonNames();
        List<String> companyNames    = this.generateCompanyNames();
        List<String> placeNames      = this.generatePlaceNames();
        
        List<String> person2person      = generatePersonToPersonPatterns();
        List<String> place2place        = generatePlaceToPlacePatterns();
        List<String> company2company    = generateCompanyToCompanyPatterns();
        List<String> person2place       = generatePersonToPlacePatterns();
        List<String> person2company     = generatePersonToCompanyPatterns();
        List<String> company2place      = generateCompanyToPlacePatterns();

        // do this first loop twice to find the some pairs multiple times
        for ( int i = 0, j = 0; i < personNames.size() - 1; i = i + 2, j++ )  
            sentences.add(String.format("%s %s %s ._OTHER", personNames.get(i), person2person.get(j % person2person.size()), personNames.get(i+1)));
        for ( int i = 0, j = 0; i < personNames.size() - 1; i = i + 2, j++ )  
            sentences.add(String.format("%s %s %s ._OTHER", personNames.get(i), person2person.get(j % person2person.size()), personNames.get(i+1)));

        for ( int i = 0, j = 0; i < placeNames.size() - 1; i = i + 2, j++ )
            sentences.add(String.format("%s %s %s ._OTHER", placeNames.get(i), place2place.get(j % place2place.size()), placeNames.get(i+1)));
        
        for ( int i = 0, j = 0; i < companyNames.size() - 1; i = i + 2, j++ ) 
            sentences.add(String.format("%s %s %s ._OTHER", companyNames.get(i), company2company.get(j % company2company.size()), companyNames.get(i+1)));
            
        for ( int i = 0; i < personNames.size() && i < placeNames.size(); i++ )
            sentences.add(String.format("%s %s %s ._OTHER", personNames.get(i), person2place.get(i % person2place.size()), placeNames.get(i)));
        
        for ( int i = 0; i < personNames.size() && i < companyNames.size(); i++ )
            sentences.add(String.format("%s %s %s ._OTHER", personNames.get(i), person2company.get(i % person2company.size()), companyNames.get(i)));
        
        for ( int i = 0; i < companyNames.size() && i < placeNames.size(); i++ )
            sentences.add(String.format("%s %s %s ._OTHER", companyNames.get(i), company2place.get(i % company2place.size()), placeNames.get(i)));
        
        return sentences;
    }

    private List<String> generatePersonToPersonPatterns(){
        
        List<String> personToPersonPattern = new ArrayList<String>();
        personToPersonPattern.add("has_OTHER meet_OTHER");
        personToPersonPattern.add("likes_OTHER");
        personToPersonPattern.add("is_OTHER married_OTHER to_OTHER");
        personToPersonPattern.add("has_OTHER an_OTHER affair_OTHER with_OTHER");
        personToPersonPattern.add("has_OTHER fired_OTHER");
        personToPersonPattern.add("hired_OTHER");
        personToPersonPattern.add("will_OTHER never_OTHER like_OTHER");
        personToPersonPattern.add("is_OTHER the_OTHER wife_OTHER of_OTHER");
        
        return personToPersonPattern;
    }
    
    private List<String> generatePlaceToPlacePatterns(){
        
        List<String> placeToPlacePattern = new ArrayList<String>();
        placeToPlacePattern.add("'s_OTHER population_OTHER is_OTHER smaller_OTHER then_OTHER the_OTHER one_OTHER of_OTHER");
        placeToPlacePattern.add("has_OTHER more_OTHER inhabitants_OTHER then_OTHER");
        placeToPlacePattern.add("lays_OTHER 20km_OTHER north_OTHER of_OTHER");
        placeToPlacePattern.add("is_OTHER a_OTHER suburb_OTHER of_OTHER");
        placeToPlacePattern.add("is_OTHER smaller_OTHER in_OTHER size_OTHER than_OTHER");
        
        return placeToPlacePattern;
    }
    
    private List<String> generateCompanyToCompanyPatterns(){
        
        List<String> companyToCompanyPattern = new ArrayList<String>();
        companyToCompanyPattern.add("is_OTHER a_OTHER subsidiary_OTHER of_OTHER");
        companyToCompanyPattern.add("is_OTHER willing_OTHER to_OTHER buy_OTHER");
        companyToCompanyPattern.add("wants_OTHER to_OTHER acquire_OTHER");
        companyToCompanyPattern.add("has_OTHER acquired_OTHER");
        companyToCompanyPattern.add("has_OTHER announced_OTHER to_OTHER buy_OTHER");
        companyToCompanyPattern.add("is_OTHER going_OTHER to_OTHER merge_OTHER with_OTHER");
        
        return companyToCompanyPattern;
    }
    
    private List<String> generatePersonToPlacePatterns(){
        
        List<String> personToPlacePattern = new ArrayList<String>();
        personToPlacePattern.add("was_OTHER born_OTHER in_OTHER");
        personToPlacePattern.add("has_OTHER studied_OTHER in_OTHER");
        personToPlacePattern.add("has_OTHER died_OTHER in_OTHER");
        personToPlacePattern.add("travelled_OTHER through_OTHER");
        personToPlacePattern.add("meet_OTHER his_OTHER wife_OTHER in_OTHER");
        
        return personToPlacePattern;
    }
    
    private List<String> generatePersonToCompanyPatterns(){
        
        List<String> personToCompanyPattern = new ArrayList<String>();
        personToCompanyPattern.add("spent_OTHER all_OTHER his_OTHER money_OTHER to_OTHER buy_OTHER");
        personToCompanyPattern.add("has_OTHER bought_OTHER");
        personToCompanyPattern.add("has_OTHER been_OTHER ruined_OTHER by_OTHER");
        personToCompanyPattern.add("is_OTHER going_OTHER to_OTHER sue_OTHER");
        personToCompanyPattern.add("build_OTHER the_OTHER company_OTHER");
        
        return personToCompanyPattern;
    }
    
    private List<String> generateCompanyToPlacePatterns(){
        
        List<String> companyToPlacePattern = new ArrayList<String>();
        companyToPlacePattern.add("is_OTHER located_OTHER in_OTHER");
        companyToPlacePattern.add("is_OTHER headquarted_OTHER in_OTHER");
        companyToPlacePattern.add("create_OTHER a_OTHER subsidiary_OTHER in_OTHER");
        companyToPlacePattern.add("has_OTHER planned_OTHER to_OTHER expand_OTHER to_OTHER");
        companyToPlacePattern.add("wants_OTHER to_OTHER export_OTHER to_OTHER");
        companyToPlacePattern.add("is_OTHER importing_OTHER products_OTHER from_OTHER");
        
        return companyToPlacePattern;
    }
    
    /**
     * 
     * @return
     */
    private List<String> generatePersonNames() {

        List<String> personNames = new ArrayList<String>();
        personNames.add("Christian_PERSON Bale_PERSON");
        personNames.add("Gary_PERSON Oldman_PERSON");
        personNames.add("Tom_PERSON Hardy_PERSON");
        personNames.add("Joseph_PERSON Gordon-Levitt_PERSON");
        personNames.add("Anne_PERSON Hathaway_PERSON");
        personNames.add("Marion_PERSON Cotillard_PERSON");
        personNames.add("Morgan_PERSON Freeman_PERSON");
        personNames.add("Michael_PERSON Caine_PERSON");
        personNames.add("Matthew_PERSON Modine_PERSON");
        personNames.add("Alon_PERSON Aboutboul_PERSON");
        personNames.add("Ben_PERSON Mendelsohn_PERSON");
        personNames.add("Burn_PERSON Gorman_PERSON");
        personNames.add("Daniel_PERSON Sunjata_PERSON");
        personNames.add("Aidan_PERSON Gillen_PERSON");
        personNames.add("Sam_PERSON Kennard_PERSON");
        personNames.add("Keanu_PERSON Reeves_PERSON");
        personNames.add("Laurence_PERSON Fishburne_PERSON");
        personNames.add("Carrie-Anne_PERSON Moss_PERSON");
        personNames.add("Hugo_PERSON Weaving_PERSON");
        personNames.add("Gloria_PERSON Foster_PERSON");
        personNames.add("Joe_PERSON Pantoliano_PERSON");
        personNames.add("Marcus_PERSON Chong_PERSON");
        personNames.add("Julian_PERSON Arahanga_PERSON");
        personNames.add("Matt_PERSON Doran_PERSON");
        personNames.add("Belinda_PERSON McClory_PERSON");
        personNames.add("Anthony_PERSON Ray_PERSON Parker_PERSON");
        personNames.add("Paul_PERSON Goddard_PERSON");
        personNames.add("Robert_PERSON Taylor_PERSON");
        personNames.add("David_PERSON Aston_PERSON");
        personNames.add("Marc_PERSON Aden_PERSON Gray_PERSON");

        return personNames;
    }
    
    /**
     * 
     * @return
     */
    private List<String> generateCompanyNames(){
        
        List<String> companyNames = new ArrayList<String>();
        
        companyNames.add("Exxon_ORGANIZATION Mobil_ORGANIZATION");
        companyNames.add("Royal_ORGANIZATION Dutch_ORGANIZATION Shell_ORGANIZATION");
        companyNames.add("Walmart_ORGANIZATION");
        companyNames.add("BP_ORGANIZATION");
        companyNames.add("Vitol_ORGANIZATION");
        companyNames.add("Sinopec_ORGANIZATION");
        companyNames.add("Chevron_ORGANIZATION");
        companyNames.add("ConocoPhillips_ORGANIZATION");
        companyNames.add("Samsung_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("Toyota_ORGANIZATION Motors_ORGANIZATION");
        companyNames.add("State_ORGANIZATION Grid_ORGANIZATION Corporation_ORGANIZATION of_ORGANIZATION China_ORGANIZATION");
        companyNames.add("PetroChina_ORGANIZATION");
        companyNames.add("Total_ORGANIZATION S.A._ORGANIZATION");
        companyNames.add("Volkswagen_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("Japan_ORGANIZATION Post_ORGANIZATION Holdings_ORGANIZATION");
        companyNames.add("Glencore_ORGANIZATION");
        companyNames.add("Saudi_ORGANIZATION Aramco_ORGANIZATION");
        companyNames.add("Gazprom_ORGANIZATION");
        companyNames.add("Fannie_ORGANIZATION Mae_ORGANIZATION");
        companyNames.add("General_ORGANIZATION Motors_ORGANIZATION");
        companyNames.add("General_ORGANIZATION Electric_ORGANIZATION");
        companyNames.add("Carrefour_ORGANIZATION");
        companyNames.add("Petrobras_ORGANIZATION");
        companyNames.add("Berkshire_ORGANIZATION Hathaway_ORGANIZATION");
        companyNames.add("Allianz_ORGANIZATION");
        companyNames.add("Daimler_ORGANIZATION AG_ORGANIZATION");
        companyNames.add("Ford_ORGANIZATION Motor_ORGANIZATION Company_ORGANIZATION");
        companyNames.add("Eni_ORGANIZATION");
        companyNames.add("Hewlett-Packard_ORGANIZATION");
        companyNames.add("AT&T_ORGANIZATION");
        companyNames.add("Nippon_ORGANIZATION Telegraph_ORGANIZATION and_ORGANIZATION Telephone_ORGANIZATION");
        companyNames.add("Assicurazioni_ORGANIZATION Generali_ORGANIZATION");
        companyNames.add("Cargill_ORGANIZATION");
        companyNames.add("E.ON_ORGANIZATION");
        companyNames.add("JX_ORGANIZATION Holdings_ORGANIZATION");
        companyNames.add("GDF_ORGANIZATION Suez_ORGANIZATION");
        companyNames.add("AXA_ORGANIZATION");
        companyNames.add("Hitachi,_ORGANIZATION Ltd._ORGANIZATION");
        companyNames.add("McKesson_ORGANIZATION Corporation_ORGANIZATION");
        companyNames.add("Bank_ORGANIZATION of_ORGANIZATION America_ORGANIZATION");
        companyNames.add("Tesco_ORGANIZATION");
        companyNames.add("Freddie_ORGANIZATION Mac_ORGANIZATION");
        companyNames.add("Apple_ORGANIZATION Inc._ORGANIZATION");
        companyNames.add("Honda_ORGANIZATION");
        companyNames.add("Verizon_ORGANIZATION");
        companyNames.add("Nissan_ORGANIZATION Motors_ORGANIZATION");
        companyNames.add("Panasonic_ORGANIZATION Corporation_ORGANIZATION");
        companyNames.add("Nestlé_ORGANIZATION");
        companyNames.add("LUKoil_ORGANIZATION");
        companyNames.add("Pemex_ORGANIZATION");
        companyNames.add("Hon_ORGANIZATION Hai_ORGANIZATION Precision_ORGANIZATION Industry_ORGANIZATION");
        companyNames.add("JPMorgan_ORGANIZATION Chase_ORGANIZATION");
        companyNames.add("Cardinal_ORGANIZATION Health_ORGANIZATION");
        companyNames.add("Koch_ORGANIZATION Industries_ORGANIZATION");
        companyNames.add("Petróleos_ORGANIZATION de_ORGANIZATION Venezuela_ORGANIZATION");
        companyNames.add("IBM_ORGANIZATION");
        companyNames.add("Siemens_ORGANIZATION AG_ORGANIZATION");
        companyNames.add("Hyundai_ORGANIZATION Motors_ORGANIZATION");
        companyNames.add("Enel_ORGANIZATION");
        companyNames.add("CVS_ORGANIZATION Caremark_ORGANIZATION");
        companyNames.add("Lloyds_ORGANIZATION Banking_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("BASF_ORGANIZATION");
        companyNames.add("UnitedHealth_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("Rosneft_ORGANIZATION");
        companyNames.add("Statoil_ORGANIZATION");
        companyNames.add("Metro_ORGANIZATION AG_ORGANIZATION");
        companyNames.add("Aviva_ORGANIZATION");
        companyNames.add("BMW_ORGANIZATION");
        companyNames.add("Electricité_ORGANIZATION de_ORGANIZATION France_ORGANIZATION");
        companyNames.add("Costco_ORGANIZATION");
        companyNames.add("Citigroup_ORGANIZATION");
        companyNames.add("National_ORGANIZATION Mutual_ORGANIZATION Insurance_ORGANIZATION Federation_ORGANIZATION of_ORGANIZATION Agricultural_ORGANIZATION Cooperatives_ORGANIZATION");
        companyNames.add("Sony_ORGANIZATION");
        companyNames.add("Wells_ORGANIZATION Fargo_ORGANIZATION");
        companyNames.add("Société_ORGANIZATION Générale_ORGANIZATION");
        companyNames.add("Kuwait_ORGANIZATION Petroleum_ORGANIZATION Corporation_ORGANIZATION");
        companyNames.add("Deutsche_ORGANIZATION Telekom_ORGANIZATION");
        companyNames.add("Procter_ORGANIZATION &_ORGANIZATION Gamble_ORGANIZATION");
        companyNames.add("ICBC_ORGANIZATION");
        companyNames.add("Lidl_ORGANIZATION");
        companyNames.add("Valero_ORGANIZATION Energy_ORGANIZATION");
        companyNames.add("Kroger_ORGANIZATION");
        companyNames.add("Nippon_ORGANIZATION Life_ORGANIZATION Insurance_ORGANIZATION");
        companyNames.add("Telefónica_ORGANIZATION");
        companyNames.add("Repsol_ORGANIZATION YPF_ORGANIZATION");
        companyNames.add("Archer_ORGANIZATION Daniels_ORGANIZATION Midland_ORGANIZATION");
        companyNames.add("AmerisourceBergen_ORGANIZATION");
        companyNames.add("HSBC_ORGANIZATION");
        companyNames.add("SK_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("National_ORGANIZATION Iranian_ORGANIZATION Oil_ORGANIZATION Company_ORGANIZATION");
        companyNames.add("Trafigura_ORGANIZATION");
        companyNames.add("ArcelorMittal_ORGANIZATION");
        companyNames.add("American_ORGANIZATION International_ORGANIZATION Group_ORGANIZATION");
        companyNames.add("Toshiba_ORGANIZATION");
        companyNames.add("Petronas_ORGANIZATION");
        companyNames.add("Itaú_ORGANIZATION Unibanco_ORGANIZATION");
        companyNames.add("Indian_ORGANIZATION Oil_ORGANIZATION Corporation_ORGANIZATION");
        companyNames.add("Fiat_ORGANIZATION");
        companyNames.add("ZEN-NOH_ORGANIZATION");
        companyNames.add("PSA_ORGANIZATION Peugeot_ORGANIZATION Citroën_ORGANIZATION");
        companyNames.add("Vodafone_ORGANIZATION");
        companyNames.add("Marathon_ORGANIZATION Oil_ORGANIZATION");
        
        return companyNames;
    }
    
    /**
     * 
     * @return
     */
    private List<String> generatePlaceNames(){
        
        List<String> placeNames = new ArrayList<String>();
        
        placeNames.add("London_PLACE");
        placeNames.add("Berlin_PLACE");
        placeNames.add("Madrid_PLACE");
        placeNames.add("Rome_PLACE");
        placeNames.add("Paris_PLACE");
        placeNames.add("Warsaw_PLACE");
        placeNames.add("Hamburg_PLACE");
        placeNames.add("Budapest_PLACE");
        placeNames.add("Vienna_PLACE");
        placeNames.add("Bucharest_PLACE");
        placeNames.add("Barcelona_PLACE");
        placeNames.add("Munich_PLACE");
        placeNames.add("Milan_PLACE");
        placeNames.add("Prague_PLACE");
        placeNames.add("Sofia_PLACE");
        placeNames.add("Brussels_PLACE");
        placeNames.add("Birmingham_PLACE");
        placeNames.add("Cologne_PLACE");
        placeNames.add("Naples_PLACE");
        placeNames.add("Turin_PLACE");
        placeNames.add("Stockholm_PLACE");
        placeNames.add("Marseille_PLACE");
        placeNames.add("Valencia_PLACE");
        placeNames.add("Amsterdam_PLACE");
        placeNames.add("Leeds_PLACE");
        placeNames.add("Kraków_PLACE");
        placeNames.add("Łódź_PLACE");
        placeNames.add("Athens_PLACE");
        placeNames.add("Riga_PLACE");
        placeNames.add("Sevilla_PLACE");
        placeNames.add("Frankfurt_PLACE");
        placeNames.add("Zaragoza_PLACE");
        placeNames.add("Palermo_PLACE");
        placeNames.add("Wrocław_PLACE");
        placeNames.add("Rotterdam_PLACE");
        placeNames.add("Genoa_PLACE");
        placeNames.add("Helsinki_PLACE");
        placeNames.add("Stuttgart_PLACE");
        placeNames.add("Düsseldorf_PLACE");
        placeNames.add("Glasgow_PLACE");
        placeNames.add("Dortmund_PLACE");
        placeNames.add("Essen_PLACE");
        placeNames.add("Málaga_PLACE");
        placeNames.add("Poznań_PLACE");
        placeNames.add("Vilnius_PLACE");
        placeNames.add("Bremen_PLACE");
        placeNames.add("Sheffield_PLACE");
        placeNames.add("Leipzig_PLACE");
        placeNames.add("Dresden_PLACE");
        placeNames.add("Dublin_PLACE");
        
        return placeNames;
    }
}
