/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

/**
 *
 * @author pierpaolo
 */
public class BootstrappingResult {
    
    private double confidence;
    
    private double value;
    
    private int seriesIdx;

    public BootstrappingResult(double confidence, double value, int seriesIdx) {
        this.confidence = confidence;
        this.value = value;
        this.seriesIdx = seriesIdx;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getSeriesIdx() {
        return seriesIdx;
    }

    public void setSeriesIdx(int seriesIdx) {
        this.seriesIdx = seriesIdx;
    }

    @Override
    public String toString() {
        return "BootstrappingResult{" + "confidence=" + confidence + ", value=" + value + ", seriesIdx=" + seriesIdx + '}';
    }
    
    
    
}
