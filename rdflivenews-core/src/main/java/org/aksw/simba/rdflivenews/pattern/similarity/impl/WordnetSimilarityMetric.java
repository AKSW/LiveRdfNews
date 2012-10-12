/**
 * 
 */
package org.aksw.simba.rdflivenews.pattern.similarity.impl;

import org.aksw.simba.rdflivenews.Constants;
import org.aksw.simba.rdflivenews.pattern.DefaultPattern;
import org.aksw.simba.rdflivenews.pattern.Pattern;
import org.aksw.simba.rdflivenews.pattern.similarity.SimilarityMetric;
import org.aksw.simba.rdflivenews.wordnet.Wordnet;
import org.apache.log4j.chainsaw.Main;

import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.similarity.wordnet.SimilarityAssessor;
import com.github.gerbsen.similarity.wordnet.WordNotFoundException;

import edu.stanford.nlp.process.Morphology;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class WordnetSimilarityMetric implements SimilarityMetric {

    private SimilarityAssessor similarityAssessor = new SimilarityAssessor();
    private Morphology lemmatizer = new Morphology();
    public int counter = 0;

    public BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/test/templemma.txt", "UTF-8", WRITER_WRITE_MODE.OVERRIDE);

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

        String[] partsOfPattern2 = pattern2.getNaturalLanguageRepresentationWithTags().split(" ");
        
        for (String partOfPattern1 : pattern1.getNaturalLanguageRepresentationWithTags().split(" ")) {

            String tokenOne = partOfPattern1.substring(0, partOfPattern1.lastIndexOf("_"));
            String tagOne = partOfPattern1.substring(partOfPattern1.lastIndexOf("_") + 1);

            if (Constants.STOP_WORDS.contains(tokenOne.toLowerCase())) continue;

            for ( int i = 0 ; i < partsOfPattern2.length ; i++ ) {

                String tokenTwo = partsOfPattern2[i].substring(0, partsOfPattern2[i].lastIndexOf("_"));
                String tagTwo = partsOfPattern2[i].substring(partsOfPattern2[i].lastIndexOf("_") + 1);

                if (Constants.STOP_WORDS.contains(tokenTwo.toLowerCase())) continue;

                double sim = Wordnet.getInstance().getWordnetSimilarity(
                        lemmatizer.lemma(tokenOne, tagOne), lemmatizer.lemma(tokenTwo, tagTwo), Wordnet.JCN_SIMILARITY);

                total += sim;
                comparison++;
            }
        }

        return total == 0D ? 0D : total / comparison;
    }

    public static void main(String[] args) {

        Pattern pattern1 = new DefaultPattern("says");
        pattern1.setNaturalLanguageRepresentationWithTags("says_VBZ");
        Pattern pattern2 = new DefaultPattern("told the `");
        pattern2.setNaturalLanguageRepresentationWithTags("told_VBD the_DT `_``");

        WordnetSimilarityMetric m = new WordnetSimilarityMetric();
        System.out.println(m.calculateSimilarity(pattern1, pattern2));
    }
}
