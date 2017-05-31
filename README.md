## search-engines
mini-projects that demonstrate process' in Information Retrieval
Note: Process' are not designed to scale and merely demonstrate common algorithms explored in an IR class.

## 1. [Web Crawler](src/main/java/edu/umass/crawler/Crawler.java)
This is an example of a single thread web crawler. The data structures in it are not designed to scale as this crawler is designed to demonstrate a basic algorithm for a web crawling node. Scaling would likely involve replacing the in memory status' of the data structures with NoSQL databases. **ToRun:** Instantiate a crawler with a list of start URLs(as String) and call the crawlFrontier method.
**Instance Variables**
- **validHosts** - Used to restrict the crawl to a specific set of hosts.
- **frontier** (Queue of URLs) - Collection of URLs that are candidates for link extraction.
- **foundURLs** (Hashset) - URLs that have been seen. Provides constant contains and add methods at the cost of space. Possible alternatives: Trie
- **uniqueList** (ArrayList of URLs) - This is the crawler output. Since the crawler only stores a small number of URLs (1000) I put this in contiguous memory. A larger crawler would likely write these to storage along with other data in the URLs.
- **delayTracker** (String -> Long) - Logs the epoch time since a host was visited.
- **robots** (String -> ArrayList of Strings) - maps hosts to their disallowed resources in their respective robots.txt file.
- **foundHosts** (ArrayList of Strings) - These are hosts that have had their robots.txt examined. This should be a Set as we only ever need to do contains and add operations, which are O(1). The current implementation is O(|unique hosts|).

**Class Methods**
- **extractLinks(URL currentURL)** - Removes currentURL from the frontier and seeks out all valid links (unexplored documents and HTML) to add them to the uniqueList and the frontier.O(|links|).
- **isValidHost(URL link)** - Used in the optional feature that limits the crawl to a collection of hosts, for my IR class this was the CIIR UMass hosts.
- **sufficientDelay(URL link)** - Helper to make sure we don't overload hosts while crawling. As written a sufficient delay is 5 seconds. O(1).
- **parseRobots(URL url)** - If a new host is encountered, this method is called to create an entry in robots referencing all disallowed resources on the hosts robots.txt O(1).
- **crawlFrontier** - This generates the link collection. Frontier crawling is done with respect to sufficientDelay and entries at the front of the queue are pushed to the back if they are not ready to crawl. URLs are crawled one at a time until the frontier is empty or the target link limit is reached.

## 2. [Preprocessing](src/main/java/edu/umass/tokenizer/Tokenizer.java)
Applies TREC tokenization, stop-word removal and Porter Stemming to a document body.
**How tokenizer works**
1. Open a document as a String using readFile.
2. Remove stop words and trim punctuation.
3. Apply Porter stemming to the document and save modified document.

**Class Methods**
- **readFile** - File->String the document body using StringBuilder.
- **preprocess** - Document is lowercased and regex to replace punctuation is applied.
- **porterStem** - [See here](http://facweb.cs.depaul.edu/mobasher/classes/csc575/papers/porter-algorithm.html) Apply Porter Stemming rules to the document body. The static functions in this class are all helper functions for Porter Stemming rule application.

**How to Use**: Instantiate a stemmer with a file to modify and the output file name.

## 3. [PageRank](src/main/java/edu/umass/pagerank/)
This section generates a link graph for processing by the [PageRank](https://en.wikipedia.org/wiki/PageRank) Random Surfer Model ranking algorithm.
**Parameters**
- **lambda, tau** - variables described in the algorithm linked above. Tau will determine convergence of PR.
- **pages**- A copy of the keys from links (all candidates to rank).
- **links** - Adjacency list representation of graph.

**How PageRank Class Works**
The class constructor prepares the algorithm to run by binding all the instance variables and seeting up the I array. To run the algorithm call **iterate**, this function will loop the algorithm until the difference in iterations is less than tau.
Now that the PR instance has a ranking, the next step is to generate the top results using **generateTopPR**, this method writes to a local file the top n ranked pages.

## 4. [Querying](src/main/java/edu/umass/queries/)
This section takes a catalog of all the works of Shakespeare in JSON format and generates an inverted index in order to perform term and phrase queries on the document collection.
The **Postings** and **Document** objects are used to organize metadata and generate position based results. Output of queries are written to local files.

## 5. [Metrics](src/main/java/edu/umass/metrics/)
The last section attempts to evaluate query performance based on the following rank outputs:
1. NDCG@20
2. P@5
3. P@10
4. Recall@10
5. F1@10
6. MAP

The performance of these search outputs is based on user judgement of search relevance, generated as **Judgements** class objects. The rank outputs are given as static class methods of Metrics. More details on these methods can be found on wiki https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval).

**ToRun**: The main in Queries evaluates the results of trecruns included in the resources folder of SDM, BM25, Stress, Q-Relevance and Q-Liklihood runs against the judgements.
