/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.api.Tri;
import static di.uniba.it.tri.script.FindNoChangeWords.cmdParser;
import di.uniba.it.tri.space.SpaceBuilder;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.ReverseObjectVectorComparator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author pierpaolo
 */
public class FindVariation {
    
    private static final Logger LOG = Logger.getLogger(FindVariation.class.getName());
    
    static Options options;
    
    static CommandLineParser cmdParser = new BasicParser();
    
    static {
        options = new Options();
        options.addOption("d", true, "TIR directory")
                .addOption("min", true, "Min threshold (optional)")
                .addOption("max", true, "Max threshold (optional)")
                .addOption("idx", true, "Index (optional)")
                .addOption("f", true, "Index field name (optional, default value 'content')")
                .addOption("o", true, "Output file");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("d") && cmd.hasOption("o")) {
                Tri tri = new Tri();
                tri.setMaindir(cmd.getOptionValue("d"));
                double min = Double.parseDouble(cmd.getOptionValue("min", "0"));
                double max = Double.parseDouble(cmd.getOptionValue("max", "1"));
                String fieldname = cmd.getOptionValue("f", "content");
                DirectoryReader reader = null;
                if (cmd.hasOption("idx")) {
                    LOG.info("Open index...");
                    reader = DirectoryReader.open(FSDirectory.open(new File(cmd.getOptionValue("idx"))));
                }
                LOG.info("Init TRI...");
                List<String> years = tri.year(0, Integer.MAX_VALUE);
                for (String y : years) {
                    tri.load("mem", y, y);
                }
                Collections.sort(years);
                int y = 0;
                while (y < years.size() - 1) {
                    LOG.log(Level.INFO, "Computing variation {0}_{1}", new Object[]{years.get(y), years.get(y + 1)});
                    List<ObjectVector> results = tri.sims(years.get(y), years.get(y + 1), Integer.MAX_VALUE, min, max);
                    if (reader != null) {
                        for (ObjectVector ov : results) {
                            double ds = (double) reader.docFreq(new Term(fieldname, ov.getKey())) / (double) reader.maxDoc();
                            ov.setScore((ov.getScore() + ds) / 2);
                        }
                    }
                    Collections.sort(results, new ReverseObjectVectorComparator());
                    LOG.info("Store...");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o") + "_" + years.get(y) + "_" + years.get(y + 1)));
                    for (ObjectVector ov : results) {
                        writer.append(ov.getKey()).append("\t").append(String.valueOf(ov.getScore()));
                        writer.newLine();
                    }
                    writer.close();
                    y++;
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Extract the list of words that change meaning", options, true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
}
