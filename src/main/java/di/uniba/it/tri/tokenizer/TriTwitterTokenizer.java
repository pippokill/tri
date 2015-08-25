/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class TriTwitterTokenizer implements TriTokenizer {

    @Override
    public List<String> getTokens(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        while (br.ready()) {
            sb.append(br.readLine());
            sb.append("\n");
        }
        br.close();
        return getTokens(sb.toString());
    }

    @Override
    public List<String> getTokens(String text) throws IOException {
        List<String> tokens=Twokenize.tokenize(text);
        List<String> newTokens=new ArrayList<>(tokens.size());
        for(String s:tokens) {
            if (s.startsWith("http://") || s.startsWith("https://")) {
                newTokens.add("_URL_");
            } else if (s.startsWith("@")) {
                newTokens.add("_USR_");
            } else {
                newTokens.add(s.toLowerCase());
            }
        }
        return newTokens;
    }

}
