/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.dictit;

import static di.uniba.it.tri.changepoint.dictit.BuildGold.cmdParser;
import di.uniba.it.tri.changepoint.v2.LemmaDict;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 *
 * @author pierpaolo
 */
public class ExtractDictFromTS {

    private static final Logger LOG = Logger.getLogger(ExtractDictFromTS.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Time series")
                .addOption("l", true, "Lemma dict")
                .addOption("o", true, "Output");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o") && cmd.hasOption("l")) {
                try {
                    LemmaDict lemmadict = new LemmaDict();
                    lemmadict.init(new File(cmd.getOptionValue("l")));
                    Reader in = new FileReader(cmd.getOptionValue("i"));
                    Set<String> set = new HashSet();
                    CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withFirstRecordAsHeader().parse(in);
                    List<CSVRecord> records = parser.getRecords();
                    for (CSVRecord record : records) {
                        String word = record.get("word");
                        set.add(word);
                        String lemma = lemmadict.getLemma(word);
                        if (lemma != null) {
                            set.add(lemma);
                        }
                    }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    for (String s : set) {
                        writer.append(s);
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Extract dict from time series", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
