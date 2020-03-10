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

/**
 * Extract textual content from a paper in the AAN corpus
 *
 * @author pierpaolo
 */
public class AANExtractor implements IterableExtractor {

    private StringBuilder sb = null;

    private boolean consumed = false;

    @Override
    public void extract(File txtfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(txtfile));
        sb = new StringBuilder();
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
    }

    @Override
    public boolean hasNext() throws IOException {
        return !consumed;
    }

    @Override
    public String next() throws IOException {
        consumed = true;
        return sb.toString();
    }

}
