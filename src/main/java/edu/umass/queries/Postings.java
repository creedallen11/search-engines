package edu.umass.queries;

public class Postings {
    Document doc;
    int position;
    public Postings(Document d, int p) {
        doc = d;
        position = p;
    }
    @Override
    public String toString() {
        return "Doc = " + this.doc + " Position = " + this.position;
    }

}

