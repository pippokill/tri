/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class WCScorer {

    private static String[] levels = new String[]{"10", "20", "50", "100", "250", "500", "1000", "all"};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                //load rank
                List<TimeWord> rank = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(args[0]));
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\\t");
                    TimeWord w = new TimeWord(split[0], Double.parseDouble(split[1]), Integer.parseInt(split[2]));
                    w.setFreq(Double.parseDouble(split[3]));
                    rank.add(w);
                }
                reader.close();
                //load word set
                Map<String, Integer> cwmap = new HashMap<>();
                reader = new BufferedReader(new FileReader(args[1]));
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\\t");
                    String[] words = split[0].split(",");
                    String[] years = split[1].split(",");
                    int min = Integer.MAX_VALUE;
                    for (String y : years) {
                        int v = Integer.parseInt(y.trim());
                        if (v < min) {
                            min = v;
                        }
                    }
                    for (String s : words) {
                        cwmap.put(s, min);
                    }
                }
                Collections.sort(rank, new TimeWordSorter());
                System.out.println();
                double[] map = new double[levels.length];
                double[] acc = new double[levels.length];
                for (int j = 0; j < levels.length; j++) {
                    String level = levels[j];
                    int k;
                    if (level.equalsIgnoreCase("all")) {
                        k = rank.size();
                    } else {
                        k = Math.min(Integer.parseInt(level), rank.size());
                    }
                    double correct = 0;
                    double ap = 0;
                    for (int i = 0; i < k; i++) {
                        Integer y = cwmap.get(rank.get(i).getWord());
                        if (y != null && rank.get(i).getCp() >= y) {
                            correct++;
                        }
                        ap += correct / (double) (i + 1);
                    }
                    ap /= (double) k;
                    map[j] = ap;
                    acc[j] = correct / (double) cwmap.size();
                }
                for (double v : map) {
                    System.out.print(v);
                    System.out.print("\t");
                }
                System.out.println();
                for (double v : acc) {
                    System.out.print(v);
                    System.out.print("\t");
                }
                System.out.println();
            }
        } catch (Exception ex) {
            Logger.getLogger(WCScorer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static private class TimeWordSorter implements Comparator<TimeWord> {

        @Override
        public int compare(TimeWord o1, TimeWord o2) {
            int c1 = Double.compare(o1.getPvalue(), o2.getPvalue());
            if (c1 == 0) {
                return -Double.compare(o1.getFreq(), o2.getFreq());
            } else {
                return c1;
            }
        }

    }

}
