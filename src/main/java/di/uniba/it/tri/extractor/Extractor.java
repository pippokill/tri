/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package di.uniba.it.tri.extractor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * Interface for classes that extract textual content from a file
 * @author pierpaolo
 */
public interface Extractor {
    
    /**
     * Given a file extract the textual content
     * @param txtfile The file
     * @return The StingReader containg the textual content
     * @throws IOException
     */
    public StringReader extract(File txtfile) throws IOException;
    
}
