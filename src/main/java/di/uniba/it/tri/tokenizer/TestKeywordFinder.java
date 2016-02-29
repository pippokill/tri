/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class TestKeywordFinder {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            KeywordFinder finder = new KeywordFinder(new File("/home/pierpaolo/Dropbox/Progetti aiinlp 1516/TRI_recsys - Corcelli/dictionary_keyword.txt"));
            TriEnStandardTokenizer tokenizer = new TriEnStandardTokenizer();
            List<String> tokens = tokenizer.getTokens("Recommender systems or recommendation systems (sometimes replacing \"system\" with a synonym such as platform or engine) are a subclass of information filtering system that seek to predict the 'rating' or 'preference' that a user would give to an item. Context-based recommendation. a/b testing.");
            System.out.println(tokens);
            List<String> process = finder.process(tokens);
            System.out.println(process);
        } catch (IOException ex) {
            Logger.getLogger(TestKeywordFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
