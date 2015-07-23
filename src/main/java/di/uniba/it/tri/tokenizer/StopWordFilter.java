/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.util.List;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class StopWordFilter implements Filter {
    
    private final Set<String> set;

    public StopWordFilter(Set<String> set) {
        this.set = set;
    }

    public Set<String> getSet() {
        return set;
    }

    @Override
    public void filter(List<String> tokens) throws Exception {
        for (int i=tokens.size()-1;i>=0;i--) {
            if (set.contains(tokens.get(i)))
                tokens.remove(i);
        }
    }
    
}
