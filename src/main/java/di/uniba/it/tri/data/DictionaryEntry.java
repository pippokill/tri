/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */

package di.uniba.it.tri.data;

import java.util.Objects;

/**
 * This paper describes a single entry in the dictionary. Each entry is composed of a word and its occurrences
 * @author pierpaolo
 */
public class DictionaryEntry implements Comparable<DictionaryEntry> {
    
    private String word;
    
    private int counter;

    /**
     * Create a new dictionary entry given the word and the occurrences
     * @param word The word
     * @param counter The occurrences
     */
    public DictionaryEntry(String word, int counter) {
        this.word = word;
        this.counter = counter;
    }

    /**
     * Get the word
     * @return The word
     */
    public String getWord() {
        return word;
    }

    /**
     * Set the word
     * @param word The word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Get the occurrences
     * @return The occurrences
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Set the occurrences
     * @param counter The occurrences
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public int compareTo(DictionaryEntry o) {
        return Integer.compare(counter, o.counter);
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
    
    /**
     * Increment the occurrences
     */
    public void incrementCounter() {
        this.counter++;
    }
    
    /**
     * Increment the occurrences given a specified value
     * @param incr The value used to increment the occurrences
     */
    public void incrementCounter(int incr) {
        this.counter+=incr;
    }
    
}
