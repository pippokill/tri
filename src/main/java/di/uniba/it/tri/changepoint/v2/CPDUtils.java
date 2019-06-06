/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class CPDUtils {
    
    public static List<ChangePoint> loadCPD(File file) throws IOException {
        List<ChangePoint> list=new ArrayList();
        BufferedReader reader=new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String[] split = reader.readLine().trim().split("\t");
            list.add(new ChangePoint(split[0], split[1], Double.parseDouble(split[2])));
        }
        reader.close();
        return list;
    }
    
}
