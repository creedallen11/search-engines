## search-engines
mini-projects that demonstrate process' in IR

## 1. [Web Crawler](src/main/java/edu/umass/crawler/Crawler.java)
This is an example of a single thread web crawler. The data structures in it are not designed to scale as this crawler is designed to demonstrate a basic algorithm for a web crawling node. Scaling would likely involve replacing the in memory status' of the data structures with NoSQL databases.
**Instance Data Structures**
- **validHosts** - Used to restrict the crawl to a specific set of hosts.
- **frontier** (Queue of URLs) - Collection of URLs that are candidates for link extraction.
- **foundURLs** (Hashset) - URLs that have been seen. Provides constant contains and add methods at the cost of space. Possible alternatives: Trie
- **uniqueList** (ArrayList of URLs) - This is the crawler output. Since the crawler only stores a small number of URLs (1000) I put this in contiguous memory. A larger crawler would likely write these to storage along with other data in the URLs.
- **delayTracker** (String -> Long) - Logs the epoch time since a host was visited.
- **robots** (String -> ArrayList of Strings) - maps hosts to their disallowed resources in their respective robots.txt file.
- **foundHosts** (ArrayList of Strings) - These are hosts that have had their robots.txt examined. This should be a Set as we only ever need to do contains and add operations, which are O(1). The current implementation is O(|unique hosts|).

**Class Methods**
- **extractLinks(URL currentURL)** - Removes currentURL from the frontier and seeks out all valid links (unexplored documents and HTML) to add them to the uniqueList and the frontier.O(|links|).
- **isValidHost(URL link)** - Used in the optional feature that limits the crawl to a collection of hosts, for my IR class this was the CIIR UMass hosts. - **logRetrievalTime(URL link)** - Helper to set the time a links host was last crawled. O(1).
- **sufficientDelay(URL link)** - Helper to make sure we don't overload hosts while crawling. As written a sufficient delay is 5 seconds. O(1).
- **parseRobots(URL url)** - If a new host is encountered, this method is called to create an entry in robots referencing all disallowed resources on the hosts robots.txt O(1).
- **notInRobots(URL link)** - Checks a link against it's hosts robots entry to make sure it is OK to crawl. O(|disallowed|), usually near O(1).
- **crawlFrontier** - This generates the link collection. Frontier crawling is done with respect to sufficientDelay and entries at the front of the queue are pushed to the back if they are not ready to crawl. URLs are crawled one at a time until the frontier is empty or the target link limit is reached.

## 2. Preprocessing (tokenizer)

## 3. PageRank (pagerank)

## 4. Querying
