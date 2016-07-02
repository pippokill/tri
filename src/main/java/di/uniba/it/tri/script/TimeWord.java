/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

/**
 *
 * @author pierpaolo
 */
public class TimeWord implements Comparable<TimeWord> {

    private String word;

    private double pvalue;

    private int cp;

    private double freq;

    public TimeWord(String word, double pvalue, int cp) {
        this.word = word;
        this.pvalue = pvalue;
        this.cp = cp;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    @Override
    public int compareTo(TimeWord o) {
        return Double.compare(pvalue, o.pvalue);
    }

}
