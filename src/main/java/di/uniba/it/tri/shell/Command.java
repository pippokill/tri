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
package di.uniba.it.tri.shell;

import di.uniba.it.tri.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.MemoryVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

/**
 * This class is used as the wrapper to interpret and execute commands from the
 * shell
 *
 * @author pierpaolo
 */
public class Command {

    private File mainDir;

    private IndexReader reader;

    //public static int MAX_SEARCH_RESULTS = 10;
    //public static int MAX_NEAR_VECTORS = 25;
    private static final String ELEMENTAL_NAME = "*elemental*";

    private static final QueryParser parser = new QueryParser(Version.LUCENE_36, "word", new StandardAnalyzer(Version.LUCENE_36));

    private final Map<String, VectorReader> stores = new HashMap<>();

    private final Map<String, Vector> vectors = new HashMap<>();

    private final Map<String, Set<String>> setmap = new HashMap<>();

    private final Properties help = new Properties();

    private static final String vs = "|";

    private static final String os = "-";

    /**
     *
     */
    public Command() {
        initHelp();
    }

    /**
     * Get the main directory of the RI spaces
     *
     * @return The main directory of the RI spaces
     */
    public File getMainDir() {
        return mainDir;
    }

    /**
     * Set the main directory of the RI spaces
     *
     * @param mainDir The main directory of the RI spaces
     */
    public void setMainDir(File mainDir) {
        this.mainDir = mainDir;
    }

    /**
     * Execute a command given the command line
     *
     * @param command The command line
     * @throws Exception
     */
    public void executeCommand(String command) throws Exception {
        if (command.matches("(^indexelem$)|(^indexelem\\s+.*$)")) {
            indexelem();
        } else if (command.matches("(^index$)|(^index\\s+.*$)")) {
            index(command);
        } else if (command.matches("(^search$)|(^search\\s+.*$)")) {
            search(command);
        } else if (command.matches("(^set$)|(^set\\s+.*$)")) {
            set(command);
        } else if (command.matches("(^year$)|(^year\\s+.*$)")) {
            year(command);
        } else if (command.matches("(^load$)|(^load\\s+.*$)")) {
            load(command);
        } else if (command.matches("(^fload$)|(^fload\\s+.*$)")) {
            fload(command);
        } else if (command.matches("(^list$)|(^list\\s+.*$)")) {
            list(command);
        } else if (command.matches("(^clear$)|(^clear\\s+.*$)")) {
            clear(command);
        } else if (command.matches("(^get$)|(^get\\s+.*$)")) {
            get(command);
        } else if (command.matches("(^add$)|(^add\\s+.*$)")) {
            add(command);
        } else if (command.matches("(^addv$)|(^addv\\s+.*$)")) {
            addv(command);
        } else if (command.matches("(^near$)|(^near\\s+.*$)")) {
            near(command);
        } else if (command.matches("(^sim$)|(^sim\\s+.*$)")) {
            sim(command);
        } else if (command.matches("(^tri$)|(^tri\\s+.*$)")) {
            tri(command);
        } else if (command.matches("(^ftri$)|(^ftri\\s+.*$)")) {
            ftri(command);
        } else if (command.matches("(^compare$)|(^compare\\s+.*$)")) {
            compare(command);
        } else if (command.matches("(^cset$)|(^cset\\s+.*$)")) {
            cset(command);
        } else if (command.matches("(^rset$)|(^rset\\s+.*$)")) {
            rset(command);
        } else if (command.matches("(^dset$)|(^dset\\s+.*$)")) {
            dset(command);
        } else if (command.matches("(^aset$)|(^aset\\s+.*$)")) {
            aset(command);
        } else if (command.matches("(^sset$)|(^sset\\s+.*$)")) {
            sset(command);
        } else if (command.matches("(^pset$)|(^pset\\s+.*$)")) {
            pset(command);
        } else if (command.matches("(^vset$)|(^vset\\s+.*$)")) {
            vset(command);
        } else if (command.matches("(^count$)|(^count\\s+.*$)")) {
            count(command);
        } else if (command.matches("(^sims$)|(^sims\\s+.*$)")) {
            sims(command);
        } else {
            throw new Exception("Unknown command: " + command);
        }
    }

