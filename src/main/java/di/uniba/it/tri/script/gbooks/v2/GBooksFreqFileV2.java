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
package di.uniba.it.tri.script.gbooks.v2;

import di.uniba.it.tri.data.DictionaryEntry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GBooksFreqFileV2 {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("in", true, "Ngram plain dir").
                addOption("out", true, "Output dir").
                addOption("lower", false, "Enable lower case (default=false)").
                addOption("c", false, "Use counter");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("in") && cmd.hasOption("out")) {
                boolean lower = cmd.hasOption("lower");
                boolean useCounter = cmd.hasOption("c");
                File corpusDir = new File(cmd.getOptionValue("in"));
                File[] files = corpusDir.listFiles();
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".gz")) {
                        Logger.getLogger(GBooksFreqFileV2.class.getName()).log(Level.INFO, "Open file {0}", file.getName());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                        Map<String, Integer> dict = new HashMap<>();
                        String line;
                        while (reader.ready()) {
                            if (lower) {
                                line = reader.readLine().toLowerCase();
                            } else {
                                line = reader.readLine();
                            }
                            String[] values = line.split("\\t");
                            String[] tokens = values[0].split("\\s");
                            int c = 1;
                            if (useCounter) {
                                c = Integer.parseInt(values[1]);
                            }
                            for (String token : tokens) {
                                Integer v = dict.get(token);
                                if (v == null) {
                                    dict.put(token, c);
                                } else {
                                    dict.put(token, v + c);
                                }
                            }
                        }
                        reader.close();
                        String[] splitname = file.getName().split("\\.");
                        List<DictionaryEntry> list = new ArrayList();
                        for (Map.Entry<String, Integer> e : dict.entrySet()) {
                            if (!e.getKey().equals("_")) {
                                list.add(new DictionaryEntry(e.getKey(), e.getValue()));
                            }
                        }
                        Collections.sort(list, Collections.reverseOrder());
                        BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("out") + "/" + splitname[0] + ".freq"));
                        for (DictionaryEntry e : list) {
                            writer.append(e.getWord()).append("\t").append(String.valueOf(e.getCounter()));
                            writer.newLine();
                        }
                        writer.close();
                    }
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build token frequencies file given ngram files", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(GBooksFreqFileV2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
