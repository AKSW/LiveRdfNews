/**
 * 
 */
package org.aksw.simba.rdflivenews.evaluation.comparator;

import java.util.Comparator;

import org.aksw.simba.rdflivenews.evaluation.ClusterEvaluationResult;

/**
 * @author gerb
 *
 */
public class InterIntraClusterSimilarityComparator implements
		Comparator<ClusterEvaluationResult> {

	@Override
	public int compare(ClusterEvaluationResult o1, ClusterEvaluationResult o2) {

		double x = (o1.intraClusterSimilarity / o1.interClusterSimilarity) - (o2.intraClusterSimilarity / o2.interClusterSimilarity);
		return x > 0 ? 1 : x == 0 ? 0 : -1;
	}
}
