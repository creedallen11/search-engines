package edu.umass.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/* Quality measures. NOTE: When @n passed to a quality measure is less than
the length of the trec results, n is set to the length of the results. */
public class Metrics {

    /* Retrived & Relevant / Retrieved */
    public static double precision(Judgments judgment, TrecRun results, int n) {
        if (n > results.length()) n = results.length();
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (judgment.docRelevance.getOrDefault(results.docIDs.get(i), -1) > 0)
                count++;
        }
        //System.out.println("count = " + count);
        return count / (float) n;
    }

    /* Retrieved & Relevant / Relevant  */
    public static double recall(Judgments judgment, TrecRun results, int n) {
        if (n > results.length()) n = results.length();

        int relevant_set = judgment.docRelevance.entrySet().stream()
                .filter(p -> p.getValue() > 0).collect(Collectors.toList()).size();
        int retrieved_and_relevant = 0;
        for (int i = 0; i < n; i++) {
            if (judgment.docRelevance.getOrDefault(results.docIDs.get(i), -1) > 0)
                retrieved_and_relevant++;
        }
        if (relevant_set > 0) return retrieved_and_relevant / (float) relevant_set;
        else return 0; // NaN Check
    }

    /* Implement F1 f-measure. Assumes user passes P@n and P@m s.t. n == m. */
    public static double f_measure(double P, double R) {
        double result = (R+P > 0) ? (2 * R * P) / (R + P): 0;
        return result;
        //return (2 * R * P) / (R + P);
    }

    /* Calculates ndcg@n and idcg@n, returns the quotient. */
    public static double ndcg(Judgments judgment, TrecRun qp, int n) {
        if (n > qp.length()) n = qp.length();

    /* Add the first term (i = 0)'s relevance then sum i up to rank
    n divided by log2 of i. Rank is 0 based here and below. */
        double dcg = judgment.docRelevance.getOrDefault(qp.docIDs.get(0), 0);
        for (int i = 1; i < n; i++)
            dcg += (judgment.docRelevance.getOrDefault(qp.docIDs.get(i), 0) / (Math.log(i+1) / Math.log(2)));

    /* Now calculate idcg. */
        Integer[] perfect = new Integer[qp.length()];
        if (perfect.length == 0) return 0.0;
        for (int i = 0; i < qp.length(); i ++) {
            perfect[i] = judgment.docRelevance.getOrDefault(qp.docIDs.get(i), 0);
        } // Sort all the relevance judgments descending, we will select the n best.
        Arrays.sort(perfect, Collections.reverseOrder());
        double perfect_dcg = perfect[0]; // relevance judgment 1 is added
        if (perfect.length < n) n = perfect.length;
        for (int i =1; i < n; i++) {
            perfect_dcg += (perfect[i] / (Math.log(i + 1) / Math.log(2)));
        } // the rest are added.
        if (perfect_dcg > 0) return dcg/perfect_dcg;
        else return 0; //NaN check
    }

    /* Average of all precision in which the corresponding recall changes. */
    public static double average_precision(Judgments groundTruth, TrecRun qp, int n) {
    /* Tracks the previous recall value to check for change. */
        double previousRecall = recall(groundTruth, qp, 1);
        double previousPrecision = precision(groundTruth, qp, 1);

        ArrayList<Double> precisionValues = new ArrayList<>();
        if (previousRecall > 0) precisionValues.add(previousPrecision);
        for (int i = 2; i <= n; i++) {
            double currentRecall = recall(groundTruth, qp, i);
      /* If recall has changed save the precision. */
            if (Math.abs(currentRecall - previousRecall) > .00001)
                precisionValues.add(precision(groundTruth, qp, i));
      /* Update the previous recall. */
            previousRecall = currentRecall;
        } // Return the average precision.
        return precisionValues.stream().mapToDouble(a -> a).average().orElse(0);
    }
}
