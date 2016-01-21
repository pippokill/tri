/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.extractor;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author pierpaolo
 */
public interface IterableExtractor {

    public void extract(File file) throws IOException;

    public boolean hasNext() throws IOException;

    public String next() throws IOException;

}
