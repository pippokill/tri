/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

/**
 *
 * @author pierpaolo
 */
public class ChangePointResults implements Comparable<ChangePointResults> {
    
    private int index;
    
    private double value;

    public ChangePointResults() {
    }

    public ChangePointResults(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(ChangePointResults o) {
        return Double.compare(this.value, o.getValue());
    }
    
    
    
}
