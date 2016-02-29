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
package di.uniba.it.tri.tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class KeywordFinder {

    private final IndexSearcher searcher;

    public KeywordFinder(File inputFile) throws IOException {
        RAMDirectory ramdir = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST, new WhitespaceAnalyzer());
        IndexWriter writer = new IndexWriter(ramdir, conf);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        while (reader.ready()) {
            String keyword = reader.readLine().toLowerCase().trim();
            if (keyword.length() > 0) {
                Document doc = new Document();
                doc.add(new TextField("keyword", keyword.replace("-", " ").replace("_", " ").replace("\\", " ").replace("/", " "), Field.Store.YES));
                writer.addDocument(doc);
            }
        }
        writer.close();
        searcher = new IndexSearcher(DirectoryReader.open(ramdir));
    }

    public List<String> search(String key) throws IOException {
        TermQuery q = new TermQuery(new Term("keyword", key));
        TopDocs topdocs = searcher.search(q, Integer.MAX_VALUE);
        List<String> rs = new ArrayList<>();
        for (int i = 0; i < topdocs.scoreDocs.length; i++) {
            rs.add(searcher.doc(topdocs.scoreDocs[i].doc).get("keyword"));
        }
        return rs;
    }

    private int find(List<String> tokens, List<String> candidate, int offset) {
        int find = -1;
        for (String c : candidate) {
            String[] split = c.split("\\s+");
            int k = offset;
            for (String s : split) {
                if (k < tokens.size()) {
                    if (!tokens.get(k).equals(s)) {
                        break;
                    } else {
                        k++;
                    }
                } else {
                    break;
                }
            }
            if ((k - offset) == split.length) {
                int idx = k - 1;
                if (idx > find) {
                    find = idx;
                }
            }
        }
        return find;
    }

    public List<String> process(List<String> tokens) throws IOException {
        List<String> newTokens = new ArrayList<>();
        int i = 0;
        while (i < tokens.size()) {
            List<String> rs = search(tokens.get(i));
            if (!rs.isEmpty()) {
                int find = find(tokens, rs, i);
                if (find >= 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int k = i; k <= find; k++) {
                        sb.append(tokens.get(k));
                        if (k < find) {
                            sb.append("_");
                        }
                    }
                    newTokens.add(sb.toString());
                    i = find + 1;
                } else {
                    newTokens.add(tokens.get(i));
                    i++;
                }
            } else {
                newTokens.add(tokens.get(i));
                i++;
            }
        }
        return newTokens;
    }

}
