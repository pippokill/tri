/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class BuildOccStatistics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            File startDir = new File(args[0]);
            File[] files = startDir.listFiles();
            Arrays.sort(files);
            Map<String, int[]> cmap = new TreeMap<>();
            int k = 0;
            for (File file : files) {
                System.out.println("Reading " + file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                String[] split;
                while (reader.ready()) {
                    split = reader.readLine().split("\\s+");
                    int c = 0;
                    for (int i = 2; i < split.length; i = i + 2) {
                        c += Integer.parseInt(split[i]);
                    }
                    int[] vc = cmap.get(split[0]);
                    if (vc == null) {
                        vc = new int[files.length];
                        cmap.put(split[0], vc);
                    }
                    vc[k] = c;
                }
                reader.close();
                k++;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            for (Map.Entry<String, int[]> e : cmap.entrySet()) {
                writer.append(e.getKey());
                int[] vc = e.getValue();
                for (int c : vc) {
                    writer.append("\t").append(String.valueOf(c));
                }
                writer.newLine();
            }
            writer.close();
        } catch (Exception ex) {
            Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
