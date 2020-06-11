/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author pierpaolo
 */
public class RankWordsCPD {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "CPD output file")
                .addOption("o", true, "Output directory");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                Map<String, List<ScoredWord>> map = new HashMap<>();
                BufferedReader in = new BufferedReader(new FileReader(cmd.getOptionValue("i")));
                while (in.ready()) {
                    String[] split = in.readLine().split("\t");
                    for (int i = 1; i < split.length; i = i + 2) {
                        List<ScoredWord> list = map.get(split[i]);
                        if (list == null) {
                            list = new ArrayList<>();
                            map.put(split[i], list);
                        }
                        list.add(new ScoredWord(split[0], Double.parseDouble(split[i + 1])));
                    }
                }
                in.close();
                for (String y : map.keySet()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o") + "/CPD_rank_" + y));
                    List<ScoredWord> list = map.get(y);
                    Collections.sort(list, Collections.reverseOrder());
                    for (ScoredWord w : list) {
                        writer.append(w.getWord()).append("\t").append(String.valueOf(w.getScore()));
                        writer.newLine();
                    }
                    writer.close();
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Rank words", options, true);
            }
        } catch (Exception ex) {
            Logger.getLogger(RankWordsCPD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class ScoredWord implements Comparable<ScoredWord> {

        private String word;

        private double score;

        public ScoredWord(String word, double score) {
            this.word = word;
            this.score = score;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + Objects.hashCode(this.word);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ScoredWord other = (ScoredWord) obj;
            if (!Objects.equals(this.word, other.word)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ScoredWord{" + "word=" + word + ", score=" + score + '}';
        }

        @Override
        public int compareTo(ScoredWord o) {
            return Double.compare(score, o.score);
        }
    }

}
