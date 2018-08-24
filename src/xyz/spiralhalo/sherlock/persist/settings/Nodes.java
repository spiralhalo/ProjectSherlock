package xyz.spiralhalo.sherlock.persist.settings;

public enum Nodes {
    NODE_APPLICATION("APPLICATION"),
    NODE_VIEW("VIEW"),
    NODE_TRACKING("TRACKING")
            ;
    public final String v;
    Nodes(String v){this.v = v;}
}
