/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.vectors.ObjectVector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class AnalyzeCPbyN {

    private static Map<String, List<String>> loadCP(File file) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            List<String> get = map.get(split[0]);
            if (get == null) {
                get = new ArrayList<>();
                map.put(split[0], get);
            }
            get.add(split[1]);
        }
        reader.close();
        return map;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 2) {
            try {
                Tri tri = new Tri();
                tri.setMaindir(args[1]);
                List<String> years = tri.year(0, Integer.MAX_VALUE);
                Map<String, List<String>> cps = loadCP(new File(args[0]));
                for (String word : cps.keySet()) {
                    List<String> cpl = cps.get(word);
                    for (String y : cpl) {
                        int i = years.indexOf(y);
                        if (i > 0) {
                            try {
                                System.out.println(word + " " + y);
                                tri.load("file", y, y);
                                tri.load("file", years.get(i - 1), years.get(i - 1));
                                tri.get(y, word + y, word);
                                tri.get(years.get(i - 1), word + years.get(i - 1), word);
                                List<ObjectVector> n1 = tri.near(y, word + y, 1000);
                                List<ObjectVector> n0 = tri.near(years.get(i - 1), word + years.get(i - 1), 1000);
                                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2] + "/N_" + word + "_" + y + ".tsv"));
                                writer.append(years.get(i - 1) + "\tsim_" + years.get(i - 1) + "\t" + y + "\tsim_" + y);
                                writer.newLine();
                                for (int k = 0; k < n0.size(); k++) {
                                    writer.append(n0.get(k).getKey());
                                    writer.append("\t");
                                    writer.append(String.valueOf(n0.get(k).getScore()));
                                    writer.append("\t");
                                    writer.append(n1.get(k).getKey());
                                    writer.append("\t");
                                    writer.append(String.valueOf(n1.get(k).getScore()));
                                    writer.newLine();
                                }
                                writer.close();
                            } catch (Exception ex) {
                                Logger.getLogger(AnalyzeCPbyN.class.getName()).log(Level.SEVERE, "Skip " + word + " " + y, ex);
                            }
                        } else {
                            Logger.getLogger(AnalyzeCPbyN.class.getName()).log(Level.WARNING, "Skip {0} {1} (no valid CP)", new Object[]{word, y});
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AnalyzeCPbyN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
