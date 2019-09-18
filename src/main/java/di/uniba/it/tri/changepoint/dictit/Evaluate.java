/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.dictit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author pierpaolo
 */
public class Evaluate {

    private static final Logger LOG = Logger.getLogger(Evaluate.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("g", true, "Gold standard")
                .addOption("d", true, "List of corpus lemmas")
                .addOption("s", true, "System output")
                .addOption("b", true, "Begin (1900)")
                .addOption("e", true, "End (2009)")
                .addOption("p", true, "Period length (5)")
                .addOption("f", false, "Filter single CP")
                .addOption("i", false, "Evaluate only intersection");
    }

    private static int check(List<Integer> list, int y, int periodLength) {
        int idx = -1;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            int d = Math.abs(y - list.get(i));
            if (d <= periodLength) {
                if (d < min) {
                    min = d;
                    idx = i;
                }
            }
        }
        return idx;
    }

    private static double[] evaluate(Map<String, List<Integer>> gold, File systemFile, int periodLength) throws IOException {
        double c = 0;
        double p = 0;
        double r = 0;
        Map<String, List<Integer>> system = DictitUtils.load(systemFile);
        System.err.println("System keys: " + system.size());
        for (String key : gold.keySet()) {
            List<Integer> goldCP = new ArrayList(gold.get(key));
            r += goldCP.size();
            List<Integer> sysCP = null;
            if (system.containsKey(key)) {
                sysCP = new ArrayList<>(system.get(key));
                if (!goldCP.isEmpty()) {
                    for (int j = sysCP.size() - 1; j >= 0; j--) {
                        int idx = check(goldCP, sysCP.get(j), periodLength);
                        if (idx > -1) {
                            c++;
                            sysCP.remove(j);
                            goldCP.remove(idx);
                        }
                    }
                }
            }
        }
        for (List<Integer> v : system.values()) {
            p += v.size();
        }
        System.err.println(p + "\t" + r + "\t" + c);
        p = c / p;
        r = c / r;
        return new double[]{p, r};
    }

    private static double[] evaluateInt(Map<String, List<Integer>> gold, File systemFile, int periodLength) throws IOException {
        double c = 0;
        double p = 0;
        double r = 0;
        Map<String, List<Integer>> system = DictitUtils.load(systemFile);
        for (String key : gold.keySet()) {
            if (system.containsKey(key)) {
                List<Integer> goldCP = new ArrayList(gold.get(key));
                r += goldCP.size();
                List<Integer> sysCP = null;
                sysCP = new ArrayList<>(system.get(key));
                p += sysCP.size();
                if (!goldCP.isEmpty()) {
                    for (int j = sysCP.size() - 1; j >= 0; j--) {
                        int idx = check(goldCP, sysCP.get(j), periodLength);
                        if (idx > -1) {
                            c++;
                            sysCP.remove(j);
                            goldCP.remove(idx);
                        }
                    }
                }
            }
        }
        System.err.println(p + "\t" + r + "\t" + c);
        p = c / p;
        r = c / r;
        return new double[]{p, r};
    }

    private static double getF(double p, double r) {
        return 2 * p * r / (p + r);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("g") && cmd.hasOption("s")) {
                try {
                    Map<String, List<Integer>> gold = DictitUtils.load(new File(cmd.getOptionValue("g")));
                    int begin = Integer.parseInt(cmd.getOptionValue("b", "1900"));
                    int end = Integer.parseInt(cmd.getOptionValue("e", "2009"));
                    int pl = Integer.parseInt(cmd.getOptionValue("p", "5"));
                    int pk = pl;
                    gold = DictitUtils.filterByYear(gold, begin, end);
                    if (cmd.hasOption("f")) {
                        gold = DictitUtils.filterSingleCP(gold);
                    }
                    System.err.println("Gold keys: " + gold.size());
                    Set<String> lemmas = new HashSet(gold.keySet());
                    if (cmd.hasOption("d")) {
                        lemmas.addAll(DictitUtils.loadDictLemmas(new File(cmd.getOptionValue("d"))));
                        System.err.println("Corpus keys: " + lemmas.size());
                        Set<String> gkeys = gold.keySet();
                        for (String gkey : gkeys) {
                            if (!lemmas.contains(gkey)) {
                                gold.remove(gkey);
                            }
                        }
                        System.err.println("Gold keys (int. corpus): " + gold.size());
                    }
                    List<Double> flist = new ArrayList();
                    System.out.println();
                    while (begin + pk < end) {
                        double[] m;
                        if (cmd.hasOption("i")) {
                            m = evaluateInt(gold, new File(cmd.getOptionValue("s")), pk);
                        } else {
                            m = evaluate(gold, new File(cmd.getOptionValue("s")), pk);
                        }
                        System.out.println(m[0] + "\t" + m[1] + "\t" + getF(m[0], m[1]));
                        flist.add(getF(m[0], m[1]));
                        pk += pl;
                    }
                    System.out.println();
                    System.out.println("F-measure");
                    for (Double d : flist) {
                        System.out.append(d.toString()).append("\t");
                    }
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Evaluate system output", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
