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
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author pierpaolo
 */
public class SimpleGbooksTRIreader {

    private final String startingDir;

    public SimpleGbooksTRIreader(String startingDir) {
        this.startingDir = startingDir;
    }

    public Vector getTRIVector(String word, int year) throws IOException {
        Vector vector = null;
        File[] files = new File(startingDir).listFiles();
        for (File file : files) {
            String[] split = file.getName().split("_");
            if (split.length > 2) {
                int endYear = Integer.parseInt(split[split.length - 1]);
                if (year >= endYear) {
                    VectorReader vr = new FileVectorReader(file);
                    vr.init();
                    if (vector == null) {
                        vector = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
                    }
                    Vector wv = vr.getVector(word);
                    if (wv != null) {
                        vector.superpose(wv, 1, null);
                    }
                    vr.close();
                }
            }
        }
        if (vector != null && !vector.isZeroVector()) {
            vector.normalize();
        }
        return vector;
    }

}
