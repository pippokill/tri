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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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

    private static final Logger logger = Logger.getLogger(SpaceBuilder.class.getName());

    private int dimension = 200;

    private int seed = 10;

    private File startingDir;

    private int size = 100000;

    private final Pattern noaPattern = Pattern.compile("^.+_+$");

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

    /**
     *
     * @param outputDir
     * @throws IOException
     */
    public void build(File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        List<DictionaryEntry> dict = buildDictionary(startingDir, size);
        Map<String, Vector> elementalSpace = new HashMap<>();
        //create random vectors space
        logger.info("Building elemental vectors...");
        Random random = new Random();
        for (DictionaryEntry entry : dict) {
            elementalSpace.put(entry.getWord(), VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
        }
        dict.clear();
        dict = null;
        logger.log(Level.INFO, "Building spaces: {0}", startingDir.getAbsolutePath());
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            logger.log(Level.INFO, "Space: {0}", file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/" + file.getName() + ".vectors")));
            String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed);
            outputStream.writeUTF(header);
            String[] split;
            while (reader.ready()) {
                split = reader.readLine().split("\t");
                String token = split[0];
                if (token != null && elementalSpace.containsKey(token)) {
                    Vector v = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                    int i = 1;
                    double totOcc = 0;
                    while (i < split.length) {
                        i++;
                        double weight = Double.parseDouble(split[i]);
                        i++;
                        totOcc += weight;
                    }
                    i = 1;
                    double t = 1 / totOcc;
                    while (i < split.length) {
                        String word = split[i];
                        if (word != null) {
                            i++;
                            double weight = Math.sqrt(t / (Double.parseDouble(split[i]) / totOcc));
                            i++;
                            Vector rv = elementalSpace.get(word);
                            if (rv != null) {
                                v.superpose(rv, weight, null);
                            }
                        } else {
                            i = i + 2;
                        }
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
        logger.log(Level.INFO, "Save elemental vectors in dir: {0}", outputDir.getAbsolutePath());
        VectorStoreUtils.saveSpace(new File(outputDir.getAbsolutePath() + "/vectors.elemental"), elementalSpace, VectorType.REAL, dimension, seed);
    }

    private List<DictionaryEntry> buildDictionary(File startingDir, int maxSize) throws IOException {
        logger.log(Level.INFO, "Building dictionary: {0}", startingDir.getAbsolutePath());
        Map<String, DictionaryEntry> dict = new HashMap<>();
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            logger.log(Level.INFO, "Working on file: {0}", file.getName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            while (reader.ready()) {
                String[] split = reader.readLine().split("\t");
                String token = split[0];
                if (token != null) {
                    DictionaryEntry entry = dict.get(split[0]);
                    if (entry == null) {
                        entry = new DictionaryEntry(split[0], 1);
                        dict.put(entry.getWord(), entry);
                    } else {
                        entry.incrementCounter();
                    }
                }
            }
            reader.close();
        }
        logger.info("Sorting...");
        List<DictionaryEntry> list = new ArrayList<>(dict.values());
        logger.log(Level.INFO, "Total elements: {0}", list.size());
        Collections.sort(list);
        if (list.size() > maxSize) {
            list = list.subList(0, maxSize);
        }
        logger.log(Level.INFO, "Dictionary size: {0}", list.size());
        return list;
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("c", true, "The directory containing the co-occurrences matrices")
                .addOption("o", true, "Output directory where WordSpaces will be stored")
                .addOption("d", true, "The vector dimension (optional, defaults 300)")
                .addOption("s", true, "The number of seeds (optional, defaults 10)")
                .addOption("v", true, "The dictionary size (optional, defaults 100.000)");
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
                    builder.build(new File(cmd.getOptionValue("o")));
                } catch (IOException | NumberFormatException ex) {
                    logger.log(Level.SEVERE, null, ex);
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
