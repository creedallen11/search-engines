package edu.umass.pagerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Preprocess file for use with PageRankCalculator.
 * @author Creed
 */
public class PreProcessing {
    HashSet<String> sources; // unique source list
    HashMap<String, ArrayList> links; // source -> links on source page
    String outFileName;

    public PreProcessing(String t) {
        outFileName = t;
        sources = new HashSet<>();
        links = new HashMap<>();
    }

    /* Populate the sources and links data structures for use in PageRank */
    public void buildGraph() {
        try (BufferedReader br = new BufferedReader(new FileReader(outFileName))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String[] tokens = sCurrentLine.split("\t"); //
                String s = tokens[0]; String d = tokens[1];
                // Ensure nodes exist for each page seen
                sources.add(s);
                sources.add(d);

                // Add dest to source if source exists, else create source->[link....] in links
                if (links.containsKey(s)) {
                    ArrayList<String> old = links.get(s);
                    old.add(d);
                    links.put(s, old);
                }
                else {
                    ArrayList<String> n = new ArrayList<>();
                    n.add(d);
                    links.put(s, n);
                }
            }
            br.close();

            for (String key: links.keySet()) {
                ArrayList<String> t = links.get(key);
                t.removeIf(dest -> !sources.contains(dest));
                links.put(key, t);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getSortedSources() {
        Object[] out = sources.toArray();
        String[] stringArray = Arrays.copyOf(out, out.length, String[].class);
        Arrays.sort(stringArray);
        return stringArray;
    }

    public HashMap<String, ArrayList> getLinks() {
        int linkCount = 0;
        for (Map.Entry<String, ArrayList> entry : links.entrySet()) {
            ArrayList value = entry.getValue();
            linkCount = linkCount + value.size();
        }
        return links;

    }

    public HashSet<String> getSources() {
        return sources;
    }
}
