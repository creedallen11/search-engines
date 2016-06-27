package edu.umass.queries;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Methods {

    public static void toFile(List l, String f) throws IOException {
        FileWriter writer = new FileWriter(f);
        for (Object str : l) {
            writer.write(str + "\n");
        }
        writer.close();
    }

    public static void main(String[] args) throws ParseException, IOException {
        Map<String, List<Postings>> index = generateIndex(); // inverted index

        Map<String, Integer> q1 = queryHelper(index, "thee thou", "sceneID");
        Map<String, Integer> q2 = queryHelper(index, "you", "sceneID");
        List<String> sortedt0 = new ArrayList(countQuery(q1, q2)); // save out to t0
        Collections.sort(sortedt0);
        toFile(sortedt0, "terms0.txt");

        Map<String, Integer> t1 = queryHelper(index, "verona italy rome", "sceneID");
        List<String> sortedt1 = new ArrayList(t1.keySet());
        Collections.sort(sortedt1);
        toFile(sortedt1, "terms1.txt");

        Map<String, Integer> t2 = queryHelper(index, "falstaff", "playID");
        List<String> sortedt2 = new ArrayList(t2.keySet());
        Collections.sort(sortedt2);
        toFile(sortedt2, "terms2.txt");

        Map<String, Integer> t3 = queryHelper(index, "soldier", "playID");
        List<String> sortedt3 = new ArrayList(t3.keySet());
        Collections.sort(sortedt3);
        toFile(sortedt3, "terms3.txt");

        String p0 = "lady macbeth";
        List<String> sortedp0 = phraseQuery(index, p0);
        HashSet<String> sp0 = new HashSet(sortedp0);
        sortedp0 = new ArrayList(sp0);
        Collections.sort(sortedp0);
        toFile(sortedp0, "phrase0.txt");

        String p1 = "a rose by any other name";
        List<String> sortedp1 = phraseQuery(index, p1);
        Collections.sort(sortedp1);
        toFile(sortedp1, "phrase1.txt");

        String p2 = "cry havoc";
        List<String> sortedp2 = phraseQuery(index, p2);
        Collections.sort(sortedp2);
        toFile(sortedp2, "phrase2.txt");

    }

    public static ArrayList<String> phraseQuery(Map<String, List<Postings>> index, String p) {
        // return scenes in which phrase was mentioned
        // assumes at least 1 term
        String[] phrase = p.split("\\s+");

        List<Postings> t1 = index.getOrDefault(phrase[0], new LinkedList<Postings>());


        for (int i = 1; i < phrase.length; i++) {
            List<Postings> t2 = index.getOrDefault(phrase[i], new LinkedList<Postings>());
            t1 = containsNext(t1, t2);
        } // save postings of terms that came after a previous term in same play/scene

        ArrayList<String> out = new ArrayList<>();
        for (Postings t : t1) {
            out.add(t.doc.sceneID);
        } // convert to sceneIDs and return
        return out;
    }

    public static ArrayList<Postings> containsNext(List<Postings> prev, List<Postings> cur) {
        ArrayList<Postings> candidates = new ArrayList<>();
        for (Postings p : prev) {
            for (Postings c : cur) {
                if (p.doc.playID.equals(c.doc.playID) && c.doc.sceneID.equals(p.doc.sceneID)
                        && c.position == p.position + 1) {
                    // proceed with these postings
                    candidates.add(c);
                }
            }
        }
        return candidates;
    }

    public static HashSet<String> countQuery(Map<String, Integer> q1, Map<String, Integer> q2) {
        HashSet<String> out = new HashSet<>();
        for (String scene : q1.keySet()) {
            if (q1.get(scene) > q2.getOrDefault(scene, 0)) {
                out.add(scene);
            }
        }
        return out;
    }


    public static Map<String, Integer> queryHelper(Map<String, List<Postings>> index, String q, String type) {
        String[] terms = q.split("\\s+");
        Map<String, Integer> count = new HashMap<>();
        // RETURNS MAP OF SCENE -> COUNT OF TERMS
        if (type.equals("sceneID")) {
            for (String term : terms) {
                List<Postings> f = index.getOrDefault(term, new LinkedList<Postings>());
                for (Postings p : f) {
                    if (count.containsKey(p.doc.sceneID)) {
                        count.put(p.doc.sceneID, count.get(p.doc.sceneID) + 1);
                    } else {
                        count.put(p.doc.sceneID, 1);
                    }
                }
            }
        } else {
            for (String term : terms) {
                List<Postings> f = index.getOrDefault(term, new LinkedList<Postings>());
                for (Postings p : f) {
                    if (count.containsKey(p.doc.playID)) {
                        count.put(p.doc.playID, count.get(p.doc.playID) + 1);
                    } else {
                        count.put(p.doc.playID, 1);
                    }

                }//System.out.println(count);

            }
        }
        return count;
    }

    public static Map<String, List<Postings>> generateIndex() throws IOException, ParseException {

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(new FileReader("shakespeare-scenes.json"));
        JSONObject jsonObject = (JSONObject) obj;
        String text = jsonObject.get("corpus").toString();

        Object obj2 = JSONValue.parse(text);
        JSONArray array = (JSONArray) obj2;

        Map<String, List<Postings>> index = new HashMap<>(); // term->posting list
        HashMap<String, Integer> playCounts = new HashMap<>();
        HashMap<String, Integer> sceneCounts = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);

            String sid = (String) object.get("sceneId");
            String pid = (String) object.get("playId");
            long sn = (long) object.get("sceneNum");
            Document d = new Document(sid, pid, sn);

            //keyset = [playId, sceneId, text, sceneNum]
            String[] words = ((String) object.get("text")).split("\\s+");
            sceneCounts.put(sid, words.length);
            if (playCounts.containsKey(pid)) {
                playCounts.put(pid, playCounts.get(pid) + words.length);
            }
            else {
                playCounts.putIfAbsent(pid, words.length);
            }


            for (int j = 0; j < words.length; j++) {
                if (words[j].length() >= 1) {
                    if (index.containsKey(words[j])) {
                        List<Postings> temp = index.get(words[j]);
                        temp.add(new Postings(d, j));
                        index.put(words[j], temp);
                    } else {
                        List<Postings> temp = new LinkedList<>();
                        temp.add(new Postings(d, j));
                        index.put(words[j], temp);
                    }
                }
            }
        }
        // Print out scene data
        String minScene = ""; int minSceneLen = Integer.MAX_VALUE;
        int total = 0; int counter = 0;
        for (String s: sceneCounts.keySet()) {
            if (sceneCounts.get(s) < minSceneLen) {
                minScene = s; minSceneLen = sceneCounts.get(s);
            }
            total += sceneCounts.get(s); counter++;
        }
        System.out.println(minScene + " " + minSceneLen);
        System.out.println("COUNTER " + counter);
        System.out.println("average Scene = " + (double)total / (double)counter);


        String minPlay = ""; String maxPlay = "";
        int maxLen = 0; int minLen = Integer.MAX_VALUE;
        // Print out play data
        for (String play: playCounts.keySet()) {
            if (playCounts.get(play) > maxLen) {
                maxLen = playCounts.get(play); maxPlay = play; }
            if (playCounts.get(play) < minLen) {
                minLen = playCounts.get(play); minPlay = play;}
        }
        System.out.println("MinPlay = " + minPlay + " MaxPlay = " + maxPlay);
        return index;
    }

}

