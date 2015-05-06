/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    private int cacheSize = 10000;

    private int dimension = 5000;

    private int seed = 10;

    private String getKeySpan(int year) {
        if (year >= startYear && year <= endYear) {
            int ni = (year - startYear) / step;
            return (ni * step + startYear) + "_" + (ni * (step + 1) + startYear - 1);
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
        DB db = DBMaker.newFileDB(dbfile).cacheSize(cacheSize).mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
        //dictionary
        HTreeMap<String, Integer> dict = db.get("dict");
        //co-occur info
        NavigableSet<Fun.Tuple2<Integer, CountEntry>> occSet = db.get("occ");
        //build random vector using word id
        Map<Integer, Vector> ri = new HashMap<>();
        Random random = new Random();
        Iterator<Integer> idIt = dict.values().iterator();
        while (idIt.hasNext()) {
            ri.put(idIt.next(), VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
        }
        LOG.log(Level.INFO, "Total words: {0}", dict.size());
        int cw = 0;
        System.out.println();
        //build semantic vector
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
                    if (riv != null) {
                        v.superpose(riv, entry.getCount(), null);
                    }
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

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
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
                .addOption("h", true, "The cache size (optional, default 10000 elements)")
                .addOption("s", true, "Seed (optional, default 4)")
                .addOption("d", true, "Vector dimensions (optional, default 5000)");
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
                gtri.setDimension(Integer.parseInt(cmd.getOptionValue("d", "5000")));
                gtri.setSeed(Integer.parseInt(cmd.getOptionValue("s", "4")));
                gtri.setCacheSize(Integer.parseInt(cmd.getOptionValue("h", "10000")));
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
