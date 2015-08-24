/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author pierpaolo
 */
public class TriEnStandardTokenizer implements TriTokenizer {

    @Override
    public List<String> getTokens(Reader reader) throws IOException {
        List<String> tokens = new ArrayList<>();
        Analyzer analyzer = new EnglishNoStemAnalyzer(CharArraySet.EMPTY_SET);
        TokenStream tokenStream = analyzer.tokenStream("text", reader);
        tokenStream.reset();
        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            tokens.add(token);
        }
        tokenStream.end();
        return tokens;
    }

    @Override
    public List<String> getTokens(String text) throws IOException {
        return getTokens(new StringReader(text));
    }

}
