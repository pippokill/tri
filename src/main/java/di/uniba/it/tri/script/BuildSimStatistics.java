/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.space.TemporalSpaceUtils;
import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.api.TriResultObject;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class BuildSimStatistics {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("o", true, "Output file")
                .addOption("f", true, "Output format plain or csv (default=plain)");
    }

    /**
     * base_dir_1 output_file_1
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                String format = cmd.getOptionValue("f", "plain");
                if (!(format.equals("plain") || format.equals("csv"))) {
                    throw new IllegalArgumentException("No valid format");
                }
                Tri api = new Tri();
                api.setMaindir(cmd.getOptionValue("i"));
                api.load("file", null, "-1");
                char sep = '\t';
                BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                if (format.equals("csv")) {
                    writer.append(",word");
                    List<String> years = api.year(0, Integer.MAX_VALUE);
                    for (String year : years) {
                        writer.append(",");
                        writer.append(year);
                    }
                    writer.newLine();
                    sep = ',';
                }
                VectorReader evr = api.getStores().get(Tri.ELEMENTAL_NAME);
                Iterator<String> keys = evr.getKeys();
                int c = 0;
                System.out.println();
                List<String> availableYears = TemporalSpaceUtils.getAvailableYears(new File(cmd.getOptionValue("i")), -Integer.MAX_VALUE, Integer.MAX_VALUE);
                Collections.sort(availableYears);
                Map<String, VectorReader> vrmap = new HashMap<>();
                //just read vector dimension
                for (String year : availableYears) {
                    System.out.println("Loading " + year);
                    VectorReader vrd = TemporalSpaceUtils.getVectorReader(new File(cmd.getOptionValue("i")), year, true);
                    vrd.init();
                    vrmap.put(year, vrd);
                }
                int dimension = evr.getDimension();
                long time = System.currentTimeMillis();
                int id = 0;
                while (keys.hasNext()) {
                    String key = keys.next();
                    Vector precv = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                    List<TriResultObject> list = new ArrayList<>();
                    for (String ys : availableYears) {
                        VectorReader vr = vrmap.get(ys);
                        Vector v = vr.getVector(key);
                        if (v != null) {
                            Vector copy = precv.copy();
                            copy.superpose(v, 1, null);
                            copy.normalize();
                            list.add(new TriResultObject(ys + "\t" + key, (float) copy.measureOverlap(precv)));
                            precv.superpose(v, 1, null);
                            precv.normalize();
                        } else {
                            list.add(new TriResultObject(ys + "\t" + key, -1));
                        }
                    }
                    if (format.equals("csv")) {
                        writer.append(String.valueOf(id));
                        writer.append(sep);
                    }
                    writer.append(key);
                    list.remove(0);
                    for (TriResultObject r : list) {
                        if (r.getScore() >= 0) {
                            writer.append(sep).append(String.valueOf(r.getScore()));
                        } else {
                            writer.append(sep).append(String.valueOf(0f));
                        }
                    }
                    writer.newLine();
                    c++;
                    if (c % 10000 == 0) {
                        System.out.println("Processed " + c + " words\t" + ((System.currentTimeMillis() - time) / 100) + " sec.");
                        time = System.currentTimeMillis();
                    }
                    id++;
                }
                writer.close();
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build sim matrix", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(BuildSimStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
