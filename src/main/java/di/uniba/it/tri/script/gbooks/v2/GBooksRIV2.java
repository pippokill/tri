/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script.gbooks.v2;

import di.uniba.it.tri.script.gbooks.GBooksUtils;
import di.uniba.it.tri.script.gbooks.Ngram;
import di.uniba.it.tri.script.gbooks.NgramPair;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class GBooksRIV2 {
    
    private static final Logger LOG = Logger.getLogger(GBooksRIV2.class.getName());
    
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
                addOption("d", true, "Vector dimension").
                addOption("s", true, "Seed dimension").
                addOption("start", true, "Start year").
                addOption("end", true, "End year").
                addOption("t", true, "Period size").
                addOption("e", false, "Perform only dictionary filtering");
    }
    
    private static void build(String inputDir, String outputDir, Map<String, Integer> dict, int d, int s, int start, int end, int t) throws IOException {
        Map<String, Vector> randomVectors = new Object2ObjectOpenHashMap<>();
        Map<String, Double> ws = new Object2DoubleOpenHashMap<>();
        Random random = new Random();
        //total occs
        double to = 0;
        for (String key : dict.keySet()) {
            to += dict.get(key).doubleValue();
        }
        //compute weights
        for (String key : dict.keySet()) {
            double c = dict.get(key).doubleValue();
            double f = c / (double) to; //downsampling
            double p = 1;
            if (f > th) { //if word frequency is greater than the threshold, compute the probability of consider the word 
                p = Math.sqrt(th / f);
            }
            ws.put(key, p);
            randomVectors.put(key, VectorFactory.generateRandomVector(VectorType.REAL, d, s, random));
        }
        int i = start;
        while (i <= end) {
            LOG.log(Level.INFO, "year: {0}", i);
            Map<String, Vector> vectors = new Object2ObjectOpenHashMap<>();
            for (int j = i; j < i + t; j++) {
                File filet = new File(inputDir + "/plain_" + j + ".ngrams.gz");
                LOG.log(Level.INFO, "processing: {0}", filet.getName());
                if (filet.exists()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filet))));
                    while (reader.ready()) {
                        String line = reader.readLine();
                        try {
                            Ngram ngram = GBooksUtils.parseNgramFromPlain(line);
                            List<String> tokens = ngram.getTokens();
                            List<NgramPair> pairs = GBooksUtils.getContexts(tokens);
                            for (NgramPair pair : pairs) {
                                if (dict.containsKey(pair.getTarget()) && dict.containsKey(pair.getContext())) {
                                    Vector vt = vectors.get(pair.getTarget());
                                    if (vt == null) {
                                        vt = VectorFactory.createZeroVector(VectorType.REAL, d);
                                        vectors.put(pair.getTarget(), vt);
                                    }
                                    Vector vr = randomVectors.get(pair.getContext());
                                    if (vr != null) {
                                        vt.superpose(vr, ws.get(pair.getContext()), null);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(GBooksRIV2.class.getName()).log(Level.WARNING, "Error to parse ngram", ex);
                        }
                    }
                    reader.close();
                } else {
                    LOG.log(Level.WARNING, "{0} does not exist.", filet.getName());
                }
            }
            LOG.info("Save vectors...");
            File svfile = new File(outputDir + "/sv_" + i + "_" + (i + t - 1)+".vectors");
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(svfile)));
            String header = VectorStoreUtils.createHeader(VectorType.REAL, d, s);
            outputStream.writeUTF(header);
            for (Map.Entry<String, Vector> entry : vectors.entrySet()) {
                outputStream.writeUTF(entry.getKey());
                entry.getValue().writeToStream(outputStream);
            }
            outputStream.close();
            i += t;
        }
        LOG.info("Save random vectors...");
        File rifile = new File(outputDir + "/vectors.elemental");
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rifile)));
        String header = VectorStoreUtils.createHeader(VectorType.REAL, d, s);
        outputStream.writeUTF(header);
        for (Map.Entry<String, Vector> entry : randomVectors.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().writeToStream(outputStream);
        }
        outputStream.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("f") && cmd.hasOption("p") && cmd.hasOption("o") && (cmd.hasOption("n") || cmd.hasOption("minf"))) {
                int d = Integer.parseInt(cmd.getOptionValue("d", "500"));
                int s = Integer.parseInt(cmd.getOptionValue("s", "10"));
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
                build(cmd.getOptionValue("p"), cmd.getOptionValue("o"), dict, d, s, start, end, t);
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build TRI on Google ngrams", options, true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
}
