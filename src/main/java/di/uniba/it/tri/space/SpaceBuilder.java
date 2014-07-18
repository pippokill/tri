/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

    private int maxNoAplhaChar = 1;

    private final Pattern noaPattern = Pattern.compile("^.+_+$");

    public SpaceBuilder(File startingDir) {
        this.startingDir = startingDir;
    }

    public SpaceBuilder(File startingDir, int dimension) {
        this.startingDir = startingDir;
        this.dimension = dimension;
    }

    public SpaceBuilder(File startingDir, int dimension, int seed) {
        this.startingDir = startingDir;
        this.dimension = dimension;
        this.seed = seed;
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

    public int getMaxNoAplhaChar() {
        return maxNoAplhaChar;
    }

    public void setMaxNoAplhaChar(int maxNoAplhaChar) {
        this.maxNoAplhaChar = maxNoAplhaChar;
    }

    public File getStartingDir() {
        return startingDir;
    }

    public void setStartingDir(File startingDir) {
        this.startingDir = startingDir;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

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
            BufferedReader reader = new BufferedReader(new FileReader(file));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/" + file.getName() + ".vectors")));
            String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed);
            outputStream.writeUTF(header);
            String[] split;
            while (reader.ready()) {
                split = reader.readLine().split("\t");
                String token = cleanToken(split[0]);
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
                        String word = cleanToken(split[i]);
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
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                String[] split = reader.readLine().split("\t");
                String token = cleanToken(split[0]);
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

    /**
     * startingDir outputDir dimension seed dictionarySize
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 5) {
            try {
                SpaceBuilder builder = new SpaceBuilder(new File(args[0]));
                builder.setDimension(Integer.parseInt(args[2]));
                builder.setSeed(Integer.parseInt(args[3]));
                builder.setSize(Integer.parseInt(args[4]));
                builder.build(new File(args[1]));
            } catch (IOException | NumberFormatException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            logger.warning("No valid arguments");
        }
    }

    private String cleanToken(String token) {
        //token ends with a sequence of '_'?
        token = token.replaceAll("^.+_+$", "");
        //token starts with a sequence of '_'?
        token = token.replaceAll("^_+.+$", "");
        if (token.length() == 0) {
            return null;
        }
        if (noaPattern.matcher(token).groupCount() > maxNoAplhaChar) {
            return null;
        } else {
            return token;
        }
    }

}
