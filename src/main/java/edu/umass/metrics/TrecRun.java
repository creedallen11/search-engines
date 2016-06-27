package edu.umass.metrics;

import java.util.ArrayList;

/*A trecrun has a query ID and associated documents, rank, score. */
public class TrecRun {
    final String queryID;
    ArrayList<String> docIDs;
    ArrayList<Integer> rank;
    ArrayList<Double> score;

    public TrecRun(String id) {
        queryID = id;
        docIDs = new ArrayList<>();
        rank = new ArrayList<>();
        score = new ArrayList<>();
    }

    /* Add a document and it's associated values to results. */
    public void addResult(String doc, int r, double s) {
        docIDs.add(doc);
        rank.add(r);
        score.add(s);
    }

    public int length() {
        return docIDs.size();
    }
}