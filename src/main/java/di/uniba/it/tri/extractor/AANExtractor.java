/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Extract textual content from a paper in the AAN corpus
 *
 * @author pierpaolo
 */
public class AANExtractor implements Extractor {

    @Override
    public StringReader extract(File txtfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(txtfile));
        StringBuilder sb = new StringBuilder();
        boolean exit = false;
        while (reader.ready() && !exit) {
            String line = reader.readLine();
            if (line.contains("References") || line.contains("Bibliography")) {
                exit = true;
            } else {
                sb.append(line).append("\n");
            }
        }
        reader.close();
        return new StringReader(sb.toString());
    }

}
