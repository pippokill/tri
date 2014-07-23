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
 *
 * @author pierpaolo
 */
public interface Extractor {
    
    public StringReader extract(File txtfile) throws IOException;
    
}
