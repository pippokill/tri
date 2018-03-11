/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author pierpaolo
 */
public class ComputeCPD {

    private static final Logger LOG = Logger.getLogger(ComputeCPD.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input file")
                .addOption("o", true, "Output file")
                .addOption("t", true, "Threshold (default 0.001)")
                .addOption("s", true, "Number of samples (default 1000)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                try {

                    double th = Double.parseDouble(cmd.getOptionValue("t", "0.001"));
                    int s = Integer.parseInt(cmd.getOptionValue("s", "1000"));
                    MeanShiftCPD cpd = new MeanShiftCPD();
                    Map<String, List<Double>> map = cpd.load(cmd.getOptionValue("i"));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));

                    //utilizzo la normalizzazione su colonne
                    map = cpd.normalize2(map);
                    int l = 0;
                    Iterator<String> keys = map.keySet().iterator();
                    while (keys.hasNext()) {
                        String word = keys.next();
                        //System.out.println(word);
                        //List<Double> w_ser = cpd.normalize(word, map);
                        List<Double> w_ser = map.get(word);

                        //elimino il primo elemento che è 0 per cumulative e pointwise
                        //w_ser.remove(0);
                        //System.out.println(w_ser);
                        List<Double> w_ms = cpd.meanShift(w_ser);

                        List<List<Double>> w_bs = cpd.bootstrapping(w_ser, s);
                        List<Double> w_pv = cpd.computePValue(w_ms, w_bs);
                        List<ChangePoint> w_cpg = cpd.changePointDetectionList(w_ser, th, w_pv);
                        if (w_cpg.size() > 0) {
                            writer.append(word);
                            for (ChangePoint cp : w_cpg) {
                                writer.append("\t").append(String.valueOf(cp.getIndex())).append("\t").append(String.valueOf(cp.getValue()));
                            }
                            writer.newLine();
                        }
                        l++;
                        if (l % 1000 == 0) {
                            System.out.print(".");
                            if (l % 10000 == 0) {
                                System.out.println(l);
                            }
                        }
                    }
                    System.out.println(l);
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Compute change points detection", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
