/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
