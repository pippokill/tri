/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class TextFileIterableExtractor implements IterableExtractor {

    private BufferedReader reader;

    private int counter = 0;

    @Override
    public void extract(File file) throws IOException {
        counter = 0;
        if (reader != null) {
            reader.close();
        } else {
            if (file.getName().endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                reader = new BufferedReader(new FileReader(file));
            }
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        boolean ready = reader.ready();
        if (!ready) {
            reader.close();
        }
        return ready;
    }

    @Override
    public String next() throws IOException {
        counter++;
        if (counter % 100 == 0) {
            System.out.print(".");
            if (counter % 10000 == 0) {
                System.out.println("." + counter);
            }
        }
        return reader.readLine();
    }

}
