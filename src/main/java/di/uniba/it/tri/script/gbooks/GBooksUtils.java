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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class GBooksUtils {

    public static final Set<String> POSTAGS = new HashSet<>();

    public static final Set<String> TAGS = new HashSet<>();

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

}
