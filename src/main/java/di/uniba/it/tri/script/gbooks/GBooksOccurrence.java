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
package di.uniba.it.tri.script.gbooks;

import di.uniba.it.tri.occ.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.StopWordFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
public class GBooksOccurrence {

    private File outputDir = new File("./");

    private static final Logger LOGGER = Logger.getLogger(GBooksOccurrence.class.getName());

    private boolean toLowerCase = false;

    private Filter swFilter = null;

    private String tokenRegExp = "^.+$";

    /**
     * Get the RegExp used to fetch files
     *
     * @return The RegExp
     */
    public String getTokenRegExp() {
        return tokenRegExp;
    }

    /**
     * Set the RegExp used to fetch files
     *
     * @param tokenRegExp The RegExp
     */
    public void setTokenRegExp(String tokenRegExp) {
        this.tokenRegExp = tokenRegExp;
    }

    /**
     * Get the output directory
     *
     * @return The output directory
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Set the output directory
     *
     * @param outputDir The output directory
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public Filter getSwFilter() {
        return swFilter;
    }

    public void setSwFilter(Filter swFilter) {
        this.swFilter = swFilter;
    }

    public boolean isToLowerCase() {
        return toLowerCase;
    }

    public void setToLowerCase(boolean toLowerCase) {
        this.toLowerCase = toLowerCase;
    }

    private OccOutput count(File file) throws Exception {
        Map<Integer, Multiset<Integer>> map = new HashMap<>();
        BiMap<String, Integer> dict = HashBiMap.create();
        int id = 0;
        LOGGER.log(Level.INFO, "Counting file: {0}", file.getName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        String line;
        while (reader.ready()) {
            line = reader.readLine();
            String[] values = line.split("\\t");
            List<String> tokens;
            if (isToLowerCase()) {
                tokens = new ArrayList<>(Arrays.asList(values[0].toLowerCase().split("\\s")));
            } else {
                tokens = new ArrayList<>(Arrays.asList(values[0].split("\\s")));
            }
            if (swFilter != null) {
                swFilter.filter(tokens);
            }
            for (int i = tokens.size() - 1; i >= 0; i--) {
                if (!tokens.get(i).matches(tokenRegExp)) {
                    tokens.remove(i);
                }
            }
            int c = Integer.parseInt(values[1]);
            for (int k = 0; k < c; k++) {
                for (int i = 0; i < tokens.size(); i++) {
                    Integer tid = dict.get(tokens.get(i));
                    if (tid == null) {
                        tid = id;
                        dict.put(tokens.get(i), tid);
                        id++;
                    }
                    Multiset<Integer> multiset = map.get(tid);
                    if (multiset == null) {
                        multiset = HashMultiset.create();
                        map.put(tid, multiset);
                    }
                    for (int j = 0; j < tokens.size(); j++) {
                        if (j != i) {
                            Integer tjid = dict.get(tokens.get(j));
                            if (tjid == null) {
                                tjid = id;
                                dict.put(tokens.get(j), tjid);
                                id++;
                            }
                            multiset.add(tjid);
                        }
                    }
                }
            }
        }
        return new OccOutput(map, dict);
    }

    /**
     * Build the co-occurrences matrix
     *
     * @param startingDir The corpus directory containing files with year
     * metadata
     * @throws Exception
     */
    public void process(File startingDir) throws Exception {
        LOGGER.log(Level.INFO, "Starting dir: {0}", startingDir.getAbsolutePath());
        LOGGER.log(Level.INFO, "Output dir: {0}", outputDir.getAbsolutePath());
        LOGGER.log(Level.INFO, "Lower case: {0}", isToLowerCase());
        LOGGER.log(Level.INFO, "Token regexp: {0}", tokenRegExp);
        File[] files = startingDir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".gz")) {
                OccOutput count = count(file);
                String[] splitname = file.getName().split("\\.");
                String filename = splitname[0] + ".occ.gz";
                save(count, filename);
            }
        }

    }

    private void save(OccOutput count, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/" + filename))));
        Iterator<String> keys = count.getDict().keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Multiset<Integer> mset = count.getOcc().get(count.getDict().get(key));
            if (mset != null) {
                writer.append(key);
                Set<Multiset.Entry<Integer>> entrySet = mset.entrySet();
                for (Entry<Integer> entry : entrySet) {
                    writer.append("\t").append(count.getDict().inverse().get(entry.getElement())).append("\t").append(String.valueOf(entry.getCount()));
                }
                writer.newLine();
            }
        }
        writer.close();
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("in", true, "The corpus directory containing ngrams")
                .addOption("out", true, "Output directory where output will be stored")
                .addOption("r", true, "Regular expression used to filter tokens (optional, default \".+\")")
                .addOption("sw", true, "Stop word file (optional)").
                addOption("lower", true, "Enable lower case (default=false)");
    }

    /**
     * Build the co-occurrences matrix given the set of files with year metadata
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("in") && cmd.hasOption("out")) {
                try {
                    GBooksOccurrence builder = new GBooksOccurrence();
                    builder.setOutputDir(new File(cmd.getOptionValue("out")));
                    if (cmd.hasOption("s")) {
                        LOGGER.info("Load stop word...");
                        builder.setSwFilter(new StopWordFilter(OccUtils.loadSet(new File(cmd.getOptionValue("s")))));
                    }
                    builder.setTokenRegExp(cmd.getOptionValue("r", "^.+$"));
                    builder.setToLowerCase(Boolean.parseBoolean(cmd.getOptionValue("lower", "false")));
                    builder.process(new File(cmd.getOptionValue("in")));
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build the co-occurrences matrix given the set of files with ngrams", options, true);
            }
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    static class OccOutput {

        private Map<Integer, Multiset<Integer>> occ;

        private BiMap<String, Integer> dict;

        public OccOutput(Map<Integer, Multiset<Integer>> occ, BiMap<String, Integer> dict) {
            this.occ = occ;
            this.dict = dict;
        }

        public Map<Integer, Multiset<Integer>> getOcc() {
            return occ;
        }

        public void setOcc(Map<Integer, Multiset<Integer>> occ) {
            this.occ = occ;
        }

        public BiMap<String, Integer> getDict() {
            return dict;
        }

        public void setDict(BiMap<String, Integer> dict) {
            this.dict = dict;
        }

    }

}
