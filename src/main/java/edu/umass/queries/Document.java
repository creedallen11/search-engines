package edu.umass.queries;

public class Document {
    String sceneID;
    String playID;
    long sceneNum;
    public Document(String sid, String pid, long sn) {
        sceneID = sid;
        playID = pid;
        sceneNum = sn;
    }

    public String toString() {
        return "Play = " + playID + " Scene =" + sceneID;
    }
}

