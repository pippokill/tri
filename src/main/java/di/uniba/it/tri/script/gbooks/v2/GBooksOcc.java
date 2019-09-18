/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script.gbooks.v2;

import di.uniba.it.tri.script.gbooks.GBooksUtils;
import di.uniba.it.tri.script.gbooks.Ngram;
import di.uniba.it.tri.script.gbooks.NgramPair;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author pierpaolo
 */
public class GBooksOcc {

    private static final Logger LOG = Logger.getLogger(GBooksOcc.class.getName());

    private static double th = 0.001;

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("f", true, "Dictionary file").
                addOption("n", true, "Dictionary dimension").
                addOption("minf", true, "Min frequecy for filtering dictionary").
                addOption("p", true, "Plain ngram directory").
                addOption("o", true, "Output directory").
                addOption("start", true, "Start year").
                addOption("end", true, "End year").
                addOption("t", true, "Period size").
                addOption("e", false, "Perform only dictionary filtering");
    }

    private static void buildOcc(String inputDir, String outputDir, Map<String, Integer> dict, int start, int end, int t) throws IOException {
        int i = start;
        while (i <= end) {
            LOG.log(Level.INFO, "year: {0}", i);
            for (int j = i; j < i + t; j++) {
                File filet = new File(inputDir + "/plain_" + j + ".ngrams.gz");
                LOG.log(Level.INFO, "processing: {0}", filet.getName());
                if (filet.exists()) {
                    File occfile = new File(outputDir + "/occ_" + i + "_" + (i + t - 1) + ".gz");
                    if (!occfile.exists()) {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(occfile))));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filet))));
                        while (reader.ready()) {
                            String line = reader.readLine();
                            try {
                                Ngram ngram = GBooksUtils.parseNgramFromPlain(line);
                                List<String> tokens = ngram.getTokens();
                                List<NgramPair> pairs = GBooksUtils.getContexts(tokens);
                                for (NgramPair pair : pairs) {
                                    if (dict.containsKey(pair.getTarget()) && dict.containsKey(pair.getContext())) {
                                        writer.append(pair.getTarget()).append(" ").append(pair.getContext());
                                        writer.newLine();
                                    }
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(GBooksOcc.class.getName()).log(Level.WARNING, "Error to parse ngram", ex);
                            }
                        }
                        reader.close();
                        writer.close();
                    }
                } else {
                    LOG.log(Level.WARNING, "{0} does not exist.", filet.getName());
                }
            }
            i += t;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("f") && cmd.hasOption("p") && cmd.hasOption("o") && (cmd.hasOption("n") || cmd.hasOption("minf"))) {
                int n = Integer.parseInt(cmd.getOptionValue("n", "100000"));
                int start = Integer.parseInt(cmd.getOptionValue("start", "1900"));
                int end = Integer.parseInt(cmd.getOptionValue("end", "2012"));
                int t = Integer.parseInt(cmd.getOptionValue("t", "10"));
                int minf = Integer.parseInt(cmd.getOptionValue("minf", "5"));
                File dictfile = new File(cmd.getOptionValue("f"));
                Map<String, Integer> dict;
                if (cmd.hasOption("n") && cmd.hasOption("min-freq")) {
                    LOG.warning("Both dict size and min-freq are set -> min-freq is used");
                    dict = GBooksUtils.filterDictByFreq(dictfile, minf);
                } else if (cmd.hasOption("n")) {
                    dict = GBooksUtils.filterDictBySize(dictfile, n);
                } else {
                    dict = GBooksUtils.filterDictByFreq(dictfile, minf);
                }
                LOG.log(Level.INFO, "Dictionary size: {0}", dict.size());
                if (cmd.hasOption("e")) {
                    System.exit(0);
                }
                buildOcc(cmd.getOptionValue("p"), cmd.getOptionValue("o"), dict, start, end, t);
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build TRI on Google ngrams", options, true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
