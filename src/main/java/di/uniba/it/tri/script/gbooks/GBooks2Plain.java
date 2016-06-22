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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
public class GBooks2Plain {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("corpus", true, "Ngram corpus dir").
                addOption("out", true, "Output dir").
                addOption("from", true, "Start year (default value=1850)").
                addOption("to", true, "End year (default value=2012)").
                addOption("step", true, "Time interval size  (default value=10)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("corpus") && cmd.hasOption("out")) {
                Map<Integer, BufferedWriter> writers = new HashMap<>();
                int start = Integer.parseInt(cmd.getOptionValue("from", "1850"));
                int end = Integer.parseInt(cmd.getOptionValue("to", "2012"));
                int step = Integer.parseInt(cmd.getOptionValue("step", "10"));
                File corpusdir = new File(cmd.getOptionValue("corpus"));
                File[] listFiles = corpusdir.listFiles();
                for (File file : listFiles) {
                    if (file.isFile() && file.getName().endsWith(".gz")) {
                        Logger.getLogger(GBooks2Plain.class.getName()).log(Level.INFO, "Open file: {0}", file.getName());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                        String line;
                        while (reader.ready()) {
                            line = reader.readLine();
                            Ngram ngram = GBooksUtils.parseNgram(line);
                            if (!ngram.getTokens().isEmpty() && ngram.getYear()>= start && ngram.getYear()<=end) {
                                int idx = (ngram.getYear() - start) / step;
                                BufferedWriter writer = writers.get(idx);
                                if (writer==null) {
                                    writer=new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cmd.getOptionValue("out")+"/plain_"+(start+idx*step)+".ngrams.gz"))));
                                    writers.put(idx, writer);
                                }
                                ngram.write(writer);
                                writer.newLine();
                            }
                        }
                        reader.close();
                    }
                }
                for (BufferedWriter writer:writers.values()) {
                    writer.close();
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build plain text file with occurrences using Google-ngram as input", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(GBooks2Plain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
