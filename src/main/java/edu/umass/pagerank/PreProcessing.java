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
 * Preprocess file for user with PageRankCalculator.
 * @author Creed
 */
public class PreProcessing {
    HashSet<String> sources; // unique source list
    HashMap<String, ArrayList> links; // source -> list of dests
    String target;

    public PreProcessing(String t) {
        target = t;
        sources = new HashSet<>();
        links = new HashMap<>();
    }

    public void buildGraph() { // build the link and source list
        try (BufferedReader br = new BufferedReader(new FileReader(target))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String[] tokens = sCurrentLine.split("\t"); //
                String s = tokens[0]; String d = tokens[1];
                sources.add(s);
                sources.add(d);

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
            System.out.println("Unique Sources: " + sources.size());

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
        //System.out.println("Total # of links: " + linkCount);
        return links;

    }

    public HashSet<String> getSources() {
        return sources;
    }
}
