package edu.umass.tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tokenization class for project 2 CMPSCI 446
 * @author Creed Allen
 */
public class Tokenizer {
    ArrayList<String> term_array; // the term array for output of part A
    HashSet<String> stop_words; // stop word list
    File f;
    HashSet<String> unique_words; int collection; // used for question 6


    /**
     * Constructor that executes the rules, stopping & Porter stemming.
     * @param f File to tokenize.
     * @param target, output for the term array, note if running top 200 words
     * as well, this will be overwritten.
     */
    public Tokenizer(File f, String target) throws FileNotFoundException, UnsupportedEncodingException {
        term_array = new ArrayList<>();
        unique_words = new HashSet<>();
        this.f = f;
        try {
            stop_words = buildStopWords("stopwords.txt");
            System.out.println("SUCCESS: Built stop_words.");
        } catch (Exception e) {
            System.out.println("WARNING: stop_words failed to build and is empty.");
            stop_words = new HashSet<>();
        }

        PrintWriter data = new PrintWriter("data.txt", "UTF-8");
        for (String w: preProcessLine(this.readFile(f)).split(" ")) {
            collection++;
            if (w.length() > 0) {
//                // NOTE: FOLLOWING IF CLAUSE CAN BE COMMENTED OUT, USED FOR P6 DATA COLLECTION
//                if (!unique_words.contains(w)) {
//                    unique_words.add(w);
//                    if (collection % 100 == 0) // note x,y coordinates every 100 words
//                        data.append(Integer.toString(collection) + ", " + Integer.toString(unique_words.size()) + "\n");
//                }
                if (!isStopWord(w)) {
                    term_array.add(w);
                    unique_words.add(w);
                }
            }
        }
        data.close();

        // At this point, we have taken in the file applied token rules and stopping.
        // Next, apply Porter Stemming rule 1a & 1b.

        for (int i = 0; i < term_array.size(); i++) term_array.set(i, porterStem(term_array.get(i)));
        // write term array to target
        PrintWriter writer = new PrintWriter(target, "UTF-8");
        for (String word : term_array)
            writer.append(word.trim() + "\n");
        writer.close();



        // COMMENT THIS OUT FOR PART A RUN
        // Writes top 200 terms to terms.txt
        //this.top_200_word_counts();
    }

    /**
     * Takes in the stopwords.txt file and builds a HashSet for the tokenizer
     * class that contains all stop words.
     * @param s String representing stopword file.
     * @throws FileNotFoundException Handled by caller.
     * @throws IOException  Handled by caller.
     */
    public HashSet<String> buildStopWords(String s) throws FileNotFoundException, IOException {
        stop_words = new HashSet<>();

        FileReader fr = new FileReader(new File(s));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
            stop_words.add(line.trim());
        fr.close();
        return stop_words;
    }

