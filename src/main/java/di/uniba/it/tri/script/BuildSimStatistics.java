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
package di.uniba.it.tri.script;

import di.uniba.it.tri.TemporalSpaceUtils;
import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.api.TriResultObject;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class BuildSimStatistics {

    /**
     * base_dir_1 output_file_1
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Tri api = new Tri();
            api.setMaindir(args[0]);
            api.load("file", null, "-1");
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            VectorReader evr = api.getStores().get(Tri.ELEMENTAL_NAME);
            Iterator<String> keys = evr.getKeys();
            int c = 0;
            System.out.println();
            List<String> availableYears = TemporalSpaceUtils.getAvailableYears(new File(args[0]), -Integer.MAX_VALUE, Integer.MAX_VALUE);
            Collections.sort(availableYears);
            Map<String, VectorReader> vrmap = new HashMap<>();
            //just read vector dimension
            for (String year : availableYears) {
                System.out.println("Loading " + year);
                VectorReader vrd = TemporalSpaceUtils.getVectorReader(new File(args[0]), year, true);
                vrd.init();
                vrmap.put(year, vrd);
            }
            int dimension = evr.getDimension();
            long time = System.currentTimeMillis();
            while (keys.hasNext()) {
                String key = keys.next();
                Vector precv = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                List<TriResultObject> list = new ArrayList<>();
                for (String ys : availableYears) {
                    VectorReader vr = vrmap.get(ys);
                    Vector v = vr.getVector(key);
                    if (v != null) {
                        Vector copy = precv.copy();
                        copy.superpose(v, 1, null);
                        copy.normalize();
                        list.add(new TriResultObject(ys + "\t" + key, (float) copy.measureOverlap(precv)));
                        precv.superpose(v, 1, null);
                        precv.normalize();
                    } else {
                        list.add(new TriResultObject(ys + "\t" + key, -1));
                    }
                }
                writer.append(key);
                list.remove(0);
                for (TriResultObject r : list) {
                    if (r.getScore() >= 0) {
                        writer.append("\t").append(String.valueOf(r.getScore()));
                    } else {
                        writer.append("\t").append(String.valueOf(0f));
                    }
                }
                writer.newLine();
                c++;
                if (c % 10000 == 0) {
                    System.out.println("Processed " + c + " words\t" + ((System.currentTimeMillis() - time) / 100) + " sec.");
                    time = System.currentTimeMillis();
                }
            }
            writer.close();
        } catch (Exception ex) {
            Logger.getLogger(BuildSimStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
