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

import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;

/**
 *
 * @author pierpaolo
 */
public class GbooksTRI {

    private static final Logger LOG = Logger.getLogger(GbooksTRI.class.getName());

    private int startYear = 1800;

    private int endYear = 2000;

    private int step = 10;

    private int dimension = 1000;

    private int seed = 10;

    private String getKeySpan(int year) {
        if (year >= startYear && year <= endYear) {
            int ni = (year - startYear) / step;
            return (ni * step + startYear) + "_" + ((ni + 1) * step + startYear - 1);
        } else {
            return null;
        }
    }

    private DataOutputStream getStream(String outputDirname, String keySpan) throws IOException {
        File file = new File(outputDirname + "/tri_" + keySpan);
        DataOutputStream outputStream;
        if (!file.exists()) {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            outputStream.writeUTF(VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed));
        } else {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
        }
        return outputStream;
    }

    public void build(String storageDirname, String outputDirname) throws IOException {
        //load DB
        File dbfile = new File(storageDirname + "/dbmap/gbmap");
        DB db = DBMaker.newFileDB(dbfile).cacheHardRefEnable().mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
        //dictionary
        HTreeMap<String, Integer> dict = db.get("dict");
        //co-occur info
        NavigableSet<Fun.Tuple2<Integer, CountEntry>> occSet = db.get("occ");
        Map<Integer, Vector> ri = new HashMap<>();
        Random random = new Random();
        int cw = 0;
        System.out.println();
        //build semantic vector
        LOG.log(Level.INFO, "Build Semantic Vectors...");
        Iterator<String> keyIt = dict.keySet().iterator();
        while (keyIt.hasNext()) {
            Map<String, Vector> tempSV = new HashMap<>();
            String currentKey = keyIt.next();
            Integer wordId = dict.get(currentKey);
            Iterable<CountEntry> occit = Fun.filter(occSet, wordId);
            for (CountEntry entry : occit) {
                if (entry.getYear() >= startYear && entry.getYear() <= endYear) {
                    String keySpan = getKeySpan(entry.getYear());
                    Vector v = tempSV.get(keySpan);
                    if (v == null) {
                        v = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                        tempSV.put(keySpan, v);
                    }
                    Vector riv = ri.get(entry.getWordId());
                    if (riv == null) {
                        riv = VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random);
                        ri.put(entry.getWordId(), riv);
                    }
                    v.superpose(riv, entry.getCount(), null);
                }
            }
            Set<String> spankeySet = tempSV.keySet();
            for (String spankey : spankeySet) {
                DataOutputStream stream = getStream(outputDirname, spankey);
                stream.writeUTF(currentKey);
                tempSV.get(spankey).writeToStream(stream);
                stream.close();
            }
            cw++;
            if (cw % 100 == 0) {
                System.out.print(".");
                if (cw % 10000 == 0) {
                    System.out.println("." + cw);
                }
            }
        }
        db.close();
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
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

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("o", true, "Output directory where spaces will be stored")
                .addOption("b", true, "Begin (year)")
                .addOption("e", true, "End (year)")
                .addOption("p", true, "Temporal step (optional, default 10)")
                .addOption("s", true, "Seed (optional, default 10)")
                .addOption("d", true, "Vector dimensions (optional, default 1000)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o") && cmd.hasOption("b") && cmd.hasOption("e")) {
                GbooksTRI gtri = new GbooksTRI();
                gtri.setStartYear(Integer.parseInt(cmd.getOptionValue("b")));
                gtri.setEndYear(Integer.parseInt(cmd.getOptionValue("e")));
                gtri.setDimension(Integer.parseInt(cmd.getOptionValue("d", "1000")));
                gtri.setSeed(Integer.parseInt(cmd.getOptionValue("s", "10")));
                gtri.setStep(Integer.parseInt(cmd.getOptionValue("p", "10")));
                try {
                    gtri.build(cmd.getOptionValue("i"), cmd.getOptionValue("o"));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build TRI spaces from Google Books pre-processed dataset", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}