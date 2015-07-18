/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public interface TriTokenizer {
    
    List<String> getTokens(Reader reader) throws IOException;
    
    List<String> getTokens(String text) throws IOException;
    
}
