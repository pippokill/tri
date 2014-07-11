/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    public FileVectorReader(File inputFile) {
        this.inputFile = inputFile;
    }

    public void init() throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        logger.log(Level.INFO, "Init vector store: {0}", inputFile.getAbsolutePath());
        Properties props = VectorStoreUtils.readHeader(inputStream.readUTF());
        dimension = Integer.parseInt(props.getProperty("-dim"));
        inputStream.close();
    }

    @Override
    public void close() throws IOException {

    }

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

    @Override
    public Iterator<ObjectVector> getAllVectors() throws IOException {
        return new FileVectorIterator(inputFile);
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    public static class FileVectorIterator implements Iterator<ObjectVector> {

        private final File file;

        private DataInputStream inputStream;

        private int dimension;

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
