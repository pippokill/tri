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
package di.uniba.it.tri.space;

import di.uniba.it.tri.data.DictionaryEntry;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
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
public class SpaceBuilder {

    private static final Logger LOG = Logger.getLogger(SpaceBuilder.class.getName());

    private int dimension = 200;

    private int seed = 10;

    private File startingDir;

    private int size = 100000;

    private boolean self = false;

    private long totalOcc = 0;

    private boolean idf = false;

    private Map<String, Double> idfMap;

    /**
     *
     * @param startingDir
     */
    public SpaceBuilder(File startingDir) {
        this.startingDir = startingDir;
    }

    /**
     *
     * @param startingDir
     * @param dimension
     */
    public SpaceBuilder(File startingDir, int dimension) {
        this.startingDir = startingDir;
        this.dimension = dimension;
    }

    /**
     *
     * @param startingDir
     * @param dimension
     * @param seed
     */
    public SpaceBuilder(File startingDir, int dimension, int seed) {
        this.startingDir = startingDir;
        this.dimension = dimension;
        this.seed = seed;
    }

    public boolean isIdf() {
        return idf;
    }

    public void setIdf(boolean idf) {
        this.idf = idf;
    }

    /**
     *
     * @return
     */
    public int getDimension() {
        return dimension;
    }

    /**
     *
     * @param dimension
     */
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    /**
     *
     * @return
     */
    public int getSeed() {
        return seed;
    }

    /**
     *
     * @param seed
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     *
     * @return
     */
    public File getStartingDir() {
        return startingDir;
    }

    /**
     *
     * @param startingDir
     */
    public void setStartingDir(File startingDir) {
        this.startingDir = startingDir;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    private double idf(String word, double wordOcc) {
        Double idf = idfMap.get(word);
        if (idf == null) {
            idf = Math.log((double) totalOcc / wordOcc) / Math.log(2);
            idfMap.put(word, idf);
        }
        return idf;
    }

    /**
     *
     * @param outputDir
     * @throws IOException
     */
    public void build(File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        Map<String, Integer> dict = buildDictionary(startingDir, size);
        LOG.log(Level.INFO, "Dictionary size {0}", dict.size());
        LOG.log(Level.INFO, "Use self random vector: {0}", self);
        LOG.log(Level.INFO, "IDF score: {0}", idf);
        Map<String, Vector> elementalSpace = new HashMap<>();
        //create random vectors space
        LOG.info("Building elemental vectors...");
        totalOcc = 0;
        Random random = new Random();
        for (String word : dict.keySet()) {
            elementalSpace.put(word, VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
            //compute total occurrences taking into account the dictionary
            totalOcc += dict.get(word);
        }
        LOG.log(Level.INFO, "Total occurrences {0}", totalOcc);
        LOG.log(Level.INFO, "Building spaces: {0}", startingDir.getAbsolutePath());
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            //init idf for each year
            idfMap = new HashMap<>();
            LOG.log(Level.INFO, "Space: {0}", file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/" + file.getName() + ".vectors")));
            String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed);
            outputStream.writeUTF(header);
            String[] split;
            while (reader.ready()) {
                split = reader.readLine().split("\t");
                String token = split[0];
                if (elementalSpace.containsKey(token)) {
                    Vector v;
                    if (self) {
                        v = elementalSpace.get(token).copy();
                    } else {
                        v = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                    }
                    int i = 1;
                    while (i < split.length) {
                        String word = split[i];
                        Vector ev = elementalSpace.get(word);
                        if (ev != null) {
                            double w = Integer.parseInt(split[i + 1]);
                            if (idf) {
                                w = w * idf(word, dict.get(word).doubleValue());
                            }
                            v.superpose(ev, w, null);
                        }
                        i = i + 2;
                    }
                    if (!v.isZeroVector()) {
                        v.normalize();
                        outputStream.writeUTF(token);
                        v.writeToStream(outputStream);
                    }
                }
            }
            reader.close();
            outputStream.close();
        }
        LOG.log(Level.INFO, "Save elemental vectors in dir: {0}", outputDir.getAbsolutePath());
        VectorStoreUtils.saveSpace(new File(outputDir.getAbsolutePath() + "/vectors.elemental"), elementalSpace, VectorType.REAL, dimension, seed);
    }

    private Map<String, Integer> buildDictionary(File startingDir, int maxSize) throws IOException {
        LOG.log(Level.INFO, "Building dictionary: {0}", startingDir.getAbsolutePath());
        Map<String, Integer> cmap = new HashMap<>();
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            LOG.log(Level.INFO, "Working on file: {0}", file.getName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            while (reader.ready()) {
                String[] split = reader.readLine().split("\t");
                String token = split[0];
                int count = 0;
                for (int i = 2; i < split.length; i = i + 2) {
                    count += Integer.parseInt(split[i]);
                }
                Integer c = cmap.get(token);
                if (c == null) {
                    cmap.put(token, count);
                } else {
                    cmap.put(token, c + count);
                }
            }
            reader.close();
        }
        LOG.info("Sorting...");
        PriorityQueue<DictionaryEntry> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> e : cmap.entrySet()) {
            if (queue.size() <= maxSize) {
                queue.offer(new DictionaryEntry(e.getKey(), e.getValue()));
            } else {
                queue.offer(new DictionaryEntry(e.getKey(), e.getValue()));
                queue.poll();
            }
        }
        queue.poll();
        cmap.clear();
        cmap = null;
        Map<String, Integer> dict = new HashMap<>(queue.size());
        for (DictionaryEntry de : queue) {
            dict.put(de.getWord(), de.getCounter());
        }
        return dict;
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("c", true, "The directory containing the co-occurrences matrices")
                .addOption("o", true, "Output directory where WordSpaces will be stored")
                .addOption("d", true, "The vector dimension (optional, defaults 300)")
                .addOption("s", true, "The number of seeds (optional, defaults 10)")
                .addOption("v", true, "The dictionary size (optional, defaults 100000)")
                .addOption("idf", true, "Enable IDF (optinal, defaults false)")
                .addOption("self", true, "Inizialize using random vector (optinal, defaults false)");
    }

    /**
     * Build WordSpace using Temporal Random Indexing
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("c") && cmd.hasOption("o")) {
                try {
                    SpaceBuilder builder = new SpaceBuilder(new File(cmd.getOptionValue("c")));
                    builder.setDimension(Integer.parseInt(cmd.getOptionValue("d", "300")));
                    builder.setSeed(Integer.parseInt(cmd.getOptionValue("s", "10")));
                    builder.setSize(Integer.parseInt(cmd.getOptionValue("v", "100000")));
                    builder.setIdf(Boolean.parseBoolean(cmd.getOptionValue("idf", "false")));
                    builder.setSelf(Boolean.parseBoolean(cmd.getOptionValue("self", "false")));
                    builder.build(new File(cmd.getOptionValue("o")));
                } catch (IOException | NumberFormatException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build WordSpace using Temporal Random Indexing", options, true);
            }
        } catch (ParseException ex) {
            Logger.getLogger(SpaceBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
