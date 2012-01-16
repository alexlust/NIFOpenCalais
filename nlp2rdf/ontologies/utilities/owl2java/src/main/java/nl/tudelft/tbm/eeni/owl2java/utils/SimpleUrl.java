package nl.tudelft.tbm.eeni.owl2java.utils;

import java.io.Serializable;

public class SimpleUrl implements Serializable, Comparable<SimpleUrl> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String url;

    public SimpleUrl() {
        url = "";
    }

    public SimpleUrl(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String toString() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public int compareTo(SimpleUrl o) {
        return url.compareTo(o.getUrl());
    }

}
