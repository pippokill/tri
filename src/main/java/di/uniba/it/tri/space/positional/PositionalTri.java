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
package di.uniba.it.tri.space.positional;

import di.uniba.it.tri.occ.*;
import di.uniba.it.tri.data.DictionaryEntry;
import di.uniba.it.tri.extractor.IterableExtractor;
import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.KeywordFinder;
import di.uniba.it.tri.tokenizer.StopWordFilter;
import di.uniba.it.tri.tokenizer.TriTokenizer;
import di.uniba.it.tri.vectors.PermutationUtils;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class PositionalTri {

    private int winsize = 5;

    private File outputDir = new File("./");

    private static final Logger LOG = Logger.getLogger(PositionalTri.class.getName());

    private IterableExtractor extractor = null;

    private Filter filter = null;

    private Filter swFilter = null;

    private TriTokenizer tokenizer;

    private String filenameRegExp = "^.+$";

    private int vocSize = 50000;

    private int dimension = 500;

    private int seed = 10;

    private long totalOcc = 0;

    private double t = 0.001;

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
    public IterableExtractor getExtractor() {
        return extractor;
    }

    /**
     * Set the iterable extractor
     *
     * @param extractor The iterable extractor
     */
    public void setExtractor(IterableExtractor extractor) {
        this.extractor = extractor;
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

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    private void buildDict(File startingDir, Map<String, Integer> cmap, int year) throws Exception {
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
                LOG.log(Level.INFO, "Working file: {0}", file.getName());
                extractor.extract(file);
                while (extractor.hasNext()) {
                    List<String> tokens = tokenizer.getTokens(extractor.next());
                    if (swFilter != null) {
                        swFilter.filter(tokens);
                    }
                    if (filter != null) {
                        filter.filter(tokens);
                    }
                    for (String t : tokens) {
                        Integer c = cmap.get(t);
                        if (c == null) {
                            cmap.put(t, 1);
                        } else {
                            cmap.put(t, c + 1);
                        }
                    }
                }
            }
        }
    }

    private void buildSemanticVectors(File startingDir, File outputFile, int year, Map<String, Integer> dict, Map<String, Vector> randomVectors) throws Exception {
        Map<String, Vector> semanticVectors = new Object2ObjectOpenHashMap<>(dict.size());
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
                LOG.log(Level.INFO, "Working file: {0}", file.getName());
                extractor.extract(file);
                while (extractor.hasNext()) {
                    List<String> tokens = tokenizer.getTokens(extractor.next());
                    if (swFilter != null) {
                        swFilter.filter(tokens);
                    }
                    if (filter != null) {
                        filter.filter(tokens);
                    }
                    for (int i = 0; i < tokens.size(); i++) {
                        if (dict.containsKey(tokens.get(i))) {
                            Vector sv = semanticVectors.get(tokens.get(i));
                            if (sv == null) {
                                sv = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                                semanticVectors.put(tokens.get(i), sv);
                            }
                            int start = Math.max(0, i - winsize);
                            int end = Math.min(tokens.size() - 1, i + winsize);
                            for (int j = start; j <= end; j++) {
                                if (i != j && dict.containsKey(tokens.get(j))) {
                                    Vector ri = randomVectors.get(tokens.get(j));
                                    double f = dict.get(tokens.get(j)).doubleValue() / (double) totalOcc; //downsampling
                                    double p = 1;
                                    if (f > t) { //if word frequency is greater than the threshold, compute the probability of consider the word 
                                        p = Math.sqrt(t / f);
                                    }
                                    sv.superpose(ri, p, PermutationUtils.getShiftPermutation(VectorType.REAL, dimension, j - i));
                                    //sum not permutated vector
                                    sv.superpose(ri, p, null);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Vector v : semanticVectors.values()) {
            if (!v.isZeroVector()) {
                v.normalize();
            }
        }
        VectorStoreUtils.saveSpace(outputFile, semanticVectors, VectorType.REAL, dimension, seed);
    }

    /**
     * Build the co-occurrences matrix
     *
     * @param startingDir The corpus directory containing files with year
     * metadata
     * @throws Exception
     */
    public void process(File startingDir) throws Exception {
        LOG.log(Level.INFO, "Starting dir: {0}", startingDir.getAbsolutePath());
        LOG.log(Level.INFO, "Output dir: {0}", outputDir.getAbsolutePath());
        LOG.log(Level.INFO, "Vocabulary size: {0}", vocSize);
        LOG.log(Level.INFO, "Window size: {0}", winsize);
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
        LOG.log(Level.INFO, "Form year: {0}", minYear);
        LOG.log(Level.INFO, "To year: {0}", maxYear);
        LOG.log(Level.INFO, "Build dictionary...");
        Map<String, Integer> cmap = new Object2IntOpenHashMap<>();
        for (int k = minYear; k <= maxYear; k++) {
            buildDict(startingDir, cmap, k);
        }
        LOG.info("Sorting...");
        PriorityQueue<DictionaryEntry> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> e : cmap.entrySet()) {
            if (queue.size() <= vocSize) {
                queue.offer(new DictionaryEntry(e.getKey(), e.getValue()));
            } else {
                queue.offer(new DictionaryEntry(e.getKey(), e.getValue()));
                queue.poll();
            }
        }
        if (queue.size() > vocSize) {
            queue.poll();
        }
        cmap.clear();
        cmap = null;
        totalOcc = 0;
        Map<String, Vector> randomVectors = new Object2ObjectOpenHashMap();
        Map<String, Integer> dict = new Object2IntOpenHashMap<>(queue.size());
        Random random = new Random();
        for (DictionaryEntry de : queue) {
            dict.put(de.getWord(), de.getCounter());
            randomVectors.put(de.getWord(), VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
            totalOcc += dict.get(de.getWord());
        }
        System.gc();
        LOG.log(Level.INFO, "Vocabulary size: {0}", dict.size());
        LOG.log(Level.INFO, "Build semantic vectors...");
        for (int k = minYear; k <= maxYear; k++) {
            buildSemanticVectors(startingDir, new File(outputDir.getAbsolutePath() + "/sv_" + k + ".v"), k, dict, randomVectors);
        }
        LOG.log(Level.INFO, "Save random vectors...");
        VectorStoreUtils.saveSpace(new File(outputDir.getAbsolutePath() + "/random.v"), randomVectors, VectorType.REAL, dimension, seed);
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
                .addOption("k", true, "Load keyword list (optional)")
                .addOption("n", true, "Size of the vocabulary (optional, default 50000)")
                .addOption("dim", true, "The vector dimension (optional, default 500)")
                .addOption("seed", true, "The number of seeds (optional, default 10)")
                .addOption("th", true, "Threshold for downsampling frequent words (optinal, default 0.001)");
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
                            LOG.log(Level.WARNING, "No KeywordFinder constructor for {0}, use default constructor...", cmd.getOptionValue("t"));
                            tokenizer = (TriTokenizer) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).newInstance();
                        }
                    } else {
                        tokenizer = (TriTokenizer) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("t", "TriStandardTokenizer")).newInstance();
                    }
                    PositionalTri builder = new PositionalTri();
                    builder.setOutputDir(new File(cmd.getOptionValue("o")));
                    builder.setWinsize(Integer.parseInt(cmd.getOptionValue("w", "5")));
                    builder.setExtractor((IterableExtractor) classExtractor);
                    builder.setTokenizer(tokenizer);
                    if (cmd.hasOption("s")) {
                        LOG.info("Load stop word...");
                        builder.setSwFilter(new StopWordFilter(OccUtils.loadSet(new File(cmd.getOptionValue("s")))));
                    }
                    if (cmd.hasOption("f")) {
                        LOG.info("Load filter...");
                        Filter filter = (Filter) Class.forName("di.uniba.it.tri.tokenizer." + cmd.getOptionValue("f")).newInstance();
                        builder.setFilter(filter);
                    }
                    builder.setFilenameRegExp(cmd.getOptionValue("r", "^.+$"));
                    builder.setVocSize(Integer.parseInt(cmd.getOptionValue("n", "50000")));
                    builder.setDimension(Integer.parseInt(cmd.getOptionValue("dim", "300")));
                    builder.setSeed(Integer.parseInt(cmd.getOptionValue("seed", "10")));
                    builder.setT(Double.parseDouble(cmd.getOptionValue("th", "0.001")));
                    builder.process(new File(cmd.getOptionValue("c")));
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build positional temporal random indexing given the set of files with year metadata", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
