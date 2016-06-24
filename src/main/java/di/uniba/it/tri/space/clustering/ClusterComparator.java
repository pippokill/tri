/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.space.clustering;

import di.uniba.it.tri.vectors.ObjectVector;
import java.util.Comparator;

/**
 *
 * @author pierpaolo
 */
public class ClusterComparator implements Comparator<ObjectVector> {

    @Override
    public int compare(ObjectVector o1, ObjectVector o2) {
        return Integer.compare(o1.getCluster(), o2.getCluster());
    }

}
