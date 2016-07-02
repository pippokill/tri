/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class AnalyzeTimeSeries {

    private static List<TimeWord> loadTimeSerie(File inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        reader.readLine();
        List<TimeWord> list = new ArrayList<>();
        while (reader.ready()) {
            String[] split = reader.readLine().split(",");
            list.add(new TimeWord(split[1], Float.parseFloat(split[2]), Integer.parseInt(split[3])));
        }
        reader.close();
        return list;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                Map<String, Integer> idxYear = new HashMap<>();
                BufferedReader reader = new BufferedReader(new FileReader(args[1]));
                if (reader.ready()) {
                    String[] split = reader.readLine().split(",");
                    for (int i = 2; i < split.length; i++) {
                        idxYear.put(split[i], i - 2);
                    }
                }
                Map<String, float[]> count = new HashMap<>();
                while (reader.ready()) {
                    String[] split = reader.readLine().split(",");
                    float[] a = new float[split.length - 2];
                    for (int i = 2; i < split.length; i++) {
                        a[i - 2] = Float.parseFloat(split[i]);
                    }
                    count.put(split[1], a);
                }
                reader.close();
                List<TimeWord> loadTimeSerie = loadTimeSerie(new File(args[0]));
                Collections.sort(loadTimeSerie);
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                for (TimeWord tw : loadTimeSerie) {
                    writer.append(tw.getWord()).append("\t").append(String.valueOf(tw.getPvalue()));
                    int cp = tw.getCp();
                    float freq = count.get(tw.getWord())[idxYear.get(String.valueOf(cp))];
                    writer.append("\t").append(String.valueOf(cp)).append("\t").append(String.valueOf(freq));
                    writer.newLine();
                }
                writer.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(AnalyzeTimeSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
