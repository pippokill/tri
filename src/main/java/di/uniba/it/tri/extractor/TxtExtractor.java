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
public class TxtExtractor implements Extractor {

    @Override
    public StringReader extract(File txtfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(txtfile));
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            sb.append(reader.readLine()).append("\n");
        }
        reader.close();
        return new StringReader(sb.toString());
    }

}
