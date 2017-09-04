/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.api.occ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class OccAPI {

    private static final Logger LOG = Logger.getLogger(OccAPI.class.getName());

    private final File mainDir;

    private final Map<Integer, File> fileMap;

    public OccAPI(File mainDir) throws IOException {
        this.mainDir = mainDir;
        this.fileMap = getYears();
    }

    public File getMainDir() {
        return mainDir;
    }

    public Map<Integer, File> getFileMap() {
        return fileMap;
    }

    private Map<Integer, File> getYears() throws IOException {
        Map<Integer, File> map = new HashMap<>();
        Pattern pattern = Pattern.compile("[0-9]+");
        File[] listFiles = mainDir.listFiles();
        for (File file : listFiles) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group());
                    map.put(year, file);
                }
            }
        }
        return map;
    }

    private List<Word> buildWordList(String[] split, int size) {
        List<Word> list = new ArrayList<>();
        int i = 1;
        double n = 0;
        while (i < split.length) {
            Word word = new Word(split[i], Double.parseDouble(split[i + 1]));
            list.add(word);
            n += word.getScore();
            i = i + 2;
        }
        for (Word word : list) {
            word.setScore(word.getScore() / n);
        }
        Collections.sort(list, Collections.reverseOrder());
        if (list.size() > size) {
            return list.subList(0, size);
        }
        return list;
    }

    public Map<Integer, List<Word>> getOccurrences(String word, int[] years, int size) throws IOException {
        Map<Integer, List<Word>> r = new HashMap<>();
        for (int year : years) {
            File file = fileMap.get(year);
            if (file != null) {
                BufferedReader reader = null;
                if (file.getName().endsWith(".gz")) {
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                } else {
                    reader = new BufferedReader(new FileReader(file));
                }
                boolean found = false;
                while (reader.ready() && !found) {
                    String line = reader.readLine();
                    String[] split = line.split("\t");
                    if (split[0].equals(word)) {
                        found = true;
                        List<Word> list = buildWordList(split, size);
                        r.put(year, list);
                    }
                }
                reader.close();
            } else {
                LOG.log(Level.WARNING, "No file for year: {0}", year);
            }
        }
        return r;
    }

}
