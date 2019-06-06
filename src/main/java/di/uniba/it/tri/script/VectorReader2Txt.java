/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.RealVector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class VectorReader2Txt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length==2) {
            try {
                BufferedWriter writer=new BufferedWriter(new FileWriter(args[1]));
                VectorReader vr=new FileVectorReader(new File(args[0]));
                vr.init();
                Iterator<ObjectVector> allVectors = vr.getAllVectors();
                while (allVectors.hasNext()) {
                    ObjectVector next = allVectors.next();
                    writer.append(next.getKey());
                    float[] coordinates = ((RealVector) next.getVector()).getCoordinates();
                    for (float v:coordinates) {
                        writer.append(" ").append(String.valueOf(v));
                    }
                    writer.newLine();
                }
                vr.close();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(VectorReader2Txt.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.err.println("This script needs two parameters: <vectors file> <output file>");
        }
    }
    
}
