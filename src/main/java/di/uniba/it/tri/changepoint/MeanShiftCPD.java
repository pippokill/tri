/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
        System.out.println("Carico file in memoria...");
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
            System.out.println("File caricato.");
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
            /*
            double mean = StatUtils.mean(arr);
            double variance = StatUtils.variance(arr);
            List<Double> r = new ArrayList<>(arr.length);
            for (int i = 0; i < arr.length; i++) {
                r.add((arr[i] - mean) / variance);
            }*/
            double[] ser_norm = StatUtils.normalize(arr);
            List<Double> r = new ArrayList<>(arr.length);
            for (int i = 0; i < arr.length; i++) {
                r.add(ser_norm[i]);
            }
            System.out.println(r);
            return r;
        }
    }

    /**
     * copio la serie in una matrice su cui vengono effettuate le opzioni di
     * normalizzazione in base alle colonne ricopio il contenuto della matrice
     * nella map
     *
     * @param series
     * @return series normalizzata
     * @throws java.io.IOException
     */
    public Map<String, List<Double>> normalize2(Map<String, List<Double>> series) throws IOException {

        System.out.println("Normalizzazione in corso...");
        //Map<String, List<Double>> ser_norm = new HashMap<>();

        //Sposto la map in una matrice
        Iterator it = series.entrySet().iterator();
        Map.Entry w = (Map.Entry) it.next();
        List<Double> s = (List<Double>) w.getValue();
        //it.remove();
        //double[][] m = new double[series.size()][s.size()];
        double[][] m = new double[series.size()][s.size()];

        int i = 0;
        Iterator ite = series.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry word = (Map.Entry) ite.next();
            List<Double> t = (List<Double>) word.getValue();
            for (int j = 0; j < s.size(); j++) {
                m[i][j] = t.get(j);
            }
            i++;
            //ite.remove(); // avoids a ConcurrentModificationException
        }

        //normalizzo la matrice
        double[] list_to_norm = new double[series.size()];

        for (int col = 0; col < s.size(); col++) {
            for (int rig = 0; rig < series.size(); rig++) {
                list_to_norm[rig] = m[rig][col];
            }

            //se la varianza e' zero ricopio solo i valori senza normalizzare
            if (StatUtils.variance(list_to_norm) != 0) {
                double[] list_norm = StatUtils.normalize(list_to_norm);
                for (int rig = 0; rig < series.size(); rig++) {
                    m[rig][col] = list_norm[rig];
                }
            } else {
                for (int rig = 0; rig < series.size(); rig++) {
                    m[rig][col] = list_to_norm[rig];
                }
            }

        }

        //copio la matrice nella map
        Iterator iter = series.entrySet().iterator();
        int riga = 0;
        //BufferedWriter writer = new BufferedWriter(new FileWriter("/home/rodman/Scrivania/Sperimentazione_TRI/en_stat/serie_normalizzata_ri_pointwise"));

        while (iter.hasNext()) {
            List<Double> t = new ArrayList<>();
            Map.Entry word = (Map.Entry) iter.next();
            //writer.write((String)word.getKey()+" ");
            for (int col = 0; col < s.size(); col++) {
                t.add(m[riga][col]);
            }
            //writer.write(t.toString()+"\n");
            word.setValue(t);

            riga++;
        }
        //writer.close();
        System.out.println("Normalizzazione effettuata.");
        return series;
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
        for (int j = 0; j < s.size() - 1; j++) {
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
     * Compute p-value righe 8-10 algoritmo
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
     * Change point detection righe 11-14 algoritmo
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
            //attenzione ai valori di soglia spesso oltre che piccolissimi sono anche negativi
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
        //gli indici ritornati devono essere incrementati di 1 per coincidere con gli anni
        //poichè non potremmo mai avere un cambiamento al primo anno
        return cgp;
    }

}
