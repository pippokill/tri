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
package di.uniba.it.tri.api;

import di.uniba.it.tri.ClusterComparator;
import di.uniba.it.tri.Clusters;
import di.uniba.it.tri.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.MemoryVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author pierpaolo
 */
public class Tri {

    private File mainDir;

    private IndexReader reader;

    private static final String ELEMENTAL_NAME = "*elemental*";

    private static final QueryParser parser = new QueryParser("word", new StandardAnalyzer(CharArraySet.EMPTY_SET));

    private final Map<String, VectorReader> stores = new HashMap<>();

    private final Map<String, Vector> vectors = new HashMap<>();

    private final Map<String, Set<String>> setmap = new HashMap<>();

    public File getMainDir() {
        return mainDir;
    }

    public Map<String, VectorReader> getStores() {
        return stores;
    }

    public Map<String, Vector> getVectors() {
        return vectors;
    }

    public Map<String, Set<String>> getSetmap() {
        return setmap;
    }

    public boolean readerReay() {
        return reader != null;
    }

    //index words from elemental file
    public void indexelem() throws Exception {
        if (mainDir != null) {
            reader = TemporalSpaceUtils.indexElemental(mainDir);
        } else {
            throw new Exception("Main dir not set");
        }
    }

    //index a vector file
    public void indexFile(File file) throws Exception {
        if (file.isFile()) {
            if (reader != null) {
                reader.close();
            }
            reader = TemporalSpaceUtils.index(file);
        } else {
            throw new Exception("no valid file: " + file.getAbsolutePath());
        }
    }

    public void indexFileInMemory(String vectorStoreName) throws Exception {
        VectorReader vr = stores.get(vectorStoreName);
        if (vr == null) {
            throw new Exception("no vector store for: " + vectorStoreName);
        } else {
            if (reader != null) {
                reader.close();
            }
            reader = TemporalSpaceUtils.index(vr);
        }
    }

