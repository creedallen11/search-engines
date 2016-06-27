package edu.umass.crawler;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Crawler Thread
 * @author Creed
 */
public class Crawler {
    public final int UNIQUE_LINKS = 1000; // number of links to find
    public final String[] validHosts = {"cs.umass.edu", "ciir.cs.umass.edu", "cics.umass.edu" };
    Queue<URL> frontier;
    HashSet<URL>  foundURLs;
    ArrayList<URL> uniqueList;
    HashMap<String, Long> delayTracker;
    HashMap<String, ArrayList<String>> robots; // host -> resources not allowed under robots
    ArrayList<String> foundHosts;

    public Crawler(String[] seeds) {
        robots = new HashMap<>();
        frontier = new LinkedList<>();
        foundURLs = new HashSet<>();
        uniqueList = new ArrayList<>();
        delayTracker = new HashMap<>();
        foundHosts = new ArrayList<>();

        for (String seed : seeds) {
            URL newURL;
            try { // Add seed URLs to frontier and mark them as seen
                newURL = new URL(seed);
                frontier.add(newURL);
            } catch (MalformedURLException ex) {
                System.out.println("Malformed Seed URL not added " + seed);
            }
        }
    }

    /* Extract links on a page */
    public void extractLinks(URL currentURL) {
        try {
            Document doc =  Jsoup.connect(currentURL.toString()).get();
            Elements links = doc.select("a[href]");
            links.stream().map((link) -> link.attr("abs:href")).forEach((found_link) -> {
                URL found_URL;
                try {
                    found_URL = new URL(found_link);
                    if (!foundURLs.contains(found_URL) && !found_URL.toString().contains("mailto:")) {
                        // URL OK, add it to crawl.
                        //&& isValidHost(found_URL) <-- used in part A
                        frontier.add(found_URL);
                        uniqueList.add(found_URL);
                    }
                    foundURLs.add(found_URL); // mark all URLs as found
                } catch (IOException ex) {
                    System.out.println("Malformed " + found_link);
                }
            });
        } catch (IOException ex) {
            System.out.println("Jsoup failed to load on: " + currentURL.toString());
        }
    }

    /* A valid host is one not disallowed on robots.txt */
    public boolean isValidHost(URL link) {
        String host = link.getHost();
        for (String vHosts: validHosts)
            if (vHosts.equals(host)) return true;
        return false;
    }

    public void logRetrievalTime(URL link) {
        long currentTime = System.currentTimeMillis();
        delayTracker.put(link.getHost(), currentTime);
    }

    public boolean sufficientDelay(URL link) {
        String host = link.getHost();
        long current = System.currentTimeMillis();
        long diff = current - delayTracker.getOrDefault(host, 0L);
        return (diff >= 5000); // true only if we havent pinged in past 5 sec
    }

    public void parseRobots(URL url) {
        // MAKES THE ASSUMPTION DISALLOWS REFER TO OUR CRAWLER
        URL robotsLink;
        String host = url.getHost();
        try {
            foundHosts.add(host);
            robotsLink = new URL("http://" + host + "/robots.txt");
            //System.out.println("Found a robots file");
        }
        catch (Exception e) {
            System.out.println("URL is likely malformed");
            return;
        }

        try {
            InputStream s = robotsLink.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Disallow:")) {
                    int i = "Disallow:".length();
                    String resource = line.substring(i);
                    StringTokenizer t = new StringTokenizer(resource);
                    // Add the next token after disallow, this is the forbidden resource
                    if (!t.hasMoreTokens()) break;
                    else {
                        ArrayList<String> temp = robots.getOrDefault(url.getHost(), new ArrayList<>());
                        String r = t.nextToken();
                        temp.add(r);
                        robots.put(url.getHost(), temp);
                    }
                }
            }
        } catch (Exception ex) {} // no robots file found crawl OK
    }

    public boolean notInRobots(URL link) {
        // Check to see if a URL's resource is in the host's disallowed.
        String host = link.getHost();
        ArrayList<String> forbidden = robots.getOrDefault(host, new ArrayList<>());
        if (!forbidden.isEmpty()) {
            for (String resource: forbidden) {
                if (link.toString().contains(resource)) return false;
            }
        }
        return true;
    }

    public void crawlFrontier() {
        int pagesCrawled = 0;
        while (!frontier.isEmpty() && uniqueList.size() < UNIQUE_LINKS) {
            URL currentURL = frontier.remove();
            // Host hasn't been seen, get its robots.txt
            if (!foundHosts.contains(currentURL.getHost()))
                parseRobots(currentURL);
            // Resource is on disallow list check

            if (!notInRobots(currentURL)) {
                //System.out.println("Restricted by robots.txt");
                continue;
            }

            if (sufficientDelay(currentURL)) { // respect 5 sec delay w/ hosts
                pagesCrawled++;
                extractLinks(currentURL);
                logRetrievalTime(currentURL);
            }
            else { // revisit after sufficient delay
                frontier.add(currentURL);
            }
        }
        // Print out links
        uniqueList.stream().forEach((u) -> {
            System.out.println(u);
        });
        System.out.println("CRAWLED " + pagesCrawled + " pages." +
                " finding " + uniqueList.size() + " unique pages.");
    }
}

