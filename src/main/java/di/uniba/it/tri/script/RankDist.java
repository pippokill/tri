/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author pierpaolo
 */
public class RankDist {

    private static final Logger LOG = Logger.getLogger(RankDist.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Vectors directory")
                .addOption("o", true, "Output directory");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                Tri tri = new Tri();
                tri.setMaindir(cmd.getOptionValue("i"));
                LOG.info("Init TRI...");
                List<String> years = tri.year(0, Integer.MAX_VALUE);
                Collections.sort(years);
                int y = 1;
                while (y < years.size()) {
                    LOG.log(Level.INFO, "Computing variation {0}_{1}", new Object[]{years.get(y - 1), years.get(y)});
                    tri.load("mem", years.get(y), years.get(y));
                    tri.load("mem", years.get(y - 1), years.get(y - 1));
                    VectorReader vr1 = tri.getStores().get(years.get(y - 1));
                    VectorReader vr2 = tri.getStores().get(years.get(y));
                    List<RankWords.ScoredWord> list = new ArrayList<>();
                    Iterator<ObjectVector> it = vr1.getAllVectors();
                    while (it.hasNext()) {
                        ObjectVector ov1 = it.next();
                        Vector v2 = vr2.getVector(ov1.getKey());
                        if (v2 != null) {
                            RankWords.ScoredWord sw = new RankWords.ScoredWord(ov1.getKey(), ov1.getVector().measureOverlap(v2));
                            list.add(sw);
                        }
                    }
                    Collections.sort(list);
                    vr1.close();
                    vr2.close();
                    tri.getStores().remove(years.get(y));
                    tri.getStores().remove(years.get(y - 1));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o") + "/DIST_rank_" + years.get(y)));
                    for (RankWords.ScoredWord sw : list) {
                        writer.append(sw.getWord()).append("\t").append(String.valueOf(sw.getScore()));
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
