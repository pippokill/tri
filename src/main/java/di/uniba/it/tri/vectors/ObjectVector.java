/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    public ObjectVector(String key, double score) {
        this.key = key;
        this.score = score;
    }

    public ObjectVector(String key, Vector vector) {
        this.key = key;
        this.vector = vector;
    }

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
