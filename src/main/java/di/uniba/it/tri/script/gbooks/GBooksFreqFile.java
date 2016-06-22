/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script.gbooks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author pierpaolo
 */
public class GBooksFreqFile {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("in", true, "Ngram plain dir").
                addOption("out", true, "Output dir").
                addOption("lower", true, "Enable lower case (default=false)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("in") && cmd.hasOption("out")) {
                boolean lower = Boolean.parseBoolean(cmd.getOptionValue("lower", "false"));
                File corpusDir = new File(cmd.getOptionValue("in"));
                File[] files = corpusDir.listFiles();
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".gz")) {
                        Logger.getLogger(GBooksFreqFile.class.getName()).log(Level.INFO, "Open file {0}", file.getName());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                        Map<String, Integer> dict = new HashMap<>();
                        String line;
                        while (reader.ready()) {
                            line = reader.readLine();
                            String[] values = line.split("\\t");
                            String[] tokens;
                            if (lower) {
                                tokens = values[0].toLowerCase().split("\\s");
                            } else {
                                tokens = values[0].split("\\s");
                            }
                            int c = Integer.parseInt(values[1]);
                            for (String token : tokens) {
                                Integer v = dict.get(token);
                                if (v == null) {
                                    dict.put(token, c);
                                } else {
                                    dict.put(token, v + c);
                                }
                            }
                        }
                        reader.close();
                        String[] splitname = file.getName().split("\\.");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("out") + "/" + splitname[0] + ".freq"));
                        for (Map.Entry<String, Integer> e : dict.entrySet()) {
                            writer.append(e.getKey()).append("\t").append(e.getValue().toString());
                            writer.newLine();
                        }
                        writer.close();
                    }
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build token frequencies file given ngram files", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(GBooksFreqFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
