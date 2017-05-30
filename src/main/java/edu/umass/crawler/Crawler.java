package edu.umass.crawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Example Crawler Thread
 * @author Creed Allen
 * Descriptions of crawler functions and data structures as well as big-O decisions are given in the README.
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

        // Initialize a frontier with URLs to explore.
        for (String seed : seeds) {
            URL newURL;
            try {
                newURL = new URL(seed);
                frontier.add(newURL);
            } catch (MalformedURLException ex) {
                System.out.println("Malformed Seed URL not added " + seed);
            }
        }
    }

    /*
    Takes a URL and finds all the links on it's page. Valid links are added to the frontier and all links are marked
    as found so they are not processed again when encountered on another page.
     */
    public void extractLinks(URL currentURL) {
        try {
            Document currentDoc =  Jsoup.connect(currentURL.toString()).get();
            Elements links = currentDoc.select("a[href]");

            links.stream().map((link) -> link.attr("abs:href")).forEach((found_link) -> {
                URL found_URL;
                try {
                    found_URL = new URL(found_link);
                    if (!foundURLs.contains(found_URL) && !found_URL.toString().contains("mailto:")) {
                        // URL OK, add it to crawl.
                        frontier.add(found_URL);
                        uniqueList.add(found_URL);
                    }
                    foundURLs.add(found_URL);
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

    /* Returns true if host hasn't been targeted in under 5 sec */
    public boolean sufficientDelay(URL link) {
        String host = link.getHost();
        long current = System.currentTimeMillis();
        long diff = current - delayTracker.getOrDefault(host, 0L);
        return (diff >= 5000); // true only if we havent pinged in past 5 sec
    }

    /* Checks a host for crawler exceptions in the robots.txt file. parseRobots makes assumptions about
     * the format of a robots.txt file. */
    public void parseRobots(URL url) {
        // MAKES THE ASSUMPTION DISALLOWS REFER TO OUR CRAWLER
        URL robotsLink;
        String host = url.getHost();
        try {
            foundHosts.add(host);
            robotsLink = new URL("http://" + host + "/robots.txt");
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

    /* If a resource's host has a robots.txt on file with the crawler, this function will confirm crawling of the
     resource is allowed.
      */
    public boolean notInRobots(URL link) {
        String host = link.getHost();
        ArrayList<String> forbidden = robots.getOrDefault(host, new ArrayList<>());
        if (!forbidden.isEmpty()) {
            for (String resource: forbidden) {
                if (link.toString().contains(resource)) return false;
            }
        }
        return true;
    }


    /* Crawl the frontier for new links. For this simple implementation a crawl will conclude after the
     * unique links count is reached or the frontier is empty (unlikely). */
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

        // From here links and status' can be saved to a file or database for further use.
        // Included in sample output is a run on the ciir domains above.
    }
}

