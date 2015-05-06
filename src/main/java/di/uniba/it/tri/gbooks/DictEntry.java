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
public class DictEntry implements Serializable {
    
    private int wordId;
    
    private int count;

    public DictEntry(int wordId, int count) {
        this.wordId = wordId;
        this.count = count;
    }

    public DictEntry() {
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.wordId;
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
        final DictEntry other = (DictEntry) obj;
        if (this.wordId != other.wordId) {
            return false;
        }
        return true;
    }
    
}