    //index words from elemental file
    private void indexelem() throws Exception {
        if (mainDir != null) {
            reader = TemporalSpaceUtils.indexElemental(mainDir);
        } else {
            throw new Exception("Main dir not set");
        }
    }

    //index a vector file
    private void index(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            switch (split[1]) {
                case "file":
                    File file = new File(split[2]);
                    if (file.isFile()) {
                        if (reader != null) {
                            reader.close();
                        }
                        reader = TemporalSpaceUtils.index(file);
                    } else {
                        throw new Exception("no valid file: " + split[2]);
                    }
                    break;
                case "mem":
                    VectorReader vr = stores.get(split[2]);
                    if (vr == null) {
                        throw new Exception("no vector store for " + split[2]);
                    } else {
                        if (reader != null) {
                            reader.close();
                        }
                        reader = TemporalSpaceUtils.index(vr);
                    }
                    break;
                default:
                    throw new Exception("index type error");
            }
        } else {
            throw new Exception("index syntax error");
        }
    }

    //seach in loaded index
    private void search(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            if (reader == null) {
                throw new Exception("no index in memory");
            } else {
                if (!split[1].matches("[0-9]+")) {
                    throw new Exception("no valid number of results");
                }
                StringBuilder qs = new StringBuilder();
                for (int i = 2; i < split.length; i++) {
                    qs.append(split[i]).append(" ");
                }
                //String q = QueryParser.escape(qs.toString().trim());
                Query query = parser.parse(qs.toString().trim());
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs topDocs = searcher.search(query, Integer.parseInt(split[1]));
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    TriShell.print(searcher.doc(scoreDoc.doc).get("word"));
                    TriShell.print("\t");
                    TriShell.println(String.valueOf(scoreDoc.score));
                }
            }
        } else {
            throw new Exception("search syntax error");
        }
    }

    //set main dir
    private void set(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 1) {
            File file = new File(split[1]);
            if (file.isDirectory()) {
                this.setMainDir(file);
            } else {
                throw new Exception("Not valid directory: " + split[1]);
            }
        } else {
            throw new Exception("set syntax error");
        }
    }

    //list available yeaar
    private void year(String cmd) throws Exception {
        if (mainDir == null) {
            throw new Exception("Main dir not set");
        } else {
            String[] split = cmd.split("\\s+");
            if (split.length == 1) {
                List<String> availableYears = TemporalSpaceUtils.getAvailableYears(mainDir, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                for (String year : availableYears) {
                    TriShell.print(year);
                    TriShell.print(" ");
                }
                TriShell.println("");
            } else if (split.length > 2) {
                int start = Integer.parseInt(split[1]);
                int end = Integer.parseInt(split[2]);
                List<String> availableYears = TemporalSpaceUtils.getAvailableYears(mainDir, start, end);
                for (String year : availableYears) {
                    TriShell.print(year);
                    TriShell.print(" ");
                }
                TriShell.println("");

            } else {
                throw new Exception("year syntax error");
            }
        }
    }

    //load a VectorReader
    private void load(String cmd) throws Exception {
        if (mainDir == null) {
            throw new Exception("Main dir not set");
        } else {
            String[] split = cmd.split("\\s+");
            if (split.length == 2) {
                loadVectorReader(split[1], ELEMENTAL_NAME, TemporalSpaceUtils.getElementalFile(mainDir));
            } else if (split.length > 3) {
                if (split[3].matches("[0-9]+")) {
                    loadVectorReader(split[1], split[2], TemporalSpaceUtils.getVectorFile(mainDir, split[3]));
                } else {
                    throw new Exception("not valid year");
                }
            } else {
                throw new Exception("load syntax error");
            }
        }
    }

    //load a VectorReader
    private void fload(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            loadVectorReader(split[1], split[2], new File(split[3]));
        } else {
            throw new Exception("load syntax error");
        }

    }

    private void list(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 2) {
            switch (split[1]) {
                case "stores":
                    Iterator<String> iterator = stores.keySet().iterator();
                    while (iterator.hasNext()) {
                        TriShell.println(iterator.next());
                    }
                    TriShell.println(stores.size() + " stores loaded.");
                    break;
                case "vectors":
                    Iterator<String> iterator1 = vectors.keySet().iterator();
                    while (iterator1.hasNext()) {
                        TriShell.println(iterator1.next());
                    }
                    TriShell.println(vectors.size() + " vectors loaded.");
                    break;
                case "sets":
                    Iterator<String> iterator2 = setmap.keySet().iterator();
                    while (iterator2.hasNext()) {
                        TriShell.println(iterator2.next());
                    }
                    TriShell.println(setmap.size() + " sets loaded.");
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        }

    }

    //Utils to load a VectorReader
    private void loadVectorReader(String type, String name, File file) throws Exception {
        VectorReader vr = null;
        if (type.equals("mem")) {
            vr = new MemoryVectorReader(file);
            vr.init();
            if (stores.containsKey(name)) {
                stores.get(name).close();
                TriShell.println("Replaced vector reader: " + name);
            } else {
                TriShell.println("New vector reader: " + name);
            }
            stores.put(name, vr);
        } else if (type.equals("file")) {
            vr = new FileVectorReader(file);
            vr.init();
            if (stores.containsKey(name)) {
                stores.get(name).close();
                TriShell.println("Replaced vector reader: " + name);
            } else {
                TriShell.println("New vector reader: " + name);
            }
            stores.put(name, vr);
        } else {
            throw new Exception("not valid vector reader type");
        }
    }

    private void clear(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 2) {
            switch (split[1]) {
                case "stores":
                    stores.clear();
                    break;
                case "vectors":
                    vectors.clear();
                    break;
                case "index":
                    if (reader != null) {
                        reader.close();
                    }
                    reader = null;
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        } else if (split.length > 2) {
            switch (split[1]) {
                case "stores":
                    stores.remove(split[2]);
                    break;
                case "vectors":
                    vectors.remove(split[2]);
                    break;
                case "index":
                    if (reader != null) {
                        reader.close();
                    }
                    reader = null;
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        } else {
            throw new Exception("clear syntax error");
        }
    }

    private void add(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            VectorReader vr = stores.get(split[1]);
            if (vr != null) {
                Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
                for (int i = 3; i < split.length; i++) {
                    Vector wv = vr.getVector(split[i]);
                    if (wv != null) {
                        v.superpose(wv, 1, null);
                    } else {
                        TriShell.println("no vector for: " + split[i]);
                    }
                }
                v.normalize();
                if (vectors.containsKey(split[2])) {
                    TriShell.println("replaced vector: " + split[2]);
                } else {
                    TriShell.println("created vector: " + split[2]);
                }
                vectors.put(split[2], v);
            } else {
                TriShell.println("no stores for: " + split[1]);
            }
        } else {
            throw new Exception("add syntax error");
        }
    }

    private void addv(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            int dimension = -1;
            for (int i = 2; i < split.length; i++) {
                Vector v = vectors.get(split[i]);
                if (v != null) {
                    dimension = v.getDimension();
                } else {
                    TriShell.println("no vector for: " + split[i]);
                }
            }
            if (dimension > 0) {
                Vector nv = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                for (int i = 2; i < split.length; i++) {
                    Vector v = vectors.get(split[i]);
                    if (v != null) {
                        nv.superpose(v, 1, null);
                    }
                }
                nv.normalize();
                if (vectors.containsKey(split[1])) {
                    TriShell.println("replaced vector: " + split[1]);
                } else {
                    TriShell.println("created vector: " + split[1]);
                }
                vectors.put(split[1], nv);
            }
        } else {
            throw new Exception("addv syntax error");
        }
    }

    private void get(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            VectorReader vr = stores.get(split[1]);
            if (vr != null) {
                Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
                Vector wv = vr.getVector(split[3]);
                if (wv != null) {
                    v.superpose(wv, 1, null);
                } else {
                    TriShell.println("no vector for: " + split[3]);
                }
                v.normalize();
                if (vectors.containsKey(split[2])) {
                    TriShell.println("replaced vector: " + split[2]);
                } else {
                    TriShell.println("created vector: " + split[2]);
                }
                vectors.put(split[2], v);
            } else {
                TriShell.println("no stores for: " + split[1]);
            }
        } else {
            throw new Exception("add syntax error");
        }
    }

    /**
     * Close all vector readers and clear stores
     *
     * @throws Exception
     */
    public void close() throws Exception {
        for (VectorReader vr : stores.values()) {
            vr.close();
        }
        stores.clear();
        vectors.clear();
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Print the full help on the shell
     */
    public void fullHelp() {
        if (help != null) {
            Iterator<String> iterator = help.stringPropertyNames().iterator();
            while (iterator.hasNext()) {
                String cmd = iterator.next();
                TriShell.println(cmd + "\t" + help.getProperty(cmd));
            }
        }
    }

    /**
     * Print the command help on the shell
     *
     * @param cmd The command
     */
    public void help(String cmd) {
        if (help != null) {
            if (cmd.equals("*")) {
                fullHelp();
            } else {
                String s = help.getProperty(cmd);
                if (s != null) {
                    TriShell.println(s);
                } else {
                    TriShell.println("no valid command: " + cmd);
                }
            }
        }
    }

    private void near(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 3) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            VectorReader vr = stores.get(split[2]);
            if (vr != null) {
                Vector v = vectors.get(split[3]);
                if (v != null) {
                    List<ObjectVector> nv = TemporalSpaceUtils.getNearestVectors(vr, v, Integer.parseInt(split[1]));
                    for (ObjectVector ov : nv) {
                        TriShell.println(ov.getKey() + "\t" + ov.getScore());
                    }
                } else {
                    throw new Exception("no vector for: " + split[3]);
                }
            } else {
                throw new Exception("vector reader not found: " + split[2]);
            }
        } else {
            throw new Exception("near syntax error");
        }
    }

    private void sim(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            Vector v1 = vectors.get(split[1]);
            if (v1 == null) {
                throw new Exception("no vector for: " + split[1]);
            }
            Vector v2 = vectors.get(split[2]);
            if (v2 == null) {
                throw new Exception("no vector for: " + split[2]);
            }
            TriShell.println("Sim(" + split[1] + ", " + split[2] + ")=" + v1.measureOverlap(v2));
        } else {
            throw new Exception("sim syntax error");
        }
    }

    private void tri(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (split[2].matches("[0-9]+") && split[3].matches("[0-9]+")) {
                List<File> files = TemporalSpaceUtils.getFileTemporalRange(mainDir, Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                VectorReader[] readers = new VectorReader[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    readers[i] = new FileVectorReader(files.get(i));
                    readers[i].init();
                }
                VectorReader tir = TemporalSpaceUtils.combineAndBuildVectorReader(readers);
                tir.init();
                if (stores.containsKey(split[1])) {
                    TriShell.println("replaced stores: " + split[1]);
                } else {
                    TriShell.println("created new stores: " + split[1]);
                }
                stores.put(split[1], tir);
                for (VectorReader r : readers) {
                    r.close();
                }
            } else {
                throw new Exception("tri syntax error");
            }
        } else {
            throw new Exception("tri syntax error");
        }
    }

    private void ftri(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (split[2].matches("[0-9]+") && split[3].matches("[0-9]+")) {
                List<File> files = TemporalSpaceUtils.getFileTemporalRange(mainDir, Integer.parseInt(split[2]), Integer.parseInt(split[3]));
                VectorReader[] readers = new VectorReader[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    readers[i] = new FileVectorReader(files.get(i));
                    readers[i].init();
                }
                TemporalSpaceUtils.combineAndSaveVectorReader(new File(split[1]), readers);
                for (VectorReader r : readers) {
                    r.close();
                }
            } else {
                throw new Exception("ftri syntax error");
            }
        } else {
            throw new Exception("ftri syntax error");
        }
    }

    private void compare(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 5) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            VectorReader vr1 = stores.get(split[2]);
            if (vr1 == null) {
                throw new Exception("no valid store for: " + split[2]);
            }
            VectorReader vr2 = stores.get(split[3]);
            if (vr2 == null) {
                throw new Exception("no valid store for: " + split[3]);
            }
            Vector v1 = vectors.get(split[4]);
            if (v1 == null) {
                throw new Exception("no vector for: " + split[4]);
            }
            Vector v2 = vectors.get(split[5]);
            if (v2 == null) {
                throw new Exception("no vector for: " + split[5]);
            }
            List<ObjectVector> n1 = TemporalSpaceUtils.getNearestVectors(vr1, v1, Integer.parseInt(split[1]));
            List<ObjectVector> n2 = TemporalSpaceUtils.getNearestVectors(vr2, v2, Integer.parseInt(split[1]));
            int size = Math.min(n1.size(), n2.size());
            for (int k = 0; k < size; k++) {
                TriShell.println(n1.get(k).getKey() + "\t" + n1.get(k).getScore() + " " + vs + " "
                        + n2.get(k).getKey() + "\t" + n2.get(k).getScore());
            }
        } else {
            throw new Exception("compare syntax error");
        }
    }

    private void count(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 1) {
            VectorReader vr = stores.get(split[1]);
            if (vr == null) {
                throw new Exception("no valid store for: " + split[1]);
            }
            int countVectors = TemporalSpaceUtils.countVectors(vr);
            TriShell.println(split[1] + " contains " + countVectors + ".");
        } else {
            throw new Exception("compare syntax error");
        }
    }

    private void cset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            if (setmap.containsKey(split[1])) {
                TriShell.println("set replaced: " + split[1]);
            }
            setmap.put(split[1], new HashSet<String>());
        } else {
            throw new Exception("cset syntax error");
        }
    }

    private void rset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            Set<String> set = setmap.get(split[1]);
            if (set != null) {
                for (int i = 2; i < split.length; i++) {
                    set.remove(split[i]);
                }
            } else {
                TriShell.println("no set for: " + split[1]);
            }
        } else {
            throw new Exception("rset syntax error");
        }
    }

    private void dset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            setmap.remove(split[1]);
        } else {
            throw new Exception("dset syntax error");
        }
    }

    private void aset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            Set<String> set = setmap.get(split[1]);
            if (set != null) {
                for (int i = 2; i < split.length; i++) {
                    set.add(split[i]);
                }
            } else {
                TriShell.println("no set for: " + split[1]);
            }
        } else {
            throw new Exception("aset syntax error");
        }
    }

    private void sset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 3) {
            if (reader == null) {
                throw new Exception("no index in memory");
            } else {
                Set<String> set = setmap.get(split[1]);
                if (set == null) {
                    throw new Exception("no set for: " + split[1]);
                }
                if (!split[2].matches("[0-9]+")) {
                    throw new Exception("no valid number of results");
                }
                StringBuilder qs = new StringBuilder();
                for (int i = 3; i < split.length; i++) {
                    qs.append(split[i]).append(" ");
                }
                //String q = QueryParser.escape(qs.toString().trim());
                Query query = parser.parse(qs.toString().trim());
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs topDocs = searcher.search(query, Integer.parseInt(split[2]));
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    TriShell.print("add to " + split[1] + "\t");
                    String word = searcher.doc(scoreDoc.doc).get("word");
                    TriShell.print(word);
                    TriShell.print("\t");
                    TriShell.println(String.valueOf(scoreDoc.score));
                    set.add(word);
                }
            }
        } else {
            throw new Exception("sset syntax error");
        }
    }

    private void pset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            Set<String> set = setmap.get(split[1]);
            if (set != null) {
                for (String word : set) {
                    TriShell.println(word);
                }
            } else {
                TriShell.println("no set for: " + split[1]);
            }
        } else {
            throw new Exception("pset syntax error");
        }
    }

    private void vset(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            VectorReader vr = stores.get(split[1]);
            if (vr == null) {
                throw new Exception("no stores for: " + split[1]);
            }
            Set<String> set = setmap.get(split[2]);
            if (set == null) {
                throw new Exception("no set for: " + split[2]);
            }
            Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
            for (String word : set) {
                Vector wv = vr.getVector(word);
                if (wv != null) {
                    v.superpose(wv, 1, null);
                } else {
                    TriShell.println("no vector for: " + word);
                }
            }
            v.normalize();
            if (vectors.containsKey(split[3])) {
                TriShell.println("replaced vector: " + split[3]);
            } else {
                TriShell.println("created vector: " + split[3]);
            }
            vectors.put(split[3], v);
        } else {
            throw new Exception("set2vec syntax error");
        }
    }

    private void sims(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            VectorReader vr1 = stores.get(split[2]);
            if (vr1 == null) {
                throw new Exception("no valid store for: " + split[2]);
            }
            VectorReader vr2 = stores.get(split[3]);
            if (vr2 == null) {
                throw new Exception("no valid store for: " + split[3]);
            }
            List<ObjectVector> sims = TemporalSpaceUtils.sims(vr1, vr2, Integer.parseInt(split[1]));
            for (ObjectVector ov : sims) {
                TriShell.println(ov.getKey() + "\t" + ov.getScore());
            }
        } else {
            throw new Exception("sims syntax error");
        }
    }

    private void initHelp() {
        help.setProperty("set", "set <main dir> - set the main directory in which WordSpaces are stored");
        help.setProperty("index", "index <file|mem> <name> - create a words index from a vector reader using a filename (file) or a previous reader loaded in memory (mem)");
        help.setProperty("search", "search <number of resutls> <query> - search in the current words index");
        help.setProperty("indexelem", "indexelem - create the words index of the elemental vector");
        help.setProperty("year", "year (<start> <end>)* - list the available years");
        help.setProperty("load", "load <file|mem> (<name> <year>)* - load a vector reader of the specified type (mem or file) and year. If no name and year are provided the elemental vector reader is loaded");
        help.setProperty("fload", "fload <file|mem> <name> <filename> - load a vector reader (name) of the specified type (mem or file) from a file");
        help.setProperty("list", "list <stores|vectors> - list stores or vectors available in memory");
        help.setProperty("clear", "clear <stores|vectors|index> <name>* - remove a vector reader (stores) or a vector (vectors) or the index. If no name is provided all the elements are removed");
        help.setProperty("get", "get <vector reader name> <vector name> <word> - get the word vector from the vector reader and store it in memory using the vector name");
        help.setProperty("add", "add <vector reader name> <vector name> <word>+ - get and sum multiple word vectors from the vector reader and store the result in memory using the vector name");
        help.setProperty("addv", "addv <vector reader name> <vector name> <vectors>+ - get and sum multiple vectors in memory and store the result in memory using the vector name");
        help.setProperty("near", "near <number of results> <vector reader name> <vector name> - print nearest vectors");
        help.setProperty("sim", "sim <vector name 1> <vector name 2> - print vectors similarity");
        help.setProperty("count", "count <vector reader name> - return the number of vectors in the vector reader");
        help.setProperty("tri", "tri <vector reader name> <start year> <end year> - create a new temporal space named vector reader name form start_year to end_year");
        help.setProperty("ftri", "ftri <output filename> <start year> <end year> - create a new temporal space form start_year to end_year and save it on disk");
        help.setProperty("sims", "sims <number of results> <vector reader name1> <vector reader name2> - find words that change meaning between two WordSpaces");
        help.setProperty("compare", "compare <number of results> <vector reader name1> <vector reader name1> <vector name1> <vector name2> - compare nearest vectors of vector name1 in vector reader name1 and vector name2 in vector reader name2");
        //help of commands related to sets
        help.setProperty("cset", "cset <name> - create a new set");
        help.setProperty("aset", "aset <name> <word>+ - add words to a set");
        help.setProperty("rset", "rset <name> <word>+ - remove words from a set");
        help.setProperty("dset", "dset <name> - remove a set");
        help.setProperty("sset", "sset <name> <number of results> <query> - search in the words index and save results in a set");
        help.setProperty("pset", "pset <name> print set");
        help.setProperty("vset", "vset <vector reader name> <set name> <vector name> - convert a set into a vector fetching vectors from the vector reader");

    }

}
