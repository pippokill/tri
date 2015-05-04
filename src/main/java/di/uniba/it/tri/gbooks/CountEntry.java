/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class CountEntry implements Serializable {

    private int wordId;

    private int year;

    private int count;

    public CountEntry(int wordId, int year, int count) {
        this.wordId = wordId;
        this.year = year;
        this.count = count;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.wordId;
        hash = 23 * hash + this.year;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountEntry other = (CountEntry) obj;
        if (this.wordId != other.wordId) {
            return false;
        }
        if (this.year != other.year) {
            return false;
        }
        return true;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CountEntry{" + "wordId=" + wordId + ", year=" + year + ", count=" + count + '}';
    }

}
