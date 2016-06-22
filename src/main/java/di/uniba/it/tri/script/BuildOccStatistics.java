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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
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
public class BuildOccStatistics {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("o", true, "Output file")
                .addOption("f", true, "Output format plain or csv (default=plain)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                String format = cmd.getOptionValue("f", "plain");
                if (format.equals("plat") || format.equals("csv")) {
                    File startDir = new File(cmd.getOptionValue("i"));
                    File[] files = startDir.listFiles();
                    Arrays.sort(files);
                    Map<String, int[]> cmap = new TreeMap<>();
                    int k = 0;
                    for (File file : files) {
                        System.out.println("Reading " + file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                        String[] split;
                        while (reader.ready()) {
                            split = reader.readLine().split("\\s+");
                            int c = 0;
                            for (int i = 2; i < split.length; i = i + 2) {
                                c += Integer.parseInt(split[i]);
                            }
                            int[] vc = cmap.get(split[0]);
                            if (vc == null) {
                                vc = new int[files.length];
                                cmap.put(split[0], vc);
                            }
                            vc[k] = c;
                        }
                        reader.close();
                        k++;
                    }
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    if (format.equals("csv")) {
                        writer.append(",word");
                        for (File f : files) {
                            writer.append(",");
                            writer.append(f.getName().replace("count_", ""));
                        }
                        writer.newLine();
                    }
                    int id = 0;
                    char sep = '\t';
                    if (format.equals("csv")) {
                        sep = ',';
                    }
                    for (Map.Entry<String, int[]> e : cmap.entrySet()) {
                        if (format.equals("csv")) {
                            writer.append(String.valueOf(id));
                            writer.append(sep);
                        }
                        writer.append(e.getKey());
                        int[] vc = e.getValue();
                        for (int c : vc) {
                            writer.append(sep).append(String.valueOf(c));
                        }
                        writer.newLine();
                        id++;
                    }
                    writer.close();
                } else {
                    throw new IllegalArgumentException("No valid format");
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build frequencies matrix", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(BuildOccStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
