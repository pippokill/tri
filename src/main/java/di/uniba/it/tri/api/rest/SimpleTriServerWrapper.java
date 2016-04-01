/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.api.rest;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.api.TriResultObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author pierpaolo
 */
public class SimpleTriServerWrapper {

    private static SimpleTriServerWrapper instance;

    private String basedir;

    private Tri api;

    private SimpleTriServerWrapper() {
    }

    public static synchronized SimpleTriServerWrapper getInstance() throws Exception {
        if (instance == null) {
            instance = new SimpleTriServerWrapper();
        }
        return instance;
    }

    public void setBasedir(String basedir) throws Exception {
        this.basedir = basedir;
        if (api == null) {
            api = new Tri();
        }
        api.setMaindir(basedir);
    }

    public String getBasedir() {
        return basedir;
    }

    public JSONObject words(String[] terms) throws Exception {
        for (int i = 0; i < terms.length; i++) {
            terms[i] = terms[i].trim().replaceAll("\\s+", "_");
        }
        List<TriResultObject> results = api.plotWord(terms);
        Map<String, List<TimeScore>> map = new HashMap<>();
        for (TriResultObject o : results) {
            String[] split = o.getValue().split("\t");
            List<TimeScore> list = map.get(split[1]);
            if (list == null) {
                list = new ArrayList<>();
                map.put(split[1], list);
            }
            list.add(new TimeScore(Integer.parseInt(split[0]), o.getScore()));
        }
        JSONObject m = new JSONObject();
        JSONArray authors = new JSONArray();
        for (String a : map.keySet()) {
            authors.add(a);
        }
        JSONObject data = new JSONObject();
        for (String a : map.keySet()) {
            JSONObject p = new JSONObject();
            JSONArray elements = new JSONArray();
            List<TimeScore> l = map.get(a);
            Collections.sort(l);
            for (int k = 1; k < l.size(); k++) {
                JSONObject tmo = new JSONObject();
                if (l.get(k).getScore() >= 0) {
                    tmo.put("value", l.get(k).getScore());
                } else {
                    tmo.put("value", 0);
                }
                tmo.put("date", l.get(k).getYear() + "-01-01");
                elements.add(tmo);
            }
            p.put("elements", elements);
            data.put(a, p);
        }
        m.put("authors", authors);
        m.put("data", data);
        return m;
    }

    public JSONObject wordsSim(String term1, String term2) throws Exception {
        term1 = term1.trim().replaceAll("\\s+", "_");
        term2 = term2.trim().replaceAll("\\s+", "_");
        List<TriResultObject> results = api.plotWords(term1, term2);
        JSONObject m = new JSONObject();
        JSONArray authors = new JSONArray();
        authors.add(term1 + "-" + term2);
        JSONObject data = new JSONObject();
        JSONObject p = new JSONObject();
        JSONArray elements = new JSONArray();
        for (TriResultObject r : results) {
            JSONObject tmo = new JSONObject();
            if (r.getScore() >= 0) {
                tmo.put("value", r.getScore());
            } else {
                tmo.put("value", 0);
            }
            String[] split = r.getValue().split("\t");
            tmo.put("date", split[0] + "-01-01");
            elements.add(tmo);
        }
        p.put("elements", elements);
        data.put(term1 + "-" + term2, p);
        m.put("authors", authors);
        m.put("data", data);
        return m;
    }

}
