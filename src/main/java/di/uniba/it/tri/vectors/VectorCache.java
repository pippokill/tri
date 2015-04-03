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
package di.uniba.it.tri.vectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class VectorCache {

    private static final String ID = UUID.randomUUID().toString().replace("-", "");

    private LoadingCache<String, Vector> vectorCache;

    private int cacheSize = 50000;

    private IndexWriter writer;

    private DirectoryReader dirReader;

    private IndexSearcher searcher;

    private final int dimension;

    public VectorCache(int dimension, int cacheSize) throws IOException {
        this.dimension = dimension;
        this.cacheSize = cacheSize;
        this.vectorCache = CacheBuilder.newBuilder()
                .maximumSize(this.cacheSize)
                .build(
                        new CacheLoader<String, Vector>() {
                            @Override
                            public Vector load(String key) throws IOException {
                                return getVectorFromIndex(key);
                            }

                        });
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, new KeywordAnalyzer());
        writer = new IndexWriter(FSDirectory.open(new File("./VC_" + ID)), iwc);
        dirReader = DirectoryReader.open(writer, true);
        searcher = new IndexSearcher(dirReader);
    }

    private Vector getVectorFromIndex(String key) throws IOException {
        checkSearcher();
        Query q = new TermQuery(new Term("key", key));
        TopDocs topDocs = searcher.search(q, 1);
        Vector v = null;
        if (topDocs.scoreDocs.length > 0) {
            Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
            v = decodeVector(doc.getField("vector").binaryValue().bytes);
        }
        return v;
    }

    private void storeVector(String key, Vector vector) throws IOException {
        Document docv = new Document();
        docv.add(new StringField("key", key, Field.Store.NO));
        docv.add(getBinaryField("vector", encodeVector(vector)));
        writer.addDocument(docv);
    }

    private Field getBinaryField(String name, byte[] bytes) {
        FieldType binType = new FieldType();
        binType.setDocValueType(FieldInfo.DocValuesType.BINARY);
        binType.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        binType.setStoreTermVectorOffsets(false);
        binType.setStoreTermVectorPayloads(false);
        binType.setStoreTermVectorPositions(false);
        binType.setStoreTermVectors(false);
        binType.setStored(true);
        binType.setTokenized(false);
        return new Field(name, bytes, binType);
    }

    private byte[] encodeVector(Vector vector) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(vector.getDimension());
        DataOutputStream outstream = new DataOutputStream(byteStream);
        vector.writeToStream(outstream);
        outstream.flush();
        return byteStream.toByteArray();
    }

    private synchronized void checkSearcher() throws IOException {
        DirectoryReader newDirReader = DirectoryReader.openIfChanged(dirReader);
        if (newDirReader != null) {
            dirReader = newDirReader;
            searcher = new IndexSearcher(dirReader);
        }
    }

    private Vector decodeVector(byte[] bytes) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        DataInputStream instream = new DataInputStream(byteStream);
        float[] v = new float[dimension];
        for (int i = 0; i < v.length; i++) {
            v[i] = Float.intBitsToFloat(instream.readInt());
        }
        byteStream.close();
        return new RealVector(v);
    }

    public void addVector(String key, Vector vector) throws IOException {
        storeVector(key, vector);
        vectorCache.invalidate(key);
    }

    public Vector getVector(String key) throws IOException, ExecutionException {
        return vectorCache.get(key);
    }

    public ObjectVector getObjectVector(String key) throws IOException, ExecutionException {
        Vector vector = getVector(key);
        return new ObjectVector(key, vector);
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getDimension() {
        return dimension;
    }

}
