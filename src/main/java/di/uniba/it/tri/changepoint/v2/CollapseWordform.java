/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
public class CollapseWordform {

    private static final Logger LOG = Logger.getLogger(CollapseWordform.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input CPD file")
                .addOption("d", true, "Dictionary for lemmas")
                .addOption("o", true, "Outputfile");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o") && cmd.hasOption("d")) {
                try {
                    LemmaDict dict = new LemmaDict();
                    dict.init(new File(cmd.getOptionValue("d")));
                    Map<String, Set<String>> cpd = new Object2ObjectOpenHashMap<>();
                    BufferedReader reader = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
                    while (reader.ready()) {
                        String[] values = reader.readLine().split("\t");
                        String lemma = dict.getLemma(values[0]);
                        if (lemma == null) {
                            lemma = values[0];
                        }
                        Set<String> set = cpd.get(lemma);
                        if (set==null) {
                            set=new HashSet();
                            cpd.put(lemma,set);
                        }
                        for (int i=1;i<values.length;i=i+2) {
                            set.add(values[i]);
                        }
                    }
                    reader.close();
                    BufferedWriter writer=new BufferedWriter(new FileWriter(new File(cmd.getOptionValue("o"))));
                    for (String l:cpd.keySet()) {
                        writer.append(l);
                        Set<String> set = cpd.get(l);
                        for (String c:set) {
                            writer.append("\t").append(c);
                        }
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Collapse word form", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }

}
