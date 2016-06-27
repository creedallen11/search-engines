package edu.umass.metrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Queries {
    /* Load a map of queryID -> Judgment pairs. */
    public static HashMap<String, Judgments> loadJudgments(String path) {
        HashMap<String, Judgments> queries = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String[] first = br.readLine().split(" ");
            String queryTag = first[0];
            Judgments current = new Judgments(queryTag);
            current.addJudgment(first[2], Integer.parseInt(first[3]));

            String line;
            while ((line = br.readLine()) != null) {

                String[] contents = line.split(" ");
                if (contents[0].equals(queryTag)) {
                    current.addJudgment(contents[2], Integer.parseInt(contents[3]));
                } else {
                    queries.put(current.queryID, current);
                    String[] next = line.split(" ");
                    queryTag = next[0];
                    current = new Judgments(queryTag);
                    current.addJudgment(next[2], Integer.parseInt(next[3]));
                }
            }
            queries.put(current.queryID, current); // add last query to map
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }

    /* Like loadJudgments but queryID -> TrecRun result for that queryID */
    public static HashMap<String, TrecRun> loadPredictions(String path) {
        HashMap<String, TrecRun> queries = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String[] first = br.readLine().split(" ");
            String queryTag = first[0];
            TrecRun current = new TrecRun(queryTag);
            current.addResult(first[2], Integer.parseInt(first[3]), Double.parseDouble(first[4]));

            String line;
            while ((line = br.readLine()) != null) {

                String[] contents = line.split(" ");
                if (contents[0].equals(queryTag)) {
                    current.addResult(contents[2], Integer.parseInt(contents[3]), Double.parseDouble(first[4]));
                } else {
                    queries.put(current.queryID, current);
                    String[] next = line.split(" ");
                    queryTag = next[0];
                    current = new TrecRun(queryTag);
                    current.addResult(next[2], Integer.parseInt(next[3]), Double.parseDouble(next[4]));
                }
            }
            queries.put(current.queryID, current);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }

    /* Main runner, generates the output.metrics file. */
    public static void main(String[] args) throws IOException {
        HashMap<String, Judgments> judgments = Queries.loadJudgments("p6-data/qrels");
        String[] results = {"p6-data/sdm.trecrun", "p6-data/bm25.trecrun", "p6-data/stress.trecrun", "p6-data/ql.trecrun"};
        List<String> output = new ArrayList<>();
    /* Generate metrics for each result. */
        for (String path : results) {
            HashMap<String, TrecRun> run = Queries.loadPredictions(path);

            double ndcg_20 = 0; double precision_5 = 0;
            double precision_10 = 0; double recall_10 = 0;
            double f1_10 = 0; double avg_precision = 0;
      /* Calculate measures for each query (id). */
            for (String id : run.keySet()) {
                ndcg_20 += Metrics.ndcg(judgments.get(id), run.get(id), 20);
                precision_5 += Metrics.precision(judgments.get(id), run.get(id), 5);
                double P = Metrics.precision(judgments.get(id), run.get(id), 10);
                precision_10 += P;
                double R = Metrics.recall(judgments.get(id), run.get(id), 10);
                recall_10 += R;
                f1_10 += Metrics.f_measure(P, R);
                avg_precision += Metrics.average_precision(judgments.get(id), run.get(id), run.get(id).docIDs.size());
            }
      /* Save the averaged output for file writing. */
            String trecRun = path.substring(8);
            output.add(trecRun + " NDCG@20 " + ndcg_20 / run.size());
            output.add(trecRun + " P@5 " + precision_5 / run.size());
            output.add(trecRun + " P@10 " + precision_10 / run.size());
            output.add(trecRun + " Recall@10 " + recall_10 / run.size());
            output.add(trecRun + " F1@10 " + f1_10 / run.size());
            output.add(trecRun + " MAP " + avg_precision / run.size() + "\n");
        }
        Files.write(Paths.get("output.metrics"), output);

//    for (String path : results) {
//      HashMap<String, TrecRun> run = Queries.loadPredictions(path);
//
//
//      String id = "450";
//      /* Calculate measures for each query (id). */
//      double P = Metrics.precision(judgments.get(id), run.get(id), run.get(id).docIDs.size());
//      double R = Metrics.recall(judgments.get(id), run.get(id), run.get(id).docIDs.size());
//
//
//      /* Save the averaged output for file writing. */
//      System.out.println("Run = " + path.substring(8));
//      System.out.println("Precision= " + P);
//      System.out.println("Recall= " + R + "\n");
//    }
    }
}