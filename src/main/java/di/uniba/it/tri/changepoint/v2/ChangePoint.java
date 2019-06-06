/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.util.Objects;

/**
 *
 * @author pierpaolo
 */
public class ChangePoint {

    private String word;

    private String label;

    private double confidance;

    public ChangePoint() {
    }

    public ChangePoint(String word, String label) {
        this.word = word;
        this.label = label;
    }

    public ChangePoint(String word, String label, double confidance) {
        this.word = word;
        this.label = label;
        this.confidance = confidance;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getConfidance() {
        return confidance;
    }

    public void setConfidance(double confidance) {
        this.confidance = confidance;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.word);
        hash = 79 * hash + Objects.hashCode(this.label);
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
        final ChangePoint other = (ChangePoint) obj;
        if (!Objects.equals(this.word, other.word)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChangePoint{" + "word=" + word + ", label=" + label + ", confidance=" + confidance + '}';
    }

}
