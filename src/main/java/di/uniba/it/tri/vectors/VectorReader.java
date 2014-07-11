/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package di.uniba.it.tri.vectors;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author pierpaolo
 */
public interface VectorReader {
    
    public void init() throws IOException;
    
    public void close() throws IOException;
    
    public Vector getVector(String key) throws IOException;
    
    public Iterator<String> getKeys() throws IOException;
    
    public Iterator<ObjectVector> getAllVectors() throws IOException;
    
    public int getDimension();
    
}
