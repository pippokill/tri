/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.vectors.Vector;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class SimpleReaderTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 4) {
            try {
                SimpleGbooksTRIreader reader = new SimpleGbooksTRIreader(args[0]);
                Vector w1 = reader.getTRIVector(args[1], Integer.parseInt(args[2]));
                Vector w2 = reader.getTRIVector(args[3], Integer.parseInt(args[4]));
                if (w1 != null && w2 != null) {
                    System.out.println(w1.measureOverlap(w2));
                }
            } catch (IOException ex) {
                Logger.getLogger(SimpleReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.err.println("Usage: <dir> <word 1> <year 1> <word 2> <year 2>");
        }
    }

}
