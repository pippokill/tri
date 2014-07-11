/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package di.uniba.it.tri.test;

import di.uniba.it.tri.TemporalSpaceUtils;
import di.uniba.it.tri.vectors.FileVectorReader;
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
            VectorReader reader=new FileVectorReader(new File("/media/pierpaolo/storage/data/gutenberg/vectors_ita/count_1518.vectors"));
            reader.init();
            Vector vector = reader.getVector("rotte");
            System.out.println(vector.toString());
            List<ObjectVector> nearestVectors = TemporalSpaceUtils.getNearestVectors(reader, vector, 10);
            for (ObjectVector ov:nearestVectors) {
                System.out.println(ov);
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(TestReadStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
