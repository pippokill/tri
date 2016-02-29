/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.io.Reader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class EnglishNoStemAnalyzer extends StopwordAnalyzerBase {

    /**
     * Returns an unmodifiable instance of the default stop words set.
     *
     * @return default stop words set.
     */
    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    /**
     * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer
     * class accesses the static final set the first time.;
     */
    private static class DefaultSetHolder {

        static final CharArraySet DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
    }

    public EnglishNoStemAnalyzer() {
        this(DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords a stopword set
     */
    public EnglishNoStemAnalyzer(CharArraySet stopwords) {
        super(stopwords);
    }

    /**
     * Creates a
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which
     * tokenizes all the text in the provided {@link Reader}.
     *
     * @param fieldName
     * @param reader
     * @return A
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} built
     * from an {@link StandardTokenizer} filtered with null null null null     {@link StandardFilter}, {@link EnglishPossessiveFilter}, 
   *         {@link LowerCaseFilter}, {@link StopFilter}
     *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is provided and
     * {@link PorterStemFilter}.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader) {
        final Tokenizer source = new StandardTokenizer(reader);
        TokenStream result = new StandardFilter(source);
        // prior to this we get the classic behavior, standardfilter does it for us.
        result = new EnglishPossessiveFilter(Version.LATEST, result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);
        return new TokenStreamComponents(source, result);
    }
}
