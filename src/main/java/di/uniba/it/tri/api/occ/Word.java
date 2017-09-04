/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.api.occ;

import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class Word implements Comparable<Word> {

    private String word;

    private double score;

    public Word(String word, double score) {
        this.word = word;
        this.score = score;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.word);
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
        final Word other = (Word) obj;
        if (!Objects.equals(this.word, other.word)) {
            return false;
        }
        return true;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Word{" + "word=" + word + ", score=" + score + '}';
    }

    @Override
    public int compareTo(Word o) {
        return Double.compare(this.score, o.score);
    }

}
