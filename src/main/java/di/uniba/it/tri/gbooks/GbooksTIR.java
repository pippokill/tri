/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

/**
 *
 * @author pierpaolo
 */
public class GbooksTIR {

    private int startYear = 1800;

    private int endYear = 2000;

    private int step = 10;

    private int cacheSize = 10000;

    private int dimension = 2000;

    private int seed = 4;

    private String getKeySpan(int year) {
        if (year >= startYear && year <= endYear) {
            int ni = year - startYear / step;
            return (ni * step + startYear) + "_" + (ni * (step + 1) + startYear);
        } else {
            return null;
        }
    }

    public void build(String storageDirname, String outputDirname) throws IOException {
        //load DB
        File dbfile = new File(storageDirname + "/dbmap/gbmap");
        DB db = DBMaker.newFileDB(dbfile).cacheSize(cacheSize).mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
        HTreeMap<String, Integer> lex = db.get("lex");
        HTreeMap<Integer, List<CountEntry>> counting = db.get("counting");
        //build random vector
        Map<String, Vector> ri = new HashMap<>();
        Random random = new Random();
        Iterator<String> keyIt = lex.keySet().iterator();
        while (keyIt.hasNext()) {
            ri.put(keyIt.next(), VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
        }
        //build semantic vector
        keyIt = lex.keySet().iterator();
        while (keyIt.hasNext()) {
            Map<String, Vector> tempSV = new HashMap<>();
            String currentKey = keyIt.next();
            List<CountEntry> currentList = counting.get(lex.get(currentKey));
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
