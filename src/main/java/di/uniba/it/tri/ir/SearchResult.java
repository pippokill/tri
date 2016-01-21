/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.ir;

import java.util.Date;

/**
 *
 * @author pierpaolo
 */
public class SearchResult implements Comparable<SearchResult> {

    private int docid;

    private float score;

    private String id;

    private String text;

    private String source;
    
    private Date date;

    public SearchResult() {
    }

    public SearchResult(int docid, float score) {
        this.docid = docid;
        this.score = score;
    }

    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.docid;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchResult other = (SearchResult) obj;
        if (this.docid != other.docid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id + "\t" + text;
    }

    @Override
    public int compareTo(SearchResult o) {
        return Float.compare(o.score, score);
    }

}
