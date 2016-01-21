/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.shell.gui.data;

import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class WordEntry {

    private String word;

    private String year;

    public WordEntry(String word, String year) {
        this.word = word;
        this.year = year;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.word);
        hash = 37 * hash + Objects.hashCode(this.year);
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
        final WordEntry other = (WordEntry) obj;
        if (!Objects.equals(this.word, other.word)) {
            return false;
        }
        if (!Objects.equals(this.year, other.year)) {
            return false;
        }
        return true;
    }

    public String getKeyLabel() {
        return this.word + "_" + this.year;
    }

    @Override
    public String toString() {
        return word + " (" + year + ")";
    }

}
