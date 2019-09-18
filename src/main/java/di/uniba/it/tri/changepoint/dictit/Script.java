/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.dictit;

/**
 *
 * @author pierpaolo
 */
public class Script {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String systemfile="/home/pierpaolo/Scaricati/temp/tri_eval/ts_union_CPD_coll";
        String cmd="-g /home/pierpaolo/dataset/tri_dictit/dictit.gold -s "+systemfile;
        Evaluate.main(cmd.split(" "));
        Evaluate.main((cmd+" -f").split(" "));
    }
    
}
