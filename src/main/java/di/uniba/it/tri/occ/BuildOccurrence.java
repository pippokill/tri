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
import di.uniba.it.tri.data.DictionaryEntry;
import di.uniba.it.tri.extractor.Extractor;
import di.uniba.it.tri.extractor.IterableExtractor;
import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.KeywordFinder;
import di.uniba.it.tri.tokenizer.StopWordFilter;
import di.uniba.it.tri.tokenizer.TriTokenizer;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class BuildOccurrence {

    private int winsize = 5;

    private File outputDir = new File("./");

    private static final Logger logger = Logger.getLogger(BuildOccurrence.class.getName());

    private Extractor extractor = null;

    private IterableExtractor itExtractor = null;

    private Filter filter = null;

    private Filter swFilter = null;

    private TriTokenizer tokenizer;

    private String filenameRegExp = "^.+$";

    private Map<String, Integer> dict;

    private Set<String> vocabulary;

    private int vocSize = 50000;

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

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getSwFilter() {
        return swFilter;
    }

    public void setSwFilter(Filter swFilter) {
        this.swFilter = swFilter;
    }

    public int getVocSize() {
        return vocSize;
    }

    public void setVocSize(int vocSize) {
        this.vocSize = vocSize;
    }

    private void buildDict(File startingDir, int year) throws Exception {
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            int eindex = file.getName().lastIndexOf(".");
            String filename;
            if (eindex < 0) {
                filename = file.getName();
            } else {
                filename = file.getName().substring(0, eindex);
            }
            if (filename.matches(filenameRegExp) && filename.contains(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                StringReader reader = extractor.extract(file);
                List<String> tokens = tokenizer.getTokens(reader);
                if (swFilter != null) {
                    swFilter.filter(tokens);
                }
                if (filter != null) {
                    filter.filter(tokens);
                }
                for (String t : tokens) {
                    Integer c = dict.get(t);
                    if (c == null) {
                        dict.put(t, 1);
                    } else {
                        dict.put(t, c + 1);
                    }
                }
            }
        }
    }

    private void buildDictIterable(File startingDir, int year) throws Exception {
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            int eindex = file.getName().lastIndexOf(".");
            String filename;
            if (eindex < 0) {
                filename = file.getName();
            } else {
                filename = file.getName().substring(0, eindex);
            }
            if (filename.matches(filenameRegExp) && filename.contains(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                itExtractor.extract(file);
                while (itExtractor.hasNext()) {
                    List<String> tokens = tokenizer.getTokens(itExtractor.next());
                    if (swFilter != null) {
                        swFilter.filter(tokens);
                    }
                    if (filter != null) {
                        filter.filter(tokens);
                    }
                    for (String t : tokens) {
                        Integer c = dict.get(t);
                        if (c == null) {
                            dict.put(t, 1);
                        } else {
                            dict.put(t, c + 1);
                        }
                    }

                }
            }
        }
    }

    private OccOutput count(File startingDir, int year) throws Exception {
        Map<Integer, Map<Integer, Integer>> map = new Int2ObjectOpenHashMap<>();
        BiMap<String, Integer> countDict = HashBiMap.create();
        int id = 0;
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            int eindex = file.getName().lastIndexOf(".");
            String filename;
            if (eindex < 0) {
                filename = file.getName();
            } else {
                filename = file.getName().substring(0, eindex);
            }
            if (filename.matches(filenameRegExp) && filename.contains(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                StringReader reader = extractor.extract(file);
                List<String> tokens = tokenizer.getTokens(reader);
                if (swFilter != null) {
                    swFilter.filter(tokens);
                }
                if (filter != null) {
                    filter.filter(tokens);
                }
                for (int i = 0; i < tokens.size(); i++) {
                    if (vocabulary.contains(tokens.get(i))) {
                        int start = Math.max(0, i - winsize);
                        int end = Math.min(tokens.size() - 1, i + winsize);
                        for (int j = start; j <= end; j++) {
                            if (i != j && vocabulary.contains(tokens.get(j))) {
                                Integer tid = countDict.get(tokens.get(i));
                                if (tid == null) {
                                    tid = id;
                                    countDict.put(tokens.get(i), tid);
                                    id++;
                                }
                                Map<Integer, Integer> countMap = map.get(tid);
                                if (countMap == null) {
                                    countMap = new Int2IntOpenHashMap();
                                    map.put(tid, countMap);
                                }
                                Integer tjid = countDict.get(tokens.get(j));
                                if (tjid == null) {
                                    tjid = id;
                                    countDict.put(tokens.get(j), tjid);
                                    id++;
                                }
                                Integer c = countMap.get(tjid);
                                if (c == null) {
                                    countMap.put(tjid, 1);
                                } else {
                                    countMap.put(tjid, c + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new OccOutput(map, countDict);
    }

    private OccOutput countIterable(File startingDir, int year) throws Exception {
        Map<Integer, Map<Integer, Integer>> map = new Int2ObjectOpenHashMap<>();
        BiMap<String, Integer> countDict = HashBiMap.create();
        int id = 0;
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            int eindex = file.getName().lastIndexOf(".");
            String filename;
            if (eindex < 0) {
                filename = file.getName();
            } else {
                filename = file.getName().substring(0, eindex);
            }
            if (filename.matches(filenameRegExp) && filename.contains(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                itExtractor.extract(file);
                while (itExtractor.hasNext()) {
                    List<String> tokens = tokenizer.getTokens(itExtractor.next());
                    if (swFilter != null) {
                        swFilter.filter(tokens);
                    }
                    if (filter != null) {
                        filter.filter(tokens);
                    }
                    for (int i = 0; i < tokens.size(); i++) {
                        if (vocabulary.contains(tokens.get(i))) {
                            int start = Math.max(0, i - winsize);
                            int end = Math.min(tokens.size() - 1, i + winsize);
                            for (int j = start; j <= end; j++) {
                                if (i != j && vocabulary.contains(tokens.get(j))) {
                                    Integer tid = countDict.get(tokens.get(i));
                                    if (tid == null) {
                                        tid = id;
                                        countDict.put(tokens.get(i), tid);
                                        id++;
                                    }
                                    Map<Integer, Integer> countMap = map.get(tid);
                                    if (countMap == null) {
                                        countMap = new Int2IntOpenHashMap();
                                        map.put(tid, countMap);
                                    }
                                    Integer tjid = countDict.get(tokens.get(j));
                                    if (tjid == null) {
                                        tjid = id;
                                        countDict.put(tokens.get(j), tjid);
                                        id++;
                                    }
                                    Integer c = countMap.get(tjid);
                                    if (c == null) {
                                        countMap.put(tjid, 1);
                                    } else {
                                        countMap.put(tjid, c + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new OccOutput(map, countDict);
    }

    /**
     * Build the co-occurrences matrix
     *
     * @param startingDir The corpus directory containing files with year
     * metadata
     * @throws Exception
     */
    public void process(File startingDir) throws Exception {
        logger.log(Level.INFO, "Starting dir: {0}", startingDir.getAbsolutePath());
        logger.log(Level.INFO, "Output dir: {0}", outputDir.getAbsolutePath());
        logger.log(Level.INFO, "Vocabulary size: {0}", vocSize);
        logger.log(Level.INFO, "Window size: {0}", winsize);
        File[] listFiles = startingDir.listFiles();
        int minYear = Integer.MAX_VALUE;
        int maxYear = -Integer.MAX_VALUE;
        Pattern yp = Pattern.compile("[0-9]+");
        for (File file : listFiles) {
            int eindex = file.getName().lastIndexOf(".");
            String filename;
            if (eindex < 0) {
                filename = file.getName();
            } else {
                filename = file.getName().substring(0, eindex);
            }
            if (filename.matches(filenameRegExp)) {
                Matcher matcher = yp.matcher(filename);
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group());
                    if (year < minYear) {
                        minYear = year;
                    }
                    if (year > maxYear) {
                        maxYear = year;
                    }
                }
            }
        }
        logger.log(Level.INFO, "Form year: {0}", minYear);
        logger.log(Level.INFO, "To year: {0}", maxYear);
        logger.log(Level.INFO, "Build dictionary...");
        dict = new Object2IntOpenHashMap<>();
        for (int k = minYear; k <= maxYear; k++) {
            //logger.log(Level.INFO, "Dict for year: {0}", k);
            if (extractor != null) {
                buildDict(startingDir, k);
            } else if (itExtractor != null) {
                buildDictIterable(startingDir, k);
            }
        }
        logger.log(Level.INFO, "Dictionary size: {0}", dict.size());
        logger.log(Level.INFO, "Build vocabulary...");
        List<DictionaryEntry> ld = new ArrayList<>();
        for (Map.Entry<String, Integer> de : dict.entrySet()) {
            ld.add(new DictionaryEntry(de.getKey(), de.getValue()));
        }
        dict.clear();
        dict = null;
        Collections.sort(ld, Collections.reverseOrder());
        if (ld.size() > vocSize) {
            ld = ld.subList(0, vocSize);
        }
        vocabulary = new ObjectOpenHashSet<>();
        for (DictionaryEntry de : ld) {
            vocabulary.add(de.getWord());
        }
        ld.clear();
        ld = null;
        System.gc();
        logger.log(Level.INFO, "Vocabulary size: {0}", vocabulary.size());
        for (int k = minYear; k <= maxYear; k++) {
            //logger.log(Level.INFO, "Counting year: {0}", k);
            OccOutput count = null;
            if (extractor != null) {
                count = count(startingDir, k);
            } else if (itExtractor != null) {
                count = countIterable(startingDir, k);
            }
            if (count != null && count.getOcc().size() > 0) {
                save(count, k);
            }
        }
    }

    private void save(OccOutput count, int year) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/count_" + year))));
        Iterator<String> keys = count.getDict().keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Map<Integer, Integer> mset = count.getOcc().get(count.getDict().get(key));
            if (mset != null) {
                writer.append(key);
                Set<Map.Entry<Integer, Integer>> entrySet = mset.entrySet();
                for (Map.Entry<Integer, Integer> entry : entrySet) {
                    writer.append("\t").append(count.getDict().inverse().get(entry.getKey())).append("\t").append(String.valueOf(entry.getValue()));
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
        options.addOption("c", true, "The corpus directory containing files with year metadata")
                .addOption("o", true, "Output directory where output will be stored")
                .addOption("w", true, "The window size used to compute the co-occurrences (optional, default 5)")
                .addOption("e", true, "The class used to extract the content from files")
                .addOption("t", true, "The class used to tokenize the content (optional, default StandardTokenizer)")
                .addOption("r", true, "Regular expression used to fetch files (optional, default \".+\")")
                .addOption("s", true, "Stop word file (optional)")
                .addOption("f", true, "Filter class (optional)")
                .addOption("k", true, "Load keyword list")
                .addOption("n", true, "Size of the vocabulary (optional, default 50000)");
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
                    TriTokenizer tokenizer = null;
                    if (cmd.hasOption("k")) {
                        Constructor<?>[] constructors = Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).getConstructors();
                        for (Constructor c : constructors) {
                            if (c.getParameterTypes().length == 1 && c.getParameterTypes()[0].equals(KeywordFinder.class)) {
                                KeywordFinder finder = new KeywordFinder(new File(cmd.getOptionValue("k")));
                                tokenizer = (TriTokenizer) c.newInstance(finder);
                                break;
                            }
                        }
                        //fall-back strategy
                        if (tokenizer == null) {
                            logger.log(Level.WARNING, "No KeywordFinder constructor for {0}, use default constructor...", cmd.getOptionValue("t"));
                            tokenizer = (TriTokenizer) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).newInstance();
                        }
                    } else {
                        tokenizer = (TriTokenizer) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).newInstance();
                    }
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
                    if (cmd.hasOption("s")) {
                        logger.info("Load stop word...");
                        builder.setSwFilter(new StopWordFilter(OccUtils.loadSet(new File(cmd.getOptionValue("s")))));
                    }
                    if (cmd.hasOption("f")) {
                        logger.info("Load filter...");
                        Filter filter = (Filter) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("f")).newInstance();
                        builder.setFilter(filter);
                    }
                    builder.setFilenameRegExp(cmd.getOptionValue("r", "^.+$"));
                    builder.setVocSize(Integer.parseInt(cmd.getOptionValue("n", "50000")));
                    builder.process(new File(cmd.getOptionValue("c")));
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

        private Map<Integer, Map<Integer, Integer>> occ;

        private BiMap<String, Integer> dict;

        public OccOutput(Map<Integer, Map<Integer, Integer>> occ, BiMap<String, Integer> dict) {
            this.occ = occ;
            this.dict = dict;
        }

        public Map<Integer, Map<Integer, Integer>> getOcc() {
            return occ;
        }

        public void setOcc(Map<Integer, Map<Integer, Integer>> occ) {
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
