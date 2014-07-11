/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package di.uniba.it.tri.data;

import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class DictionaryEntry implements Comparable<DictionaryEntry> {
    
    private String word;
    
    private int counter;

    public DictionaryEntry(String word, int counter) {
        this.word = word;
        this.counter = counter;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public int compareTo(DictionaryEntry o) {
        return Integer.compare(o.counter, counter);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.word);
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
        final DictionaryEntry other = (DictionaryEntry) obj;
        if (!Objects.equals(this.word, other.word)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DictionaryEntry{" + "word=" + word + ", counter=" + counter + '}';
    }
    
    public void incrementCounter() {
        this.counter++;
    }
    
    public void incrementCounter(int incr) {
        this.counter+=incr;
    }
    
}