    //seach in loaded index
    public List<TriResultObject> search(String strQuery, int n) throws Exception {
        if (reader == null) {
            throw new Exception("no index in memory");
        } else {
            Query query = parser.parse(strQuery);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, n);
            List<TriResultObject> list = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                list.add(new TriResultObject(searcher.doc(scoreDoc.doc).get("word"), scoreDoc.score));
            }
            return list;
        }
    }

    //set main dir
    public void setMaindir(String mainDirname) throws Exception {
        File file = new File(mainDirname);
        if (file.isDirectory()) {
            this.mainDir = file;
        } else {
            throw new Exception("Not valid directory: " + mainDirname);
        }
    }

    //list available year
    public List<String> year(int startYear, int endYear) throws Exception {
        if (mainDir == null) {
            throw new Exception("Main dir not set");
        } else {
            List<String> availableYears = TemporalSpaceUtils.getAvailableYears(mainDir, startYear, endYear);
            Collections.sort(availableYears);
            List<String> years = new ArrayList<>();
            for (String year : availableYears) {
                years.add(year);
            }
            return years;
        }
    }

    //load a VectorReader
    public void load(String type, String name, String year) throws Exception {
        if (mainDir == null) {
            throw new Exception("Main dir not set");
        } else if (name == null) {
            loadVectorReader(type, ELEMENTAL_NAME, TemporalSpaceUtils.getElementalFile(mainDir));
        } else {
            loadVectorReader(type, name, TemporalSpaceUtils.getVectorFile(mainDir, year));
        }
    }

    //load a VectorReader
    public void fload(String type, String name, String vectorFilename) throws Exception {
        loadVectorReader(type, name, new File(vectorFilename));
    }

    public List<String> listStores() throws Exception {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = stores.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public List<String> listVectors() throws Exception {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = vectors.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public List<String> listSets() throws Exception {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = setmap.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    //Utils to load a VectorReader
    private void loadVectorReader(String type, String name, File file) throws Exception {
        VectorReader vr = null;
        if (type.equals("mem")) {
            vr = new MemoryVectorReader(file);
            vr.init();
            if (stores.containsKey(name)) {
                stores.get(name).close();
            }
            stores.put(name, vr);
        } else if (type.equals("file")) {
            vr = new FileVectorReader(file);
            vr.init();
            if (stores.containsKey(name)) {
                stores.get(name).close();
            }
            stores.put(name, vr);
        } else {
            throw new Exception("not valid vector reader type");
        }
    }

    public void clearStores() {
        stores.clear();
    }

    public void clearStore(String name) {
        stores.remove(name);
    }

    public void clearVectors() {
        vectors.clear();
    }

    public void clearVector(String name) {
        vectors.remove(name);
    }

    public void clearIndex() throws IOException {
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }

    public Set<String> add(String vectorstoreName, String resultVector, String[] vectors) throws Exception {
        VectorReader vr = stores.get(vectorstoreName);
        Set<String> addedVectors = new HashSet<>();
        if (vr != null) {
            Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
            for (String vector : vectors) {
                Vector wv = vr.getVector(vector);
                if (wv != null) {
                    v.superpose(wv, 1, null);
                    addedVectors.add(vector);
                }
            }
            v.normalize();
            this.vectors.put(resultVector, v);
        } else {
            throw new Exception("no stores for: " + vectorstoreName);
        }
        return addedVectors;
    }

    public Set<String> addv(String resultVector, String[] terms) throws Exception {
        Set<String> addedVectors = new HashSet<>();
        Vector nv = null;
        for (String term : terms) {
            Vector v = vectors.get(term);
            if (v != null) {
                if (nv == null) {
                    nv = VectorFactory.createZeroVector(VectorType.REAL, v.getDimension());
                }
                nv.superpose(v, 1, null);
                addedVectors.add(term);
            }
        }
        if (nv == null) {
            throw new Exception("Result vector is null");
        } else {
            nv.normalize();
            vectors.put(resultVector, nv);
            return addedVectors;
        }
    }

    public void get(String vectorstoreName, String vectorName, String term) throws Exception {
        VectorReader vr = stores.get(vectorstoreName);
        if (vr != null) {
            Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
            Vector wv = vr.getVector(term);
            if (wv != null) {
                v.superpose(wv, 1, null);
            } else {
                throw new Exception("no vector for: " + term);
            }
            v.normalize();
            vectors.put(vectorName, v);
        } else {
            throw new Exception("no stores for: " + vectorstoreName);
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

    public List<ObjectVector> near(String vectorstoreName, String vectorName, int n) throws Exception {
        VectorReader vr = stores.get(vectorstoreName);
        if (vr != null) {
            Vector v = vectors.get(vectorName);
            if (v != null) {
                List<ObjectVector> nv = TemporalSpaceUtils.getNearestVectors(vr, v, n);
                return nv;
            } else {
                throw new Exception("no vector for: " + vectorName);
            }
        } else {
            throw new Exception("vector reader not found: " + vectorstoreName);
        }
    }

    public double sim(String vectorName1, String vectorName2) throws Exception {
        Vector v1 = vectors.get(vectorName1);
        if (v1 == null) {
            throw new Exception("no vector for: " + vectorName1);
        }
        Vector v2 = vectors.get(vectorName2);
        if (v2 == null) {
            throw new Exception("no vector for: " + vectorName2);
        }
        return v1.measureOverlap(v2);
    }

    public void tri(String vectorstoreName, int start, int end) throws Exception {
        List<File> files = TemporalSpaceUtils.getFileTemporalRange(mainDir, start, end);
        VectorReader[] readers = new VectorReader[files.size()];
        for (int i = 0; i < files.size(); i++) {
            readers[i] = new FileVectorReader(files.get(i));
            readers[i].init();
        }
        VectorReader tir = TemporalSpaceUtils.combineAndBuildVectorReader(readers);
        tir.init();
        stores.put(vectorstoreName, tir);
        for (VectorReader r : readers) {
            r.close();
        }
    }

    public void ftri(String vectorstoreFilename, int start, int end) throws Exception {
        List<File> files = TemporalSpaceUtils.getFileTemporalRange(mainDir, start, end);
        VectorReader[] readers = new VectorReader[files.size()];
        for (int i = 0; i < files.size(); i++) {
            readers[i] = new FileVectorReader(files.get(i));
            readers[i].init();
        }
        TemporalSpaceUtils.combineAndSaveVectorReader(new File(vectorstoreFilename), readers);
        for (VectorReader r : readers) {
            r.close();
        }
    }

    public List<TriResultObject> compare(String vectorstoreName1, String vectorstoreName2, String vector1, String vector2, int n) throws Exception {
        VectorReader vr1 = stores.get(vectorstoreName1);
        if (vr1 == null) {
            throw new Exception("no valid store for: " + vectorstoreName1);
        }
        VectorReader vr2 = stores.get(vectorstoreName2);
        if (vr2 == null) {
            throw new Exception("no valid store for: " + vectorstoreName2);
        }
        Vector v1 = vectors.get(vector1);
        if (v1 == null) {
            throw new Exception("no vector for: " + vector1);
        }
        Vector v2 = vectors.get(vector2);
        if (v2 == null) {
            throw new Exception("no vector for: " + vector2);
        }
        List<ObjectVector> n1 = TemporalSpaceUtils.getNearestVectors(vr1, v1, n);
        List<ObjectVector> n2 = TemporalSpaceUtils.getNearestVectors(vr2, v2, n);
        int size = Math.min(n1.size(), n2.size());
        List<TriResultObject> list = new ArrayList<>(size);
        for (int k = 0; k < size; k++) {
            list.add(new TriResultObject(n1.get(k).getKey() + "\t" + n1.get(k).getScore() + " | "
                    + n2.get(k).getKey() + "\t" + n2.get(k).getScore(), 0));
        }
        return list;
    }

    public int count(String vectorstoreName) throws Exception {
        VectorReader vr = stores.get(vectorstoreName);
        if (vr == null) {
            throw new Exception("no valid store for: " + vectorstoreName);
        }
        return TemporalSpaceUtils.countVectors(vr);
    }

    public void cset(String setname) throws Exception {
        setmap.put(setname, new HashSet<String>());
    }

    public void rset(String setname, String[] elements) throws Exception {
        Set<String> set = setmap.get(setname);
        if (set != null) {
            for (String element : elements) {
                set.remove(element);
            }
        } else {
            throw new Exception("no set for: " + setname);
        }
    }

    public void dset(String setname) throws Exception {
        setmap.remove(setname);
    }

    public void aset(String setname, String[] elements) throws Exception {
        Set<String> set = setmap.get(setname);
        if (set != null) {
            for (String element : elements) {
                set.add(element);
            }
        } else {
            throw new Exception("no set for: " + setname);
        }
    }

    public List<TriResultObject> sset(String setname, String strQuery, int n) throws Exception {
        if (reader == null) {
            throw new Exception("no index in memory");
        } else {
            Set<String> set = setmap.get(setname);
            if (set == null) {
                throw new Exception("no set for: " + setname);
            }
            Query query = parser.parse(strQuery);
            List<TriResultObject> list = new ArrayList<>();
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, n);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                String word = searcher.doc(scoreDoc.doc).get("word");
                list.add(new TriResultObject(word, scoreDoc.score));
            }
            return list;
        }
    }

    public Set<String> getSet(String setname) throws Exception {
        Set<String> set = setmap.get(setname);
        if (set != null) {
            return set;
        } else {
            throw new Exception("no set for: " + setname);
        }
    }

    public void vset(String vectorstoreName, String setName, String vectorName) throws Exception {
        VectorReader vr = stores.get(vectorstoreName);
        if (vr == null) {
            throw new Exception("no stores for: " + vectorstoreName);
        }
        Set<String> set = setmap.get(setName);
        if (set == null) {
            throw new Exception("no set for: " + setName);
        }
        Vector v = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
        for (String word : set) {
            Vector wv = vr.getVector(word);
            if (wv != null) {
                v.superpose(wv, 1, null);
            } else {
                throw new Exception("no vector for: " + word);
            }
        }
        v.normalize();
        vectors.put(vectorName, v);
    }

    public List<ObjectVector> sims(String vectorstoreName1, String vectorstoreName2, int n, double min, double max) throws Exception {
        VectorReader vr1 = stores.get(vectorstoreName1);
        if (vr1 == null) {
            throw new Exception("no valid store for: " + vectorstoreName1);
        }
        VectorReader vr2 = stores.get(vectorstoreName2);
        if (vr2 == null) {
            throw new Exception("no valid store for: " + vectorstoreName2);
        }
        List<ObjectVector> sims = TemporalSpaceUtils.sims(vr1, vr2, n, min, max);
        return sims;
    }

    public List<TriResultObject> plotWord(String[] terms) throws Exception {
        if (mainDir == null) {
            throw new Exception("No main dir set");
        }
        List<String> availableYears = TemporalSpaceUtils.getAvailableYears(mainDir, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        Collections.sort(availableYears);
        //just read vector dimension
        VectorReader vrd = TemporalSpaceUtils.getVectorReader(mainDir, availableYears.get(0), false);
        vrd.init();
        int dimension = vrd.getDimension();
        vrd.close();
        List<Vector> precv = new ArrayList<>();
        for (int i = 0; i < terms.length; i++) {
            precv.add(VectorFactory.createZeroVector(VectorType.REAL, dimension));
        }
        List<TriResultObject> list = new ArrayList<>();
        for (String ys : availableYears) {
            VectorReader vr = TemporalSpaceUtils.getVectorReader(mainDir, ys, false);
            for (int i = 0; i < terms.length; i++) {
                Vector v = vr.getVector(terms[i]);
                if (v != null) {
                    Vector copy = precv.get(i).copy();
                    copy.superpose(v, 1, null);
                    copy.normalize();
                    list.add(new TriResultObject(ys + "\t" + terms[i], (float) copy.measureOverlap(precv.get(i))));
                    precv.get(i).superpose(v, 1, null);
                    precv.get(i).normalize();
                } else {
                    list.add(new TriResultObject(ys + "\t" + terms[i], -1));
                }
            }
            vr.close();
        }
        return list;
    }

    public List<TriResultObject> plotWords(String term1, String term2) throws Exception {
        List<String> availableYears = TemporalSpaceUtils.getAvailableYears(mainDir, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        Collections.sort(availableYears);
        //just read vector dimension
        VectorReader vrd = TemporalSpaceUtils.getVectorReader(mainDir, availableYears.get(0), false);
        vrd.init();
        int dimension = vrd.getDimension();
        vrd.close();
        Vector v1 = VectorFactory.createZeroVector(VectorType.REAL, dimension);
        Vector v2 = VectorFactory.createZeroVector(VectorType.REAL, dimension);
        List<TriResultObject> list = new ArrayList<>();
        for (String ys : availableYears) {
            VectorReader vr = TemporalSpaceUtils.getVectorReader(mainDir, ys, false);
            vr.init();
            Vector v = vr.getVector(term1);
            if (v != null) {
                v1.superpose(v, 1, null);
                v1.normalize();
            }
            v = vr.getVector(term2);
            if (v != null) {
                v2.superpose(v, 1, null);
                v2.normalize();
            }
            list.add(new TriResultObject(ys + "\t" + term1 + "-" + term2, (float) v1.measureOverlap(v2)));
            vr.close();
        }
        return list;
    }

    public void cluster(String vectorName, int k, String outFilename) throws Exception {
        VectorReader vr = stores.get(vectorName);
        if (vr != null) {
            List<ObjectVector> vectors = new ArrayList<>();
            Iterator<ObjectVector> allVectors = vr.getAllVectors();
            while (allVectors.hasNext()) {
                vectors.add(allVectors.next());
            }
            Clusters clusters = TemporalSpaceUtils.kMeansCluster(vr, vectors, k);
            int[] mappings = clusters.getClusterMappings();
            for (int i = 0; i < vectors.size(); i++) {
                vectors.get(i).setCluster(mappings[i]);
            }
            Collections.sort(vectors, new ClusterComparator());
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename));
            for (ObjectVector ov : vectors) {
                writer.append(ov.getKey()).append("\t").append(String.valueOf(ov.getCluster()));
                writer.newLine();
            }
            writer.newLine();
            Vector[] centroids = clusters.getCentroids();
            for (int i = 0; i < centroids.length; i++) {
                for (int j = 0; j < centroids.length; j++) {
                    if (i != j) {
                        writer.append(String.valueOf(i)).append("\t").append(String.valueOf(j)).append("\t")
                                .append(String.valueOf(centroids[i].measureOverlap(centroids[j])));
                        writer.newLine();
                    }
                }
            }
            writer.close();
        } else {
            throw new Exception("Vector reader " + vectorName + " not found");
        }

    }
}
