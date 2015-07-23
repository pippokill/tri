/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class OccUtils {
    
    public static Set<String> loadSet(File inputfile) throws IOException {
        Set<String> set=new HashSet<>();
        BufferedReader reader=new BufferedReader(new FileReader(inputfile));
        while (reader.ready()) {
            set.add(reader.readLine().trim().toLowerCase());
        }
        return set;
    }
    
}
