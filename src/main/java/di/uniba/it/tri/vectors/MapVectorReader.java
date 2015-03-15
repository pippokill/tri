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

    /**
     *
     * @param memory
     */
    public MapVectorReader(Map<String, Vector> memory) {
        this.memory = memory;
    }

    /**
     *
     * @throws IOException
     */
    public void init() throws IOException {
        Iterator<Vector> iterator = memory.values().iterator();
        if (iterator.hasNext()) {
            this.dimension=iterator.next().getDimension();
        }
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     */
    public Vector getVector(String key) throws IOException {
        return memory.get(key);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public Iterator<String> getKeys() throws IOException {
        return memory.keySet().iterator();
    }

    /**
     *
     * @throws IOException
     */
    public void close() throws IOException {
        memory.clear();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    public Iterator<ObjectVector> getAllVectors() throws IOException {
        return new MemoryVectorIterator(memory);
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
    public final class MemoryVectorIterator implements Iterator<ObjectVector> {

        private final Map<String, Vector> memory;

        private final Iterator<String> internalIterator;

        /**
         *
         * @param memory
         */
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
