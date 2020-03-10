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

package di.uniba.it.tri.test;

import di.uniba.it.tri.space.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.MemoryVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class TestReadStore {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            VectorReader reader=new MemoryVectorReader(new File("/media/pierpaolo/KINGSTON/semeval/tri_pos/sv_2.v"));
            reader.init();
            Vector vector = reader.getVector("attack");
            //System.out.println(vector.toString());
            List<ObjectVector> nearestVectors = TemporalSpaceUtils.getNearestVectors(reader, vector, 50);
            for (ObjectVector ov:nearestVectors) {
                System.out.println(ov);
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(TestReadStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
