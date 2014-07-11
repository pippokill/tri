/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.vectors;

import java.util.Comparator;

/**
 *
 * @author pierpaolo
 */
public class ReverseObjectVectorComparator implements Comparator<ObjectVector> {

    @Override
    public int compare(ObjectVector o1, ObjectVector o2) {
        return Double.compare(o2.getScore(), o1.getScore());
    }

}