    /**
     * Converts a file to a string to be processed by tokenizer.
     * @param file input file to tokenize
     * @return string of file contents
     */
    public String readFile(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                sb.append(line + " ");
                line = br.readLine();
            }
            fr.close();
            return sb.toString();
        } catch (FileNotFoundException ex) {
            System.out.println("In file not found.");
        } catch (IOException ex) {
            Logger.getLogger(Tokenizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();

    }

    public String preProcessLine(String in) {
        ArrayList<String> out = new ArrayList<>();
        String[] input = in.toLowerCase().replaceAll("[^a-z. 0-9]", " ").trim().split(" ");
        for (String word: input) {
            out.add(word.replaceAll
                    ("(?<=(^|\\.)[\\S&&\\D])\\.(?=[\\S&&\\D](\\.|$))", "").replace('.', ' '));
        }
        StringBuilder sb = new StringBuilder();
        for (String word: out)
            sb.append(word.trim() + " ");
        return sb.toString();
    }


    public boolean isStopWord(String word) {
        return stop_words.contains(word);
    }

    /**
     * Porter stems the word, details given in README.
     * @param word
     * @return
     */
    public String porterStem(String word) {
        int last = word.length() - 1;

        // 2 sses->ss   ex stresses -> stress
        if (word.length() > 4 && word.substring(last-3).equals("sses"))
            return word.substring(0, last -1);

        // replace ied or ies by i if preceded by more than 1 letter, otherwise ie
        if (word.length() > 3 && (word.substring(last-2).equals("ies") || word.substring(last-2).equals("ied") )) {
            if (word.length() > 4) return word.substring(0, last - 1);
            else return word.substring(0, last);
        }

        // if suffix is us or ss do nothing.
        if (word.length() > 1 && (word.substring(last-1).equals("us") || word.substring(last-1).equals("ss")))
            return word;

        // delete s if the preceding word part contained a vowel not immediately before the s
        if (word.length() > 2 && word.charAt(last) == 's' && containsVowel(word.substring(0, last-1)))
            return word.substring(0, last);

        //replace eed, eedly -> ee if if it is in the part of the word after the first
        //non vowel following a vowel.
        if (word.length() > 6 && word.substring(last-4).equals("eedly" ) && vowelThenCons(word.substring(0, last-4)))
            return word.substring(0, last-2);
        if (word.length() > 4 && word.substring(last-2).equals("eed" ) && word.length() > 4 && vowelThenCons(word.substring(0, last-2)))
            return word.substring(0, last);


        //     * delete ed, edly, ing, ingly if the preceding word part
        //     * contains a vowel and then if the word ends in at, bl or iz
        //     * add e ex.
        //       fished->fish, pirating->pirate. or if the word ends with
        //     * a double letter that is not ll ss or zz, remove the last letter
        //     * ex. falling->fall, dripping->drip, or if the word is short, add e
        //     * ex. hoping-> hope

        if (word.length() > 2 && word.substring(last-1).equals("ed") && containsVowel(word.substring(0, last-1)))
            return auxillary(word.substring(0, last-1));

        if (word.length() > 4 && word.substring(last-3).equals("edly") && containsVowel(word.substring(0, last-3)))
            return auxillary(word.substring(0, last-3));

        if (word.length() > 3 && word.substring(last-2).equals("ing") && containsVowel(word.substring(0, last-2)))
            return auxillary(word.substring(0, last - 2));

        if (word.length() > 5 && word.substring(last-4).equals("ingly") && containsVowel(word.substring(0, last-4)))
            return auxillary(word.substring(0, last-4));

        return word;
    }

    public void top_200_word_counts() throws FileNotFoundException, UnsupportedEncodingException {
        HashMap<String, Integer> counts = new HashMap();
        for (String word: term_array) {
//            if (word.length() < 1) continue; line was used to look at pre stopped results.
            if (!counts.containsKey(word))
                counts.put(word, 1);
            else
                counts.put(word, counts.get(word) + 1);
        }

        PrintWriter writer = new PrintWriter("terms.txt", "UTF-8");
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(200).forEach(writer::println);
        writer.close();
        //System.out.println(term_array);

    }

    public static boolean isVowel(char ch) {
        return (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u');
    }

    public static boolean containsVowel(String s) {
        for (int i = 0; i < s.length(); i++)
            if (isVowel(s.charAt(i))) return true;
        return false;
    }

    public static boolean vowelThenCons(String s) {
        for (int i = 0; i < s.length()-1; i++)
            if (isVowel(s.charAt(i)) && !isVowel(s.charAt(i+1))) return true;
        return false;
    }

    public static boolean consThenVowel(String s) {
        for (int i = 0; i < s.length()-1; i++)
            if (!isVowel(s.charAt(i)) && isVowel(s.charAt(i+1))) return true;
        return false;
    }

    // ASSUMES LENGTH IS AT LEAST 2, USED FOR PORTER STEMMING, README.
    public static String auxillary(String s) {
        if (s.substring(s.length()-2).equals("at") || s.substring(s.length()-2).equals("bl") || s.substring(s.length()-2).equals("iz"))
            return s + "e";
        else if (s.charAt(s.length()-1) == s.charAt(s.length()-2) && s.charAt(s.length()-1) != 'l'
                && s.charAt(s.length()-1) != 's' && s.charAt(s.length()-1) != 'z')
            return s.substring(0, s.length()-1);
        else if (s.length() < 4) return s + "e";
        else return s;
    }


}