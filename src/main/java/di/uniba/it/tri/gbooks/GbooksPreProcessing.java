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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;

/**
 *
 * @author pierpaolo
 */
public class GbooksPreProcessing {

    int minYear = Integer.MAX_VALUE;

    int maxYear = -Integer.MAX_VALUE;

    private String wordRegexpFilter = "[a-z]+";

    private DB db;

    private final String dirname;

    private static final Logger LOG = Logger.getLogger(GbooksPreProcessing.class.getName());

    private static final int MAX_LENGTH_WORD = 128;

    private static final int FILE_LIMIT = Integer.MAX_VALUE;

    private static final long MAX_FILE_SIZE = Long.MAX_VALUE;

    public GbooksPreProcessing(String dirname) {
        this.dirname = dirname;
    }

    public void init() {
        File dbfile = new File(dirname + "/dbmap/");
        dbfile.mkdir();
        dbfile = new File(dirname + "/dbmap/gbmap");
        db = DBMaker.newFileDB(dbfile).cacheHardRefEnable().mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
    }

    private GBLineResult processLine(String line) {
        String[] values = line.split("\t");
        String[] words = values[0].split(" ");
        List<String> tokens = new ArrayList<>(words.length);
        for (String word : words) {
            String[] split = word.split("_");
            if (split.length > 0) {
                tokens.add(split[0].toLowerCase());
            }
        }
        return new GBLineResult(tokens.toArray(new String[tokens.size()]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }

    private void store(File dir) throws IOException {
        LOG.info("Start counting...");
        HTreeMap<String, Integer> dictMap = db.createHashMap("dict").make();
        NavigableSet<Fun.Tuple2<Integer, CountEntry>> occSet = db.createTreeSet("occ").serializer(BTreeKeySerializer.TUPLE2).make();
        minYear = Integer.MAX_VALUE;
        maxYear = -Integer.MAX_VALUE;
        long c = 0;
        int wordId = 0;
        int fileCount = 0;
        File[] listFiles = dir.listFiles();
        for (int k = 0; k < listFiles.length && fileCount < FILE_LIMIT; k++) {
            if (listFiles[k].getName().startsWith("googlebooks-") && listFiles[k].getName().endsWith(".gz") && listFiles[k].length() < MAX_FILE_SIZE) {
                LOG.log(Level.INFO, "Working on file {0}", listFiles[k].getAbsolutePath());
                GZIPInputStream is = new GZIPInputStream(new FileInputStream(listFiles[k]));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.ready()) {
                    String line = reader.readLine();
                    GBLineResult gbres = null;
                    try {
                        gbres = processLine(line);
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Line error: {0}", line);
                    }
                    if (gbres != null) {
                        minYear = Math.min(minYear, gbres.getYear());
                        maxYear = Math.max(maxYear, gbres.getYear());
                        //generate id
                        for (String ngram : gbres.getNgram()) {
                            if (ngram.matches(wordRegexpFilter) && ngram.length() < MAX_LENGTH_WORD) {
                                Integer mid = dictMap.get(ngram);
                                if (mid == null) {
                                    dictMap.put(ngram, wordId);
                                    wordId++;
                                }
                            }
                        }
                        for (int i = 0; i < gbres.getNgram().length; i++) {
                            Integer id1 = dictMap.get(gbres.getNgram()[i]);
                            if (id1 != null) {
                                for (int j = 0; j < gbres.getNgram().length; j++) {
                                    if (i != j) {
                                        Integer id2 = dictMap.get(gbres.getNgram()[j]);
                                        if (id2 != null) {
                                            occSet.add(Fun.t2(id1, new CountEntry(id2, gbres.getYear(), gbres.getCount())));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c++;
                    if (c % 10000000 == 0) {
                        LOG.log(Level.INFO, "Processed lines: {0}", c);
                    }
                }
                LOG.log(Level.INFO, "Close file {0} ({1})", new Object[]{listFiles[k].getAbsolutePath(), c});
                reader.close();
                fileCount++;
            }
        }
        LOG.log(Level.INFO, "Min year: {0}", minYear);
        LOG.log(Level.INFO, "Max year: {0}", maxYear);
        LOG.info("Closing storage...");
        db.commit();
        db.close();
    }

    public void process(File startingDir) throws IOException, SQLException {
        store(startingDir);
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "The corpus directory containing Google Books 2-grams dataset")
                .addOption("t", true, "Output directory where processed data will be stored")
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
                    GbooksPreProcessing gbp = new GbooksPreProcessing(cmd.getOptionValue("t"));
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

    public String getWordRegexpFilter() {
        return wordRegexpFilter;
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
