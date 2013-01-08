/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.aksw.simba.rdflivenews.wordnet.Wordnet.WordnetSimilarity;

import edu.stanford.nlp.process.Morphology;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class WordnetSimilarityMetric implements SimilarityMetric {

    private Morphology lemmatizer = new Morphology();
    public int counter = 0;
    private WordnetSimilarity similarity = WordnetSimilarity.LIN;

    /*
     * (non-Javadoc)
     * 
     * @see org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric#
     * calculateSimilartiy(org.aksw.simba.rdflivenews.pattern.Pattern,
     * org.aksw.simba.rdflivenews.pattern.Pattern)
     */
    @Override
    public double calculateSimilarity(Pattern pattern1, Pattern pattern2) {

        double total = 0;
        int comparison = 0;

        String[] partsOfPattern2 = pattern2.getNaturalLanguageRepresentationWithTags().replace("-", "").split(" ");
        
        for (String partOfPattern1 : pattern1.getNaturalLanguageRepresentationWithTags().replace("-", "").split(" ")) {

            String tokenOne = partOfPattern1.substring(0, partOfPattern1.lastIndexOf("_"));
            String tagOne = partOfPattern1.substring(partOfPattern1.lastIndexOf("_") + 1);

            if (Constants.STOP_WORDS.contains(tokenOne.toLowerCase())) continue;

            for ( int i = 0 ; i < partsOfPattern2.length ; i++ ) {

                String tokenTwo = partsOfPattern2[i].substring(0, partsOfPattern2[i].lastIndexOf("_"));
                String tagTwo = partsOfPattern2[i].substring(partsOfPattern2[i].lastIndexOf("_") + 1);

                if (Constants.STOP_WORDS.contains(tokenTwo.toLowerCase())) continue;
                
                double sim = Wordnet.getInstance().getWordnetSimilarity(
                        lemmatizer.lemma(tokenOne, tagOne), lemmatizer.lemma(tokenTwo, tagTwo), this.similarity);

                total += sim;
                comparison++;
            }
        }

        return total == 0D ? 0D : total / comparison;
    }

    public static void main(String[] args) {

        Pattern pattern1 = new DefaultPattern("said on");
        pattern1.setNaturalLanguageRepresentationWithTags("said_VBZ on_DT");
        Pattern pattern2 = new DefaultPattern("said");
        pattern2.setNaturalLanguageRepresentationWithTags("said_VBD");

        WordnetSimilarityMetric m = new WordnetSimilarityMetric();
        System.out.println(m.calculateSimilarity(pattern1, pattern2));
    }

    /**
     * Default is LIN metric
     * 
     * @param sim
     */
    public void setWordnetSimilarity(WordnetSimilarity sim) {

        this.similarity = sim;
    }

    public WordnetSimilarity getSimilarityMetric() {

        return this.similarity;
    }
}
