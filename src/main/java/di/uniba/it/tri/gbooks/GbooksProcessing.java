/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.data.DictionaryEntry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.zip.GZIPInputStream;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 *
 * @author pierpaolo
 */
public class GbooksProcessing {

    private int vocSize = 100000;

    private int cacheSize = 100000;

    int minYear = Integer.MAX_VALUE;

    int maxYear = -Integer.MAX_VALUE;

    int dimension = 1000;

    int seed = 10;

    private String wordRegexpFilter = "[a-z]+";

    private DB db;

    private static final Logger LOG = Logger.getLogger(GbooksProcessing.class.getName());

    public GbooksProcessing(int dimension, int seed) {
        this.dimension = dimension;
        this.seed = seed;
    }

    public void init(File dbfile) {
        db = DBMaker.newFileDB(dbfile).cacheSize(cacheSize).mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
    }

    private GBLineResult processLine(String line) {
        String[] values = line.split("\t");
        String[] words = values[0].split(" ");
        String[] tokens = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            String[] split = words[i].split("_");
            if (split == null || split.length == 0) {
                split = new String[]{words[i]};
            }
            if (split[0].length() > 0) {
                tokens[i] = split[0].toLowerCase();
            } else {
                tokens[i] = words[i].toLowerCase();
            }
        }
        return new GBLineResult(tokens, Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }

    private List<DictionaryEntry> count(File dir) throws IOException {
        LOG.info("Start counting...");
        HTreeMap<String, Integer> counterMap = db.createHashMap("lex").make();
        minYear = Integer.MAX_VALUE;
        maxYear = -Integer.MAX_VALUE;
        long c = 0;
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("googlebooks-") && file.getName().endsWith(".gz")) {
                GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.ready()) {
                    String line = reader.readLine();
                    GBLineResult gbres = null;
                    try {
                        gbres = processLine(line);
                    } catch (Exception ex) {
                        System.err.println("Line error: " + line);
                    }
                    if (gbres != null) {
                        minYear = Math.min(minYear, gbres.getYear());
                        maxYear = Math.max(maxYear, gbres.getYear());
                        for (String ngram : gbres.getNgram()) {
                            if (ngram.matches(wordRegexpFilter)) {
                                Integer count = counterMap.get(ngram);
                                if (count == null) {
                                    counterMap.put(ngram, gbres.getCount());
                                } else {
                                    counterMap.put(ngram, count + gbres.getCount());
                                }
                            }
                        }
                    }
                    c++;
                    if (c % 1000000 == 0) {
                        LOG.log(Level.INFO, "count {0}", c);
                    }
                }
            }
        }
        LOG.log(Level.INFO, "Min year: {0}", minYear);
        LOG.log(Level.INFO, "Max year: {0}", maxYear);
        LOG.log(Level.INFO, "Build dictionary ({0})...", counterMap.size());
        //create dictionary
        List<DictionaryEntry> dict = new ArrayList<>();
        for (String key : counterMap.keySet()) {
            Integer counter = counterMap.get(key);
            dict.add(new DictionaryEntry(key, counter));
        }
        LOG.info("Sorting dictionary...");
        Collections.sort(dict);
        db.delete("lex");
        db.commit();
        db.compact();
        LOG.info("Return dictionary...");
        if (dict.size() > vocSize) {
            return dict.subList(0, vocSize);
        } else {
            return dict;
        }
    }

    private short[] getRandomVector(int dimension, int seed, Random random) {
        short[] v = new short[seed];
        boolean[] occupiedPositions = new boolean[dimension];
        int testPlace, entryCount = 0;

        // Put in +1 entries.
        while (entryCount < seed / 2) {
            testPlace = random.nextInt(dimension);
            if (!occupiedPositions[testPlace]) {
                occupiedPositions[testPlace] = true;
                v[entryCount]
                        = new Integer(testPlace + 1).shortValue();
                entryCount++;
            }
        }

        // Put in -1 entries.
        while (entryCount < seed) {
            testPlace = random.nextInt(dimension);
            if (!occupiedPositions[testPlace]) {
                occupiedPositions[testPlace] = true;
                v[entryCount]
                        = new Integer((1 + testPlace) * -1).shortValue();
                entryCount++;
            }
        }
        return v;
    }

    private void buildSpaces(File dir, List<DictionaryEntry> dict) throws IOException {
        LOG.info("Start spaces building...");
        Map<String, short[]> elementalSpace = new HashMap<>();
        //create random vectors space
        LOG.info("Building elemental vectors...");
        Random random = new Random();
        for (DictionaryEntry entry : dict) {
            elementalSpace.put(entry.getWord(), getRandomVector(dimension, seed, random));
        }
        dict.clear();
        dict = null;
        System.gc();
        long c = 0;
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("googlebooks-") && file.getName().endsWith(".gz")) {
                GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.ready()) {
                    String line = reader.readLine();
                    GBLineResult gbres = null;
                    try {
                        gbres = processLine(line);
                    } catch (Exception ex) {
                        System.err.println("Line error: " + line);
                    }
                    if (gbres != null) {
                        HTreeMap<String, float[]> svMap = db.createHashMap("SV_" + gbres.getYear()).makeOrGet();
                        for (int i = 0; i < gbres.getNgram().length; i++) {
                            if (elementalSpace.containsKey(gbres.getNgram()[i])) {
                                float[] sv = svMap.get(gbres.getNgram()[i]);
                                if (sv == null) {
                                    sv = new float[dimension];
                                    svMap.put(gbres.getNgram()[i], sv);
                                }
                                for (int j = 0; j < gbres.getNgram().length; j++) {
                                    if (i != j) {
                                        short[] idx = elementalSpace.get(gbres.getNgram()[j]);
                                        if (idx != null) {
                                            for (int k = 0; k < idx.length; k++) {
                                                if (idx[k] > 0) {
                                                    sv[idx[k] - 1]++;
                                                } else {
                                                    sv[-idx[k] - 1]--;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c++;
                    if (c % 1000000 == 0) {
                        LOG.log(Level.INFO, "count {0}", c);
                    }
                }
                db.commit();
            }
        }
        db.close();
    }

    public void process(File startingDir) throws IOException {
        List<DictionaryEntry> count = count(startingDir);
        buildSpaces(startingDir, count);
    }

    public int getVocSize() {
        return vocSize;
    }

    public void setVocSize(int vocSize) {
        this.vocSize = vocSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public String getWordRegexpFilter() {
        return wordRegexpFilter;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setWordRegexpFilter(String wordRegexpFilter) {
        this.wordRegexpFilter = wordRegexpFilter;
    }

    private class GBLineResult {

        private String[] ngram;

        private int year;

        private int count;

        public GBLineResult(String[] ngram, int year, int count) {
            this.ngram = ngram;
            this.year = year;
            this.count = count;
        }

        public String[] getNgram() {
            return ngram;
        }

        public void setNgram(String[] ngram) {
            this.ngram = ngram;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }

}
