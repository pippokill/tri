/*
 * Questo software è stato sviluppato dal gruppo di ricerca SWAP del Dipartimento di Informatica dell'Università degli Studi di Bari.
 * Tutti i diritti sul software appartengono esclusivamente al gruppo di ricerca SWAP.
 * Il software non può essere modificato e utilizzato per scopi di ricerca e/o industriali senza alcun permesso da parte del gruppo di ricerca SWAP.
 * Il software potrà essere utilizzato a scopi di ricerca scientifica previa autorizzazione o accordo scritto con il gruppo di ricerca SWAP.
 * 
 * Bari, Marzo 2014
 */
package di.uniba.it.tri.tokenizer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

/**
 * {@link Analyzer} for Italian.
 * 
 * You may specify the {@link Version} compatibility when creating
 * ItalianAnalyzer:
 * <ul>
 * <li> As of 3.6, ItalianLightStemFilter is used for less aggressive stemming.
 * <li> As of 3.2, ElisionFilter with a set of Italian contractions is used by
 * default.
 * </ul>
 */
public final class ItalianNoStemAnalyzer extends StopwordAnalyzerBase {

    /**
     * File containing default Italian stopwords.
     */
    public final static String DEFAULT_STOPWORD_FILE = "italian_stop.txt";

    private static final CharArraySet DEFAULT_ARTICLES = CharArraySet.unmodifiableSet(
            new CharArraySet(
                    Arrays.asList(
                            "c", "l", "all", "dall", "dell", "nell", "sull", "coll", "pell",
                            "gl", "agl", "dagl", "degl", "negl", "sugl", "un", "m", "t", "s", "v", "d"
                    ), true));

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

        static final CharArraySet DEFAULT_STOP_SET;

        static {
            try {
                DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(IOUtils.getDecodingReader(SnowballFilter.class,
                        DEFAULT_STOPWORD_FILE, StandardCharsets.UTF_8));
            } catch (IOException ex) {
                // default set should always be present as it is part of the
                // distribution (JAR)
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }

    /**
     * Builds an analyzer with the default stop words:
     * {@link #DEFAULT_STOPWORD_FILE}.
     */
    public ItalianNoStemAnalyzer() {
        this(DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopwords a stopword set
     */
    public ItalianNoStemAnalyzer(CharArraySet stopwords) {
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
     * from an {@link StandardTokenizer} filtered with null null     {@link StandardFilter}, {@link ElisionFilter}, {@link LowerCaseFilter}, {@link StopFilter}
   *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is provided and
     * {@link ItalianLightStemFilter}.
     */
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new StandardTokenizer(reader);
        TokenStream result = new StandardFilter(source);
        result = new ElisionFilter(result, DEFAULT_ARTICLES);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);
        return new Analyzer.TokenStreamComponents(source, result);
    }
}
