/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri;

import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.MapVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.ReverseObjectVectorComparator;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class TemporalSpaceUtils {

    public static Map<String, Vector> combineSpaces(Map<String, Vector>... spaces) {
        Map<String, Vector> newSpace = new HashMap<>();
        for (Map<String, Vector> space : spaces) {
            Iterator<String> iterator = space.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Vector v = space.get(key);
                Vector nw = newSpace.get(key);
                if (nw != null) {
                    nw.superpose(v, 1, null);
                } else {
                    newSpace.put(key, v);
                }
            }
        }
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return newSpace;
    }

    public static Map<String, Vector> combineVectorReader(VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        for (VectorReader reader : readers) {
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return newSpace;
    }

    public static VectorReader combineAndBuildVectorReader(VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        System.out.println();
        for (VectorReader reader : readers) {
            System.out.print(".");
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        System.out.println();
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return new MapVectorReader(newSpace);
    }

    public static void combineAndSaveVectorReader(File outputFile, VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        System.out.println();
        for (VectorReader reader : readers) {
            System.out.print(".");
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        System.out.println();
        int dimension = 0;
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            if (dimension == 0) {
                Vector v = iterator.next();
                dimension = v.getDimension();
                v.normalize();
            } else {
                iterator.next().normalize();
            }
        }
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, -1);
        outputStream.writeUTF(header);
        for (Entry<String, Vector> entry : newSpace.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().writeToStream(outputStream);
        }
        outputStream.close();
        newSpace.clear();
        newSpace = null;
    }

    public static List<ObjectVector> getNearestVectors(VectorReader store, String word, int n) throws IOException {
        Vector vector = store.getVector(word);
        if (vector != null) {
            return getNearestVectors(store, vector, n);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<ObjectVector> getNearestVectors(VectorReader store, Vector vector, int n) throws IOException {
        PriorityQueue<ObjectVector> queue = new PriorityQueue<>();
        Iterator<ObjectVector> allVectors = store.getAllVectors();
        while (allVectors.hasNext()) {
            ObjectVector ov = allVectors.next();
            double overlap = ov.getVector().measureOverlap(vector);
            ov.setScore(overlap);
            if (queue.size() <= n) {
                queue.offer(ov);
            } else {
                queue.poll();
                queue.offer(ov);
            }
        }
        queue.poll();
        List<ObjectVector> list = new ArrayList<>(queue);
        Collections.sort(list, new ReverseObjectVectorComparator());
        return list;
    }

    public static List<File> getFileTemporalRange(File startDir, int start, int end) {
        List<File> list = new ArrayList<>();
        File[] listFiles = startDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".vectors")) {
                String stringYear = file.getName().replaceAll("count_", "").replaceAll(".vectors", "");
                int year = Integer.parseInt(stringYear);
                if (year >= start && year <= end) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public static List<String> getAvailableYears(File startDir, int start, int end) {
        List<String> list = new ArrayList<>();
        File[] listFiles = startDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".vectors")) {
                String stringYear = file.getName().replaceAll("count_", "").replaceAll(".vectors", "");
                int year = Integer.parseInt(stringYear);
                if (year >= start && year <= end) {
                    list.add(stringYear);
                }
            }
        }
        return list;
    }

    public static IndexReader indexElemental(File stardDir) throws IOException {
        File elementalFile = getElementalFile(stardDir);
        return index(elementalFile);
    }

    public static IndexReader index(VectorReader vreader) throws IOException {
        Iterator<String> keys = vreader.getKeys();
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriterConfig iwconfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
        IndexWriter writer = new IndexWriter(ramDir, iwconfig);
        while (keys.hasNext()) {
            String word = keys.next();
            Document doc = new Document();
            doc.add(new Field("word", word, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
            writer.addDocument(doc);
        }
        writer.close();
        return IndexReader.open(ramDir);
    }

    public static IndexReader index(File file) throws IOException {
        FileVectorReader vreader = new FileVectorReader(file);
        vreader.init();
        IndexReader index = index(vreader);
        vreader.close();
        return index;
    }

    public static File getElementalFile(File startDir) {
        return new File(startDir.getAbsolutePath() + "/vectors.elemental");
    }

    public static File getVectorFile(File startDir, int year) {
        return getVectorFile(startDir, String.valueOf(year));
    }

    public static File getVectorFile(File startDir, String year) {
        return new File(startDir.getAbsolutePath() + "/count_" + year + ".vectors");
    }

    public static int countVectors(VectorReader reader) throws IOException {
        Iterator<ObjectVector> allVectors = reader.getAllVectors();
        int counter = 0;
        while (allVectors.hasNext()) {
            allVectors.next();
            counter++;
        }
        return counter;
    }

}
