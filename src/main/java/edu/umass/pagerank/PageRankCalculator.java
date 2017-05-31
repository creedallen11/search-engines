package edu.umass.pagerank;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of PageRank on an adjacency list graph.
 * @author Creed
 */
public class PageRankCalculator {

    String[] pages;
    HashMap<String, ArrayList> links;
    double[] I;
    double[] R;
    double tau;
    double lambda;
    HashMap<String, Integer> lookup;

    public PageRankCalculator(String[] pages, HashMap<String, ArrayList> links, double lambda, double tau) {
        this.lambda = lambda;
        this.tau = tau;
        this.links = links;
        this.pages = pages;

        I = new double[pages.length];
        lookup = new HashMap<>();

        for (int i = 0; i < pages.length; i++) {
            I[i] = 1.0 / pages.length;
            lookup.put(pages[i], i);
        }
    }

    public void iterate() {
        // PageRank from 4.11P
        boolean converged = false;
        R = new double[pages.length];
        do {
            System.out.println("iterating");
            for (int i = 0; i < R.length; i++) {
                R[i] = lambda / pages.length;
            }

            double accumulator = 0;

            for (int i = 0; i < pages.length; i++) {
                ArrayList<String> Q = links.getOrDefault(pages[i], new ArrayList<>());
                if (Q.size() > 0) {
                    for (String q : Q) {
                        R[lookup.get(q)] = R[lookup.get(q)] + (1 - lambda) * I[i] / Q.size();
                    }
                } else {
                    accumulator += (1 - lambda) * I[i] / pages.length;
                }
            }
            // disperse random surf in 1 sweep of the pages instead of every sink page
            for (int j = 0; j < pages.length; j++) {
                R[j] = R[j] + accumulator;
            }

            converged = hasConverged();
            // Deep copy R <- I
            for (int i = 0; i < I.length; i++) {
                I[i] = R[i];
            }
        } while (!converged);
    }

    /* Helper method to check for convergence of the algorithm */
    public boolean hasConverged() {
        //System.out.println("Check: " + Math.abs(DoubleStream.of(this.I).sum() - DoubleStream.of(this.R).sum()));
        double s = 0;
        for (int k = 0; k < I.length; k++) {
            s += Math.abs(I[k] - R[k]);
        }
        //double difference = Math.abs(Math.sqrt(I_norm) - Math.sqrt(R_norm));
        System.out.println("Difference = " + s);
        return s < tau;
    }

    /* Output of iterate, writes to a local file but should be modified for usage. */
    public void generateTopInlinks(int n) {
        // generate the top n pages by inlinks
        int[] inLinkCounts = new int[pages.length];
        for (Map.Entry<String, ArrayList> entry : links.entrySet()) {
            ArrayList<String> targets = entry.getValue();
            for (String t : targets) {
                inLinkCounts[lookup.get(t)]++;
            }
        }

        Tuple<String, Integer>[] pairs = new Tuple[pages.length];
        for (int i = 0; i < pages.length; i++) {
            pairs[i] = new Tuple<String, Integer>(pages[i], inLinkCounts[i]);
        }
        Arrays.sort(pairs, (a, b) -> a.y.compareTo(b.y)); //a.y.compareTo(b.y)
        Collections.reverse(Arrays.asList(pairs));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("inlinks.txt")))) {
            for (int i = 0; i < n; i++)
                bw.write((i+1) + ". " + pairs[i].x + "\t" + pairs[i].y + "\n");
            bw.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(PageRankCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Another output of iterate, writes to a local file but should be modified for usage. */
    public void generateTopPR(int n) {
        // generate the top n pages by page rank
        Tuple<String, Double>[] pairs = new Tuple[pages.length];
        for (int i = 0; i < pages.length; i++) {
            pairs[i] = new Tuple<>(pages[i], I[i]);
        }
        Arrays.sort(pairs, (a, b) -> a.y.compareTo(b.y)); //a.y.compareTo(b.y)
        Collections.reverse(Arrays.asList(pairs));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("pagerank.txt")))) {
            for (int i = 0; i < n; i++)
                bw.write((i+1) + ". " + pairs[i].x + "\t" + pairs[i].y + "\n");
            bw.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(PageRankCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

