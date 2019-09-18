/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.dictit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class BuildGold {

    private static final Logger LOG = Logger.getLogger(Evaluate.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("s", true, "Sense file")
                .addOption("d", true, "Dictionary (for filtering word)")
                .addOption("o", true, "Output");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("s") && cmd.hasOption("o")) {
                try {
                    Set<String> dict = new HashSet<>();
                    if (cmd.hasOption("d")) {
                        BufferedReader reader = new BufferedReader(new FileReader(cmd.getOptionValue("d")));
                        while (reader.ready()) {
                            String[] split = reader.readLine().split("\t");
                            dict.add(split[0]);
                        }
                        reader.close();
                    }
                    BufferedReader reader = new BufferedReader(new FileReader(cmd.getOptionValue("s")));
                    Map<String, Set<Integer>> cpdmap = new HashMap<>();
                    Pattern pattern = Pattern.compile("(a\\.\\s)([0-9]+)");
                    while (reader.ready()) {
                        String[] split = reader.readLine().split("\t");
                        if (!cmd.hasOption("d") || dict.contains(split[0])) {
                            String lastinfo = split[split.length - 1];
                            Matcher matcher = pattern.matcher(lastinfo);
                            while (matcher.find()) {
                                String year = matcher.group(2);
                                Set<Integer> set = cpdmap.get(split[0]);
                                if (set == null) {
                                    set = new HashSet<>();
                                    cpdmap.put(split[0], set);
                                }
                                set.add(Integer.parseInt(year));
                            }
                        }
                    }
                    reader.close();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    for (Map.Entry<String, Set<Integer>> e : cpdmap.entrySet()) {
                        writer.append(e.getKey());
                        List<Integer> list = new ArrayList<>(e.getValue());
                        Collections.sort(list);
                        for (Integer y : list) {
                            writer.append("\t").append(y.toString());
                        }
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Evaluate EOD", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
