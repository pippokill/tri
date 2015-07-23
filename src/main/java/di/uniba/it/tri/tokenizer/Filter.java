/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.util.List;

/**
 *
 * @author pierpaolo
 */
public interface Filter {
    
    public void filter(List<String> tokens) throws Exception;
    
}
