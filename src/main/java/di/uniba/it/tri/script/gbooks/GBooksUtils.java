/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */
package di.uniba.it.tri.script.gbooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author pierpaolo
 */
public class GBooksUtils {

    public static final Set<String> POSTAGS = new HashSet<>();

    public static final Set<String> TAGS = new HashSet<>();

    public static final String REG_EXP_IT = "[a-zèéàòùì]+";

    public static final String REG_EXP_EN = "[a-z]+";

    static {
        POSTAGS.add("NOUN");
        POSTAGS.add("VERB");
        POSTAGS.add("ADJ");
        POSTAGS.add("ADV");
        POSTAGS.add("PRON");
        POSTAGS.add("DET");
        POSTAGS.add("ADP");
        POSTAGS.add("NUM");
        POSTAGS.add("CONJ");
        POSTAGS.add("PRT");
        POSTAGS.add(".");

        TAGS.add("_ROOT_");
        TAGS.add("_START_");
        TAGS.add("_END_");
        TAGS.add("_NOUN_");
        TAGS.add("_VERB_");
        TAGS.add("_ADJ_");
        TAGS.add("_ADV_");
        TAGS.add("_PRON_");
        TAGS.add("_DET_");
        TAGS.add("_ADP_");
        TAGS.add("_NUM_");
        TAGS.add("_CONJ_");
        TAGS.add("_PRT_");
    }

    public static Ngram parseNgram(String line) throws Exception {
        String[] split = line.split("\\t+");
        Ngram ngram = new Ngram(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        String[] tokens = split[0].split("\\s+");
        for (String token : tokens) {
            if (TAGS.contains(token)) {
                //skip
            } else {
                int uid = token.lastIndexOf("_");
                if (uid >= 0) {
                    String a = token.substring(0, uid);
                    String b = token.substring(uid + 1);
                    if (POSTAGS.contains(b)) {
                        ngram.getTokens().add(a);
                    } else {
                        ngram.getTokens().add(token);
                    }
                } else {
                    ngram.getTokens().add(token);
                }
            }
        }
        return ngram;
    }

    public static Ngram parseNgramV2(String line, String regexp) throws Exception {
        String[] split = line.split("\\t+");
        Ngram ngram = new Ngram(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        String[] tokens = split[0].split("\\s+");
        for (String token : tokens) {
            if (token.matches(regexp)) {
                ngram.getTokens().add(token);
            } else {
                ngram.getTokens().add("_");
            }
        }
        return ngram;
    }

    public static Ngram parseNgramFromPlain(String line) throws Exception {
        String[] split = line.split("\\t+");
        Ngram ngram = new Ngram(-1, Integer.parseInt(split[1]), -1);
        String[] tokens = split[0].split("\\s+");
        for (String token : tokens) {
            ngram.getTokens().add(token);
        }
        return ngram;
    }

    public static Map<String, Integer> filterDictByFreq(File dictfile, int minfreq) throws IOException {
        Map<String, Integer> dict = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(dictfile));
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            int f = Integer.parseInt(split[1]);
            if (f >= minfreq) {
                dict.put(split[0], f);
            }
        }
        reader.close();
        return dict;
    }

    public static Map<String, Integer> filterDictBySize(File dictfile, int size) throws IOException {
        Map<String, Integer> dict = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(dictfile));
        int s = 0;
        while (reader.ready() && s < size) {
            String[] split = reader.readLine().split("\t");
            dict.put(split[0], Integer.parseInt(split[1]));
            s++;
        }
        reader.close();
        return dict;
    }

    public static List<NgramPair> getContexts(List<String> tokens) {
        List<NgramPair> list = new ArrayList<>();
        int k = 0;
        for (int i = 1; i < tokens.size(); i++) {
            list.add(new NgramPair(tokens.get(k), tokens.get(i)));
            list.add(new NgramPair(tokens.get(i), tokens.get(k)));
        }
        k = tokens.size() - 1;
        for (int i = 0; i < tokens.size() - 1; i++) {
            list.add(new NgramPair(tokens.get(k), tokens.get(i)));
            list.add(new NgramPair(tokens.get(i), tokens.get(k)));
        }
        return list;
    }

}
