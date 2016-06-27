package edu.umass.metrics;

import java.util.HashMap;

/*Judgment is a queryID and a map of (document, relevance pairs) for query. */
public class Judgments {
    final String queryID;
    HashMap<String, Integer> docRelevance;
    public Judgments(String id) {
        this.queryID = id;
        docRelevance = new HashMap<>();
    }

    /* Input a document and it's relevance judgment into
    this query's log. */
    public void addJudgment(String docID, int grade) {
        docRelevance.put(docID, grade);
    }

    @Override
    public String toString() {
        return docRelevance.toString();
    }
}
