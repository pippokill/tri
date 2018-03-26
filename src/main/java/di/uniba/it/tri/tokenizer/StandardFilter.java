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
public class StandardFilter implements Filter {

    @Override
    public void filter(List<String> tokens) throws Exception {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (!tokens.get(i).matches("^[A-Za-z_0-9]+$") || tokens.get(i).length() < 3) {
                tokens.remove(i);
            }
        }
    }

}
