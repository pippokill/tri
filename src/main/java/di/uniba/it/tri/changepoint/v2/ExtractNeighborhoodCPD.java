/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.space.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

/**
 *
 * @author pierpaolo
 */
public class ExtractNeighborhoodCPD {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("p", true, "CPD file")
                .addOption("o", true, "Output file")
                .addOption("n", true, "Number of neighborhood (default n=25)")
                .addOption("s", true, "StopWord file");
    }

    private static final Logger LOG = Logger.getLogger(ExtractNeighborhoodCPD.class.getName());

    private static List<ObjectVector> search(Tri tri, List<String> years, String word, String label, int ns) throws IOException {
        int i = years.indexOf(label) - 2;
        while (i >= 0) {
            VectorReader vr = TemporalSpaceUtils.getVectorReader(tri.getMainDir(), years.get(i), false);
            vr.init();
            if (vr.getVector(word) != null) {
                List<ObjectVector> nv = TemporalSpaceUtils.getNearestVectors(vr, word, ns);
                vr.close();
                return nv;
            }
            vr.close();
            i--;
        }
        return new ArrayList();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o") && cmd.hasOption("p")) {
                Tri tri = new Tri();
                tri.setMaindir(cmd.getOptionValue("i"));
                Set<String> sw = new HashSet<>();
                if (cmd.hasOption("s")) {
                    sw = TemporalSpaceUtils.loadStopWord(cmd.getOptionValue("s"));
                }
                int ns = Integer.parseInt(cmd.getOptionValue("n", "25"));
                List<String> year = tri.year(0, Integer.MAX_VALUE);
                List<ChangePoint> points = CPDUtils.loadCPD(new File(cmd.getOptionValue("p")));
                Collections.sort(points, new ChangePointLabelComparator());
                BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                String prevLabel = "";
                VectorReader vr = null;
                VectorReader prevr = null;
                for (ChangePoint point : points) {
                    if (!point.getLabel().equals(prevLabel)) {
                        if (prevr != null) {
                            prevr.close();
                            prevr = null;
                        }
                        if (vr != null) {
                            vr.close();
                            vr = null;
                        }
                        int indexOf = year.indexOf(point.getLabel());
                        if (indexOf > 0) {
                            prevr = TemporalSpaceUtils.getVectorReader(new File(cmd.getOptionValue("i")), year.get(indexOf - 1), true);
                            prevr.init();
                        }
                        vr = TemporalSpaceUtils.getVectorReader(new File(cmd.getOptionValue("i")), point.getLabel(), true);
                        vr.init();
                        prevLabel = point.getLabel();
                    }
                    if (prevr != null && vr != null) {
                        writer.append("#CPD\t").append(point.getWord()).append("\t").append(point.getLabel()).append("\t").append(String.valueOf(point.getConfidance())).append("\n");
                        List<ObjectVector> v1;
                        if (prevr.getVector(point.getWord()) != null) {
                            v1 = TemporalSpaceUtils.getNearestVectors(prevr, point.getWord(), ns);
                        } else {
                            v1 = search(tri, year, point.getWord(), prevLabel, ns);
                        }
                        List<ObjectVector> v2 = TemporalSpaceUtils.getNearestVectors(vr, point.getWord(), ns);
                        int max = Math.max(v1.size(), v2.size());
                        for (int k = 0; k < max; k++) {
                            if (k < v1.size()) {
                                writer.append(v1.get(k).getKey()).append("\t").append(String.valueOf(v1.get(k).getScore()));
                            } else {
                                writer.append("#NA\t0.0");
                            }
                            if (k < v2.size()) {
                                writer.append("\t").append(v2.get(k).getKey()).append("\t").append(String.valueOf(v2.get(k).getScore()));
                            } else {
                                writer.append("\t#NA\t0.0");
                            }
                            writer.newLine();
                        }
                    }
                }
                writer.close();
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build neighborhood for CP points.", options, true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
