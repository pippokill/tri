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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class FileVectorReader implements VectorReader {

    private static final Logger logger = Logger.getLogger(MemoryVectorReader.class.getName());

    private final File inputFile;

    private int dimension;

    /**
     *
     * @param inputFile
     */
    public FileVectorReader(File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     *
     * @throws IOException
     */
    public void init() throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        logger.log(Level.INFO, "Init vector store: {0}", inputFile.getAbsolutePath());
        Properties props = VectorStoreUtils.readHeader(inputStream.readUTF());
        dimension = Integer.parseInt(props.getProperty("-dim"));
        inputStream.close();
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     */
    @Override
    public Vector getVector(String key) throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        inputStream.readUTF();
        while (inputStream.available() > 0) {
            String fkey = inputStream.readUTF();
            if (fkey.equals(key)) {
                Vector vector = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                vector.readFromStream(inputStream);
                inputStream.close();
                return vector;
            } else {
                inputStream.skipBytes(VectorFactory.getByteSize(VectorType.REAL, dimension));
            }
        }
        inputStream.close();
        return null;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public Iterator<String> getKeys() throws IOException {
        Set<String> keySet = new HashSet<>();
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        inputStream.readUTF();
        while (inputStream.available() > 0) {
            String fkey = inputStream.readUTF();
            keySet.add(fkey);
            inputStream.skipBytes(VectorFactory.getByteSize(VectorType.REAL, dimension));
        }
        inputStream.close();
        return keySet.iterator();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public Iterator<ObjectVector> getAllVectors() throws IOException {
        return new FileVectorIterator(inputFile);
    }

    /**
     *
     * @return
     */
    @Override
    public int getDimension() {
        return this.dimension;
    }

    /**
     *
     */
    public static class FileVectorIterator implements Iterator<ObjectVector> {

        private final File file;

        private DataInputStream inputStream;

        private int dimension;

        /**
         *
         * @param file
         */
        public FileVectorIterator(File file) {
            this.file = file;
            try {
                this.inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                Properties props = VectorStoreUtils.readHeader(inputStream.readUTF());
                dimension = Integer.parseInt(props.getProperty("-dim"));
            } catch (IOException ex) {
                Logger.getLogger(FileVectorReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public boolean hasNext() {
            try {
                boolean hasNext = this.inputStream.available() > 0;
                if (!hasNext) {
                    this.inputStream.close();
                }
                return hasNext;
            } catch (IOException ex) {
                Logger.getLogger(FileVectorReader.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        @Override
        public ObjectVector next() {
            try {
                String key = this.inputStream.readUTF();
                Vector vector = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                vector.readFromStream(inputStream);
                return new ObjectVector(key, vector);
            } catch (IOException ex) {
                Logger.getLogger(FileVectorReader.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
