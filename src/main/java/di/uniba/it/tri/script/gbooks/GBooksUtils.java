/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
