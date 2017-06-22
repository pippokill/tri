/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.api.Tri;
import static di.uniba.it.tri.script.BuildOccStatistics.cmdParser;
import di.uniba.it.tri.space.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author pierpaolo
 */
public class BuildNeighborhood {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("d", true, "Base directory")
                .addOption("w", true, "Words list, list of words separated by ';'")
                .addOption("o", true, "Output file")
                .addOption("n", true, "Neighborhood size (optional)")
                .addOption("s", true, "Start year (optional)")
                .addOption("e", true, "End year (optional)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("d") && cmd.hasOption("w") && cmd.hasOption("o")) {
                Tri tri = new Tri();
                tri.setMaindir(cmd.getOptionValue("d"));
                int n = Integer.parseInt(cmd.getOptionValue("n", "100"));
                String start = cmd.getOptionValue("s", "0");
                String end = cmd.getOptionValue("e", "999999");
                String[] words = cmd.getOptionValue("w").split(";");
                Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.INFO, "Start " + start);
                Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.INFO, "End " + end);
                Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.INFO, "Size " + n);
                List<String> years = tri.year(0, Integer.MAX_VALUE);
                Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.INFO, "Computing...");
                Map<String, List<ObjectVector>> mapr = new HashMap<>(years.size());
                for (String year : years) {
                    if (year.compareTo(start) >= 0 && year.compareTo(end) <= 0) {
                        VectorReader vr = TemporalSpaceUtils.getVectorReader(tri.getMainDir(), year, true);
                        Vector s = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
                        double nw = 0;
                        for (String w : words) {
                            Vector wv = vr.getVector(w);
                            if (wv != null) {
                                s.superpose(wv, 1, null);
                                nw++;
                            }
                        }
                        Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
                        v.superpose(s, 1 / nw, null);
                        List<ObjectVector> nearestVectors = TemporalSpaceUtils.getNearestVectors(vr, v, n);
                        mapr.put(year, nearestVectors);
                    }
                    System.out.print(".");
                }
                System.out.println();
                Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.INFO, "Writing...");
                List<String> hyear = new ArrayList<>(mapr.keySet());
                Collections.sort(hyear);
                String[] headers = new String[hyear.size() * 2];
                int j = 0;
                for (String hy : hyear) {
                    headers[j] = hy + "_W";
                    j++;
                    headers[j] = hy + "_S";
                    j++;
                }
                Collections.sort(hyear);
                final Appendable out = new FileWriter(cmd.getOptionValue("o"));
                final CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(',').withHeader(headers).print(out);
                for (int row = 0; row < n; row++) {
                    for (String hy : hyear) {
                        List<ObjectVector> nl = mapr.get(hy);
                        if (nl.size() > row) {
                            printer.print(nl.get(row).getKey());
                            printer.print(nl.get(row).getScore());
                        } else {
                            printer.print("");
                            printer.print("");
                        }
                    }
                    printer.println();
                }
                printer.close();
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build neighborhood", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
