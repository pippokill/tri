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

import di.uniba.it.tri.script.gbooks.*;
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
public class GBooks2PlainV2 {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("corpus", true, "Ngram corpus dir").
                addOption("out", true, "Output dir").
                addOption("from", true, "Start year (default value=1850)").
                addOption("to", true, "End year (default value=2012)").
                addOption("step", true, "Time interval size  (default value=10)").
                addOption("regexp", true, "Regular expression for tokens (default [a-z]+)");
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
                String regexp = cmd.getOptionValue("regexp", GBooksUtils.REG_EXP_EN);
                Logger.getLogger(GBooks2PlainV2.class.getName()).log(Level.INFO, "Reg-exp: {0}", regexp);
                File corpusdir = new File(cmd.getOptionValue("corpus"));
                File[] listFiles = corpusdir.listFiles();
                for (File file : listFiles) {
                    if (file.isFile() && file.getName().endsWith(".gz")) {
                        Logger.getLogger(GBooks2PlainV2.class.getName()).log(Level.INFO, "Open file: {0}", file.getName());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                        String line;
                        while (reader.ready()) {
                            line = reader.readLine();
                            Ngram ngram = GBooksUtils.parseNgramV2(line, regexp);
                            if (!ngram.getTokens().isEmpty() && ngram.getYear() >= start && ngram.getYear() <= end) {
                                int idx = (ngram.getYear() - start) / step;
                                BufferedWriter writer = writers.get(idx);
                                if (writer == null) {
                                    writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cmd.getOptionValue("out") + "/plain_" + (start + idx * step) + ".ngrams.gz"))));
                                    writers.put(idx, writer);
                                }
                                ngram.write(writer);
                                writer.newLine();
                            }
                        }
                        reader.close();
                    }
                }
                for (BufferedWriter writer : writers.values()) {
                    writer.close();
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build plain text file with occurrences using Google-ngram as input", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(GBooks2PlainV2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
