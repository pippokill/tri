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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
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
public class GbooksProcessingFromValidFile {

    private int vocSize = 100000;

    private int cacheSize = 1000000;

    int minYear = Integer.MAX_VALUE;

    int maxYear = -Integer.MAX_VALUE;

    private String wordRegexpFilter = "[a-z]+";

    private final File validGbookFile;

    private DB db;

    private final String dirname;

    private static final Logger LOG = Logger.getLogger(GbooksProcessingFromValidFile.class.getName());

    public GbooksProcessingFromValidFile(String dirname, File validGbookFile) {
        this.dirname = dirname;
        this.validGbookFile = validGbookFile;
    }

    public void init() {
        try {
            File dbfile = new File(dirname + "/dbmap/");
            dbfile.mkdir();
            dbfile = new File(dirname + "/dbmap/gbmap");
            db = DBMaker.newFileDB(dbfile).cacheSize(cacheSize).mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error to init Gbooks processing", ex);
        }
    }

    private List<DictionaryEntry> count() throws IOException {
        LOG.info("Start counting...");
        File dictFile = new File(dirname + "/googlebooks-valid.dict");
        if (dictFile.exists()) {
            LOG.info("Previous dict file found, loading...");
            List<DictionaryEntry> dict = new ArrayList<>();
            BufferedReader dictReader = new BufferedReader(new FileReader(dictFile));
            while (dictReader.ready()) {
                String[] split = dictReader.readLine().split("\t");
                if (split.length == 2) {
                    dict.add(new DictionaryEntry(split[0], Integer.parseInt(split[1])));
                }
            }
            dictReader.close();
            LOG.log(Level.INFO, "Loaded {0} dict entries", dict.size());
            return dict;
        }
        HTreeMap<String, Integer> counterMap = db.createHashMap("dict").make();
        minYear = Integer.MAX_VALUE;
        maxYear = -Integer.MAX_VALUE;
        long c = 0;
        GZIPInputStream is = new GZIPInputStream(new FileInputStream(validGbookFile));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (reader.ready()) {
            String line = reader.readLine();
            //word1 word2 year count
            String[] split = line.split("\t");
            int year = Integer.parseInt(split[2]);
            int count = Integer.parseInt(split[3]);
            minYear = Math.min(minYear, year);
            maxYear = Math.max(maxYear, year);
            Integer v1 = counterMap.get(split[0]);
            if (v1 == null) {
                counterMap.put(split[0], 1);
            } else {
                counterMap.put(split[0], v1 + count);
            }
            Integer v2 = counterMap.get(split[1]);
            if (v2 == null) {
                counterMap.put(split[1], 1);
            } else {
                counterMap.put(split[1], v2 + count);
            }
            c++;
            if (c % 10000000 == 0) {
                LOG.log(Level.INFO, "count {0}", c);
            }
        }
        reader.close();
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

    private CountEntry find(List<CountEntry> list, int wordid, int year) {
        for (CountEntry entry : list) {
            if (entry.getWordId() == wordid && entry.getYear() == year) {
                return entry;
            }
        }
        return null;
    }

    private void store(List<DictionaryEntry> dict) throws IOException, SQLException {
        LOG.info("Store lex...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(dirname + "/googlebooks-valid.dict"));
        //clean previous map
        db.delete("lex");
        db.delete("counting");
        db.commit();
        db.compact();
        HTreeMap<String, Integer> lex = db.createHashMap("lex").make();
        HTreeMap<Integer, List<CountEntry>> counting = db.createHashMap("counting").make();
        int lid = 0;
        for (DictionaryEntry dictEntry : dict) {
            lex.put(dictEntry.getWord(), lid);
            lid++;
            writer.append(dictEntry.getWord()).append("\t").append(String.valueOf(dictEntry.getCounter()));
            writer.newLine();
        }
        writer.close();
        dict.clear();
        dict = null;
        System.gc();
        LOG.info("Store occ...");
        long c = 0;
        GZIPInputStream is = new GZIPInputStream(new FileInputStream(validGbookFile));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] split = line.split("\t");
            Integer lid1 = lex.get(split[0]);
            Integer lid2 = lex.get(split[1]);
            int year = Integer.parseInt(split[2]);
            if (lid1 != null && lid2 != null) {
                List<CountEntry> list = counting.get(lid1);
                if (list == null) {
                    list = new ArrayList<>();
                    counting.put(lid1, list);
                }
                list.add(new CountEntry(lid2, year, Integer.parseInt(split[3])));
            }
            c++;
            if (c % 10000000 == 0) {
                LOG.log(Level.INFO, "count {0}", c);
                db.commit();
            }
        }
        reader.close();
        db.commit();
        db.close();
    }

    public void process() throws IOException, SQLException {
        List<DictionaryEntry> dict = count();
        store(dict);
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "The valid Google Books 2-grams file")
                .addOption("t", true, "Output directory where processed data will be stored")
                .addOption("c", true, "The cache size used by MapDB in elements (optional, default 100000)")
                .addOption("h", true, "The cache size used by H2 storage in Kbyte (optional, default 512MB)")
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
                    GbooksProcessingFromValidFile gbp = new GbooksProcessingFromValidFile(cmd.getOptionValue("t"), new File(cmd.getOptionValue("i")));
                    gbp.setCacheSize(Integer.parseInt(cmd.getOptionValue("c", "1000000")));
                    gbp.setVocSize(Integer.parseInt(cmd.getOptionValue("v", "50000")));
                    gbp.setWordRegexpFilter(cmd.getOptionValue("r", "[a-z]+"));
                    gbp.init();
                    gbp.process();
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

}
