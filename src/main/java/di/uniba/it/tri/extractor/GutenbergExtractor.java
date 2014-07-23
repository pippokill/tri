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
public class GutenbergExtractor implements Extractor {

    @Override
    public StringReader extract(File txtfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(txtfile));
        StringBuilder sb = new StringBuilder();
        while (reader.ready()) {
            sb.append(reader.readLine()).append("\n");
        }
        reader.close();
        //remove intro
        int l = sb.indexOf("*** START OF THIS PROJECT GUTENBERG");
        if (l >= 0) {
            int end = sb.indexOf("\n\n", l + 1);
            if (end >= 0) {
                sb = sb.delete(l, end + 1);
            }
        }
        l = sb.indexOf("Produced by");
        if (l >= 0) {
            int end = sb.indexOf("\n\n", l + 1);
            if (end >= 0) {
                sb = sb.delete(l, end + 1);
            }
        }
        l = sb.indexOf("This file was produced");
        if (l >= 0) {
            int end = sb.indexOf("\n\n", l + 1);
            if (end >= 0) {
                sb = sb.delete(l, end + 1);
            }
        }
        //remove end
        l = sb.indexOf("End of the Project Gutenberg");
        if (l >= 0) {
            sb = sb.delete(l, sb.length());
        }
        l = sb.indexOf("*** END OF THIS PROJECT GUTENBERG");
        if (l >= 0) {
            sb = sb.delete(l, sb.length());
        }
        return new StringReader(sb.toString());
    }

}
