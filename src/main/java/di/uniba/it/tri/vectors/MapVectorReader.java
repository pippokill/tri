/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.vectors;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class MapVectorReader implements VectorReader {

    private final Map<String, Vector> memory;

    private static final Logger logger = Logger.getLogger(MapVectorReader.class.getName());
    
    private int dimension;

    public MapVectorReader(Map<String, Vector> memory) {
        this.memory = memory;
    }

    public void init() throws IOException {
        Iterator<Vector> iterator = memory.values().iterator();
        if (iterator.hasNext()) {
            this.dimension=iterator.next().getDimension();
        }
    }

    public Vector getVector(String key) throws IOException {
        return memory.get(key);
    }

    public Iterator<String> getKeys() throws IOException {
        return memory.keySet().iterator();
    }

    public void close() throws IOException {
        memory.clear();
    }

    @Override
    public Iterator<ObjectVector> getAllVectors() throws IOException {
        return new MemoryVectorIterator(memory);
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    public final class MemoryVectorIterator implements Iterator<ObjectVector> {

        private final Map<String, Vector> memory;

        private final Iterator<String> internalIterator;

        public MemoryVectorIterator(Map<String, Vector> memory) {
            this.memory = memory;
            this.internalIterator = this.memory.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.internalIterator.hasNext();
        }

        @Override
        public ObjectVector next() {
            String key = this.internalIterator.next();
            Vector v = memory.get(key);
            return new ObjectVector(key, v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
