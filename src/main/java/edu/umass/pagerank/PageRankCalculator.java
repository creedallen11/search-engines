package edu.umass.pagerank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
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
        //System.out.println("I=" + DoubleStream.of(I).sum());
    }

    public void iterate() {
        // PageRank from 4.11
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
            // disperse random surf in 1 sweet of the pages instead of every sink page
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

    public void generateTopInlinks(int n) {
        // generate the top n pages by inlinks
        int[] inLinkCounts = new int[pages.length];
        for (Map.Entry<String, ArrayList> entry : links.entrySet()) {
            ArrayList<String> targets = entry.getValue();
            for (String t : targets) {
                inLinkCounts[lookup.get(t)]++;
            }
        }
        //System.out.println("Targets count: " + IntStream.of(inLinkCounts).sum());

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

