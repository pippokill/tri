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
package di.uniba.it.tri.occ;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import di.uniba.it.tri.extractor.Extractor;
import di.uniba.it.tri.extractor.IterableExtractor;
import di.uniba.it.tri.tokenizer.TriTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class BuildOccurrence {

    private int winsize = 5;

    private File outputDir = new File("./");

    private static final Logger logger = Logger.getLogger(BuildOccurrence.class.getName());

    private Extractor extractor = null;

    private IterableExtractor itExtractor = null;

    private TriTokenizer tokenizer;

    private String filenameRegExp = "^.+$";

    /**
     * Get the RegExp used to fetch files
     *
     * @return The RegExp
     */
    public String getFilenameRegExp() {
        return filenameRegExp;
    }

    /**
     * Set the RegExp used to fetch files
     *
     * @param filenameRegExp The RegExp
     */
    public void setFilenameRegExp(String filenameRegExp) {
        this.filenameRegExp = filenameRegExp;
    }

    /**
     * Get the window size
     *
     * @return The window size
     */
    public int getWinsize() {
        return winsize;
    }

    /**
     * Set the window size
     *
     * @param winsize The window size
     */
    public void setWinsize(int winsize) {
        this.winsize = winsize;
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

    /**
     * Get the extractor
     *
     * @return The extractor
     */
    public Extractor getExtractor() {
        return extractor;
    }

    /**
     * Set the extractor
     *
     * @param extractor The extractor
     */
    public void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }

    /**
     * Get the tokenizer
     *
     * @return Teh tokenizer
     */
    public TriTokenizer getTokenizer() {
        return tokenizer;
    }

    /**
     * Get the iterable extractor
     *
     * @return The iterable extractor
     */
    public IterableExtractor getItExtractor() {
        return itExtractor;
    }

    /**
     * Set the iterable extractor
     *
     * @param itExtractor The iterable extractor
     */
    public void setItExtractor(IterableExtractor itExtractor) {
        this.itExtractor = itExtractor;
    }

    /**
     * Set the tokenizer
     *
     * @param tokenizer The tokenizer
     */
    public void setTokenizer(TriTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private OccOutput count(File startingDir, int year) throws IOException {
        Map<Integer, Multiset<Integer>> map = new HashMap<>();
        BiMap<String, Integer> dict = HashBiMap.create();
        int id = 0;
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().matches(filenameRegExp) && file.getName().lastIndexOf("_") > -1 && file.getName().endsWith(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                StringReader reader = extractor.extract(file);
                List<String> tokens = tokenizer.getTokens(reader);
                for (int i = 0; i < tokens.size(); i++) {
                    int start = Math.max(0, i - winsize);
                    int end = Math.min(tokens.size() - 1, i + winsize);
                    for (int j = start; j < end; j++) {
                        if (i != j) {
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

    private OccOutput countIterable(File startingDir, int year) throws IOException {
        Map<Integer, Multiset<Integer>> map = new HashMap<>();
        BiMap<String, Integer> dict = HashBiMap.create();
        int id = 0;
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().matches(filenameRegExp) && file.getName().lastIndexOf("_") > -1 && file.getName().endsWith(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                itExtractor.extract(file);
                while (itExtractor.hasNext()) {
                    List<String> tokens = tokenizer.getTokens(itExtractor.next());
                    for (int i = 0; i < tokens.size(); i++) {
                        int start = Math.max(0, i - winsize);
                        int end = Math.min(tokens.size() - 1, i + winsize);
                        for (int j = start; j < end; j++) {
                            if (i != j) {
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
        }
        return new OccOutput(map, dict);
    }

    /**
     * Build the co-occurrences matrix
     *
     * @param startingDir The corpus directory containing files with year
     * metadata
     * @param tokenizer The tokenizer used to extract tokens
     * @throws Exception
     */
    public void process(File startingDir, TriTokenizer tokenizer) throws Exception {
        logger.log(Level.INFO, "Starting dir: {0}", startingDir.getAbsolutePath());
        logger.log(Level.INFO, "Output dir: {0}", outputDir.getAbsolutePath());
        logger.log(Level.INFO, "Window size: {0}", winsize);
        File[] listFiles = startingDir.listFiles();
        int minYear = Integer.MAX_VALUE;
        int maxYear = -Integer.MAX_VALUE;
        for (File file : listFiles) {
            int i = file.getName().lastIndexOf("_");
            if (i > -1 && file.getName().substring(0, i).matches(filenameRegExp)) {
                //fix year to consider only the last 4 chars
                //old year int year = Integer.parseInt(file.getName().substring(i + 1));
                int year = Integer.parseInt(file.getName().substring(file.getName().length() - 4, file.getName().length()));
                if (year < minYear) {
                    minYear = year;
                }
                if (year > maxYear) {
                    maxYear = year;
                }
            }
        }
        logger.log(Level.INFO, "Form year: {0}", minYear);
        logger.log(Level.INFO, "To year: {0}", maxYear);
        for (int k = minYear; k <= maxYear; k++) {
            logger.log(Level.INFO, "Counting year: {0}", k);
            OccOutput count = null;
            if (extractor != null) {
                count = count(startingDir, k);
            } else if (itExtractor != null) {
                count = countIterable(startingDir, k);
            }
            save(count, k);
        }
    }

    private void save(OccOutput count, int year) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/count_" + year));
        Iterator<String> keys = count.getDict().keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            writer.append(key);
            Set<Multiset.Entry<Integer>> entrySet = count.getOcc().get(count.getDict().get(key)).entrySet();
            for (Entry<Integer> entry : entrySet) {
                writer.append("\t").append(count.getDict().inverse().get(entry.getElement())).append("\t").append(String.valueOf(entry.getCount()));
            }
            writer.newLine();
        }
        writer.close();
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("c", true, "The corpus directory containing files with year metadata")
                .addOption("o", true, "Output directory where output will be stored")
                .addOption("w", true, "The window size used to compute the co-occurrences (optional, default 5)")
                .addOption("e", true, "The class used to extract the content from files")
                .addOption("t", true, "The class used to tokenize the content (optional, defaul StandardTokenizer)")
                .addOption("r", true, "Regular expression used to fetch files (optional, default \".+\")");
    }

    /**
     * Build the co-occurrences matrix given the set of files with year metadata
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("c") && cmd.hasOption("o") && cmd.hasOption("e")) {
                try {
                    Object classExtractor = Class.forName("di.uniba.it.tri.extractor." + cmd.getOptionValue("e")).newInstance();
                    TriTokenizer tokenizer = (TriTokenizer) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).newInstance();
                    BuildOccurrence builder = new BuildOccurrence();
                    builder.setOutputDir(new File(cmd.getOptionValue("o")));
                    builder.setWinsize(Integer.parseInt(cmd.getOptionValue("w", "5")));
                    if (classExtractor instanceof Extractor) {
                        builder.setExtractor((Extractor) classExtractor);
                    } else if (classExtractor instanceof IterableExtractor) {
                        builder.setItExtractor((IterableExtractor) classExtractor);
                    } else {
                        throw new IllegalArgumentException("No valid extractor");
                    }
                    builder.setTokenizer(tokenizer);
                    builder.setFilenameRegExp(cmd.getOptionValue("r", "^.+$"));
                    builder.process(new File(cmd.getOptionValue("c")), tokenizer);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build the co-occurrences matrix given the set of files with year metadata", options, true);
            }
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
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
