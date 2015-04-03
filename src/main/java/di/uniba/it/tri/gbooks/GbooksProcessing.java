/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class GbooksProcessing {

    private int vocSize = 100000;

    private int minOcc = 20;

    private BiMap<String, Integer> lexMap;

    private static final String WORD_REGEXP = "[a-z]+";

    public GbooksProcessing() {
    }

    public GbooksProcessing(int vocSize, int minOcc) {
        this.vocSize = vocSize;
        this.minOcc = minOcc;
    }

    private GBLineResult processLine(String line) {
        String[] values = line.split("\t");
        String[] words = values[0].split(" ");
        String[] tokens = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            String[] split = words[i].split("_");
            tokens[i] = split[0].toLowerCase();
        }
        return new GBLineResult(tokens, Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }

    private void count(File dir) throws IOException {
        lexMap = HashBiMap.create(vocSize);
        Map<Integer, Integer> counter = new HashMap<>();
        int minYear = Integer.MAX_VALUE;
        int maxYear = -Integer.MAX_VALUE;
        int idk = 0;
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("googlebooks-") && file.getName().endsWith(".gz")) {
                GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.ready()) {
                    String line = reader.readLine();
                    GBLineResult gbres = processLine(line);
                    minYear = Math.min(minYear, gbres.getYear());
                    maxYear = Math.max(maxYear, gbres.getYear());
                    for (String ngram : gbres.getNgram()) {
                        Integer sid = lexMap.get(ngram);
                        if (sid == null) {
                            sid = idk;
                            lexMap.put(ngram, sid);
                            idk++;
                        }
                        Integer c = counter.get(sid);
                        if (c == null) {
                            counter.put(sid, gbres.getCount());
                        } else {
                            counter.put(sid, c + gbres.getCount());
                        }
                    }
                }
            }
        }
    }

    public int getVocSize() {
        return vocSize;
    }

    public void setVocSize(int vocSize) {
        this.vocSize = vocSize;
    }

    public int getMinOcc() {
        return minOcc;
    }

    public void setMinOcc(int minOcc) {
        this.minOcc = minOcc;
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
