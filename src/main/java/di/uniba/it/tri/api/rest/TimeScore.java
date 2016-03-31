/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.api.rest;

/**
 *
 * @author pierpaolo
 */
public class TimeScore implements Comparable<TimeScore> {

    private int year;

    private float score;

    public TimeScore(int year, float score) {
        this.year = year;
        this.score = score;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(TimeScore o) {
        return Integer.compare(year, o.year);
    }

}
