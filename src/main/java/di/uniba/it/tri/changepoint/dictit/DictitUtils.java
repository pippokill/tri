/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.dictit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class DictitUtils {

    public static Map<String, List<Integer>> load(File file) throws IOException {
        Map<String, List<Integer>> gold = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            List<Integer> list = new ArrayList<>(split.length - 1);
            for (int i = 1; i < split.length; i++) {
                String[] split1 = split[i].split("_");
                list.add(Integer.parseInt(split1[0]));
            }
            Collections.sort(list);
            gold.put(split[0], list);
        }
        return gold;
    }

    public static Set<String> loadDictLemmas(File file) throws IOException {
        Set<String> lemmas = new HashSet();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String lemma = reader.readLine().split("\t")[0];
            lemmas.add(lemma);
        }
        reader.close();
        return lemmas;
    }

    public static Map<String, List<Integer>> filterByYear(Map<String, List<Integer>> map, int startYear, int endYear) {
        Map<String, List<Integer>> res = new HashMap<>();
        for (String key : map.keySet()) {
            List<Integer> list = map.get(key);
            for (int i = list.size() - 1; i >= 0; i--) {
                if (!(list.get(i) >= startYear && list.get(i) <= endYear)) {
                    list.remove(i);
                }
            }
            res.put(key, new ArrayList(list));
        }
        return res;
    }

    public static Map<String, List<Integer>> filterSingleCP(Map<String, List<Integer>> map) {
        Map<String, List<Integer>> res = new HashMap<>();
        for (String key : map.keySet()) {
            List<Integer> list = map.get(key);
            if (list.size() > 1) {
                list.remove(0);
                res.put(key, new ArrayList(list));
            }
        }
        return res;
    }

}
