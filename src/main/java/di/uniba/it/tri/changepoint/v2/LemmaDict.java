/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author pierpaolo
 */
public class LemmaDict {

    private Map<String,String> dict=null;
    
    public LemmaDict() {
    }

    public void init(File dictFile) throws IOException {
        dict=new Object2ObjectOpenHashMap<>();
        BufferedReader reader=new BufferedReader(new FileReader(dictFile));
        while (reader.ready()) {
            String[] values=reader.readLine().split("\t");
            for (int i=1;i<values.length;i++) {
                dict.put(values[i],values[0]);
            }
        }
        reader.close();
    }
    
    public void close() {
        dict=null;
        System.gc();
    }
    
    public String getLemma(String wordform) {
        return dict.get(wordform);
    }
    
}
