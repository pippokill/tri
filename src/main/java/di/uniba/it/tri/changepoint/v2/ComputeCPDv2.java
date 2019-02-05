/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This CPD method is based on: "Change-Point Analysis: A Powerful New Tool For Detecting Changes", WAYNE A. TAYLOR
 * https://variation.com/change-point-analysis-a-powerful-new-tool-for-detecting-changes/
 * 
 * @author pierpaolo
 */
public class ComputeCPDv2 {

    private static final Logger LOG = Logger.getLogger(ComputeCPDv2.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input file")
                .addOption("o", true, "Output file")
                .addOption("c", true, "Confidance (default 0.95)")
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
                    double conf = Double.parseDouble(cmd.getOptionValue("c", "0.95"));
                    int n = Integer.parseInt(cmd.getOptionValue("s", "1000"));
                    ComputeCPTTaylor cpd = new ComputeCPTTaylor();
                    Reader in = new FileReader(cmd.getOptionValue("i"));

                    CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withFirstRecordAsHeader().parse(in);
                    List<CSVRecord> records = parser.getRecords();
                    Map<String, Integer> map = parser.getHeaderMap();
                    Map<Integer, String> headerMap = new HashMap<>();
                    for (String name : map.keySet()){
                        headerMap.put(map.get(name),name);
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    int l = 0;
                    for (CSVRecord record : records) {
                        double[] datapoint = new double[record.size() - 2];
                        String word = record.get(1);
                        for (int i = 2; i < record.size(); i++) {
                            datapoint[i - 2] = Double.parseDouble(record.get(i));
                        }
                        List<BootstrappingResult> points = new ArrayList<>();
                        cpd.changePointDetection(datapoint, conf, n, points, 0);
                        if (!points.isEmpty()) {
                            writer.append(word);
                            for (BootstrappingResult r : points) {
                                writer.append("\t")
                                        .append(headerMap.get(r.getSeriesIdx()+2)) // was String.valueOf(r.getSeriesIdx())
                                        .append("\t")
                                        .append(String.valueOf(r.getConfidence()));
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
                    in.close();
                    System.out.println(l);
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Compute change points detection (version 2)", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
