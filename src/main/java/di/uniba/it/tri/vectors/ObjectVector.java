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
package di.uniba.it.tri.vectors;

import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class ObjectVector implements Comparable<ObjectVector> {

    private String key;

    private Vector vector;

    private double score = 1;

    /**
     *
     * @param key
     * @param score
     */
    public ObjectVector(String key, double score) {
        this.key = key;
        this.score = score;
    }

    /**
     *
     * @param key
     * @param vector
     */
    public ObjectVector(String key, Vector vector) {
        this.key = key;
        this.vector = vector;
    }

    /**
     *
     * @param key
     * @param vector
     * @param score
     */
    public ObjectVector(String key, Vector vector, double score) {
        this.key = key;
        this.vector = vector;
        this.score = score;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.key);
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
        final ObjectVector other = (ObjectVector) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ObjectVector{" + "key=" + key + ", score=" + score + '}';
    }

    @Override
    public int compareTo(ObjectVector o) {
        return Double.compare(score, o.score);
    }

    /**
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     *
     * @return
     */
    public Vector getVector() {
        return vector;
    }

    /**
     *
     * @param vector
     */
    public void setVector(Vector vector) {
        this.vector = vector;
    }

    /**
     *
     * @return
     */
    public double getScore() {
        return score;
    }

    /**
     *
     * @param score
     */
    public void setScore(double score) {
        this.score = score;
    }

}
