/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.util.Comparator;

/**
 *
 * @author pierpaolo
 */
public class ChangePointLabelComparator implements Comparator<ChangePoint> {

    @Override
    public int compare(ChangePoint arg0, ChangePoint arg1) {
        return arg0.getLabel().compareTo(arg1.getLabel());
    }
    
}
