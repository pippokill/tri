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
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.data.DictionaryEntry;
import di.uniba.it.tri.data.h2.H2Storage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private int h2CacheSize = 1024 * 512;

    int minYear = Integer.MAX_VALUE;

    int maxYear = -Integer.MAX_VALUE;

    private String wordRegexpFilter = "[a-z]+";

    private DB db;

    private H2Storage h2s;

    private final String dirname;

    private static final Logger LOG = Logger.getLogger(GbooksProcessing.class.getName());

    private static final int MAX_LENGTH_WORD = 128;

    public GbooksProcessing(String dirname) {
        this.dirname = dirname;
    }

    public void init() {
        try {
            File dbfile = new File(dirname + "/dbmap/");
            dbfile.mkdir();
            dbfile = new File(dirname + "/dbmap/gbmap");
            db = DBMaker.newFileDB(dbfile).cacheSize(cacheSize).mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
            h2s = new H2Storage(dirname + "/h2/gb.db", h2CacheSize);
        } catch (IOException | SQLException ex) {
            LOG.log(Level.SEVERE, "Error to init Gbooks processing", ex);
        }
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
        HTreeMap<String, Integer> counterMap = db.createHashMap("dict").make();
        minYear = Integer.MAX_VALUE;
        maxYear = -Integer.MAX_VALUE;
        long c = 0;
        GZIPOutputStream gzout = new GZIPOutputStream(new FileOutputStream(dirname + "googlebooks-valid.gz"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzout));
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("googlebooks-") && file.getName().endsWith(".gz")) {
                LOG.log(Level.INFO, "Count file {0}", file.getAbsolutePath());
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
                        for (int i = 0; i < gbres.getNgram().length; i++) {
                            if (gbres.getNgram()[i].matches(wordRegexpFilter) && gbres.getNgram()[i].length() < MAX_LENGTH_WORD) {
                                Integer count = counterMap.get(gbres.getNgram()[i]);
                                if (count == null) {
                                    counterMap.put(gbres.getNgram()[i], gbres.getCount());
                                } else {
                                    counterMap.put(gbres.getNgram()[i], count + gbres.getCount());
                                }
                            }
                            for (int j = 0; j < gbres.getNgram().length; j++) {
                                if (i != j) {
                                    writer.append(gbres.getNgram()[i]).append("\t").append(gbres.getNgram()[j]).append("\t")
                                            .append(String.valueOf(gbres.getYear())).append("\t").append(String.valueOf(gbres.getCount()));
                                    writer.newLine();
                                }
                            }
                        }
                    }
                    c++;
                }
                LOG.log(Level.INFO, "Close file {0} ({1})", new Object[]{file.getAbsolutePath(), c});
                reader.close();
            }
        }
        writer.close();
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
        db.delete("dict");
        db.commit();
        db.compact();
        LOG.info("Return dictionary...");
        if (dict.size() > vocSize) {
            return dict.subList(0, vocSize);
        } else {
            return dict;
        }
    }

    private void store(List<DictionaryEntry> dict) throws IOException, SQLException {
        LOG.info("Store lex...");
        HTreeMap<String, Integer> lex = db.createHashMap("lex").make();
        int lid = 0;
        for (DictionaryEntry dictEntry : dict) {
            h2s.addLex(lid, dictEntry.getWord(), dictEntry.getCounter());
            lex.put(dictEntry.getWord(), lid);
            lid++;

        }
        dict.clear();
        dict = null;
        System.gc();
        LOG.info("Store occ...");
        long c = 0;
        GZIPInputStream is = new GZIPInputStream(new FileInputStream(dirname + "googlebooks-valid.gz"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] split = line.split("\t");
            Integer lid1 = lex.get(split[0]);
            Integer lid2 = lex.get(split[1]);
            if (lid1 != null && lid2 != null) {
                h2s.addOcc(lid1, lid2, Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            }
            c++;
            if (c % 10000000 == 0) {
                LOG.log(Level.INFO, "count {0}", c);
            }
        }
        reader.close();
        db.commit();
        db.close();
        h2s.commit();
        h2s.close();
    }

    public void process(File startingDir) throws IOException, SQLException {
        List<DictionaryEntry> dict = count(startingDir);
        store(dict);
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "The corpus directory containing Google Books 2-grams dataset")
                .addOption("t", true, "Output directory where processed data will be stored")
                .addOption("c", true, "The cache size used by MapDB in elements (optional, default 100000)")
                .addOption("h", true, "The cache size used by H2 storage in Kbyte (optional, default 32MB)")
                .addOption("v", true, "The vocabulary size (optional, default 50000")
                .addOption("r", true, "Regular expression used to filter token (optional, default [a-z]+");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("t")) {
                try {
                    GbooksProcessing gbp = new GbooksProcessing(cmd.getOptionValue("t"));
                    gbp.setCacheSize(Integer.parseInt(cmd.getOptionValue("c", "100000")));
                    gbp.setH2CacheSize(Integer.parseInt(cmd.getOptionValue("h", "32768")));
                    gbp.setVocSize(Integer.parseInt(cmd.getOptionValue("v", "50000")));
                    gbp.setWordRegexpFilter(cmd.getOptionValue("r", "[a-z]+"));
                    gbp.init();
                    gbp.process(new File(cmd.getOptionValue("i")));
                } catch (IOException | SQLException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Process Google Books 2-grams dataset", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
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

    public int getH2CacheSize() {
        return h2CacheSize;
    }

    public void setH2CacheSize(int h2CacheSize) {
        this.h2CacheSize = h2CacheSize;
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
