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
public class BasicLatinFilter implements Filter {

    @Override
    public void filter(List<String> tokens) throws Exception {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (!tokens.get(i).matches("[\u0020-\u007E\u00A0-\u00FF]+")) {
                tokens.remove(i);
            }
        }
    }

}
