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
 *
 * @author pierpaolo
 */
public class TxtExtractor implements IterableExtractor {

    private StringBuilder sb = null;

    private boolean consumed = false;

    @Override
    public void extract(File txtfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(txtfile));
        sb = new StringBuilder();
        while (reader.ready()) {
            sb.append(reader.readLine()).append("\n");
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
