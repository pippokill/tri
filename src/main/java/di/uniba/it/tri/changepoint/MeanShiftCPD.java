/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.stat.StatUtils;

/**
 *
 * @author pierpaolo
 */
public class MeanShiftCPD {

    private static final Logger LOG = Logger.getLogger(MeanShiftCPD.class.getName());

    private String cvsSplitBy = ",";

    public Map<String, List<Double>> load(String filename) {
        return load(new File(filename));
    }

    /**
     * Load time series from file
     *
     * @param file Input file
     * @return HashMap of time series
     */
    public Map<String, List<Double>> load(File file) {
        //HashMap of time series
        Map<String, List<Double>> series = new HashMap<>();
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            //skip first line
            if (br.ready()) {
                br.readLine();
            }
            while (br.ready()) {
                line = br.readLine();
                // split values
                String[] word = line.split(cvsSplitBy);
                // build time series
                List<Double> value = new ArrayList<>();
                for (int i = 2; i < word.length; i++) {
                    value.add(Double.parseDouble(word[i]));
                }
                // add time series into hashmap
                series.put(word[1], value);
            }
            br.close();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return series;
    }

    public String getCvsSplitBy() {
        return cvsSplitBy;
    }

    public void setCvsSplitBy(String cvsSplitBy) {
        this.cvsSplitBy = cvsSplitBy;
    }

    /**
     * normalize time series
     *
     * @param word
     * @param series
     * @return
     */
    public List<Double> normalize(String word, Map<String, List<Double>> series) {
        List<Double> get = series.get(word);
        if (get == null) {
            return new ArrayList<>();
        } else {
            double[] arr = new double[get.size()];
            for (int i = 0; i < get.size(); i++) {
                arr[i] = get.get(i);
            }
            double mean = StatUtils.mean(arr);
            double variance = StatUtils.variance(arr);
            List<Double> r = new ArrayList<>(arr.length);
            for (int i = 0; i < arr.length; i++) {
                r.add((arr[i] - mean) / variance);
            }
            return r;
        }
    }

    /**
     * Compute mean shift for a normalized time series
     *
     * @param s time series
     * @return mean shift values
     */
    public List<Double> meanShift(List<Double> s) {
        double sum_pre_j = 0;
        double sum_post_j = 0;
        List<Double> meanShift = new ArrayList<>();
        for (int j = 0; j < s.size(); j++) {
            //after j
            for (int k = j + 1; k < s.size(); k++) {
                sum_post_j += s.get(k);
            }
            sum_post_j = sum_post_j / (s.size() - (j + 1));
            //before j
            for (int k = 0; k <= j; k++) {
                sum_pre_j += s.get(k);
            }
            sum_pre_j = sum_pre_j / (j + 1);
            meanShift.add(sum_post_j - sum_pre_j);
            sum_post_j = 0;
            sum_pre_j = 0;
        }
        return meanShift;
    }

    /**
     * Bootstrapping
     *
     * @param k
     * @param num_bs
     * @return
     */
    public List<List<Double>> bootstrapping(List<Double> k, int num_bs) {
        List<List<Double>> bs = new ArrayList<>();

        for (int i = 0; i < num_bs; i++) {
            List<Double> temp = new ArrayList<>(k);
            java.util.Collections.shuffle(temp);
            bs.add(temp);
        }
        return bs;
    }

    /**
     * Compute p-value
     *
     * @param meanshift
     * @param samples
     * @return
     */
    public List<Double> computePValue(List<Double> meanshift, List<List<Double>> samples) {
        List<Double> p_values = new ArrayList<>();
        List<List<Double>> samples_ms = new ArrayList();

        //Compute meanShift of samples
        for (List s : samples) {
            samples_ms.add(meanShift(s));
        }
        //System.out.println("mean_shift_samples: " + samples_ms);

        for (int i = 0; i < meanshift.size(); i++) {

            int cont = 0;

            for (int j = 0; j < samples_ms.size(); j++) {

                if (samples_ms.get(j).get(i) > meanshift.get(i)) {

                    cont++;
                }
            }

            double v = (double) cont / (samples.size());

            p_values.add(v);
        }
        return p_values;
    }

    /**
     * Change point detection
     *
     * @param norm normalized values
     * @param threshold threshold
     * @param pValues p values
     * @return
     */
    public Map<Double, Integer> changePointDetection(List<Double> norm, double threshold, List<Double> pValues) {
        Map<Double, Integer> cgp = new HashMap<>();

        //series indicies that overcome the threshold
        List<Integer> c = new ArrayList<>();
        for (int j = 0; j < norm.size(); j++) {
            if (norm.get(j) > threshold) {
                c.add(j);
            }
        }
        //System.out.println(c);
        if (!c.isEmpty()) {
            double min = pValues.get(0);
            int j = 0;
            for (int i = 1; i < c.size() - 1; i++) {
                if (pValues.get(c.get(i)) < min) {
                    j = c.get(i);
                    min = pValues.get(c.get(i));
                }
            }

            cgp.put(min, j);
        }/* else {
            cgp.put(Double.NaN, -1);
        }*/
        return cgp;
    }

}
