/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

/**
 *
 * @author pierpaolo
 */
public class NgramSearchResult implements Comparable<NgramSearchResult> {
    
    private String ngram;
    
    private float score;
    
    private int count;

    public NgramSearchResult(String ngram, float score) {
        this.ngram = ngram;
        this.score = score;
    }

    public NgramSearchResult(String ngram, float score, int count) {
        this.ngram = ngram;
        this.score = score;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNgram() {
        return ngram;
    }

    public void setNgram(String ngram) {
        this.ngram = ngram;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "NgramSearchResult{" + "ngram=" + ngram + ", score=" + score + '}';
    }

    @Override
    public int compareTo(NgramSearchResult o) {
        return Float.compare(o.score, score);
    }
    
    
    
}
