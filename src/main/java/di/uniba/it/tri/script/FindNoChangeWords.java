/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */
package di.uniba.it.tri.script;

import di.uniba.it.tri.space.SpaceBuilder;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class finds words that don't change their semantic over time periods
 *
 * @author pierpaolo
 */
public class FindNoChangeWords {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("d", true, "Word spaces directory")
                .addOption("t", true, "Threshold")
                .addOption("o", true, "Output file");
    }

    private static List<WordEntry> computeWordMap(String dirname, double tr) throws IOException {
        Map<String, WordEntry> map = new HashMap<>();
        File dir = new File(dirname);
        File[] listFiles = dir.listFiles();
        Arrays.sort(listFiles);
        for (File file : listFiles) {
            if (file.isFile() && !file.getName().equals("vectors.elemental")) {
                System.out.println("Reading space: " + file);
                VectorReader vr = new FileVectorReader(file);
                vr.init();
                int i = 0;
                Iterator<ObjectVector> it = vr.getAllVectors();
                while (it.hasNext()) {
                    ObjectVector ov = it.next();
                    WordEntry we = map.get(ov.getKey());
                    if (we == null) {
                        we = new WordEntry(ov.getKey(), ov.getVector(), 0);
                        map.put(ov.getKey(), we);
                    } else {
                        Vector cv1 = we.getVector().copy(); //copy current vector to preserve it over time
                        Vector cv2 = ov.getVector().copy(); //copy word vector and sum it to previous time vector
                        cv2.superpose(cv1, 1, null);
                        cv1.normalize(); //normalize
                        cv2.normalize();
                        double score = cv1.measureOverlap(cv2);
                        if (score >= tr) { //if similarity is above the threshold then increment year
                            we.setYearCounter(we.getYearCounter() + 1);
                            if (we.getScore() < score) { //set max score
                                we.setScore(score);
                            }
                        } else {
                            we.setYearCounter(we.getYearCounter() - 1); //if similarity is below the threshold then increment year
                            if (we.getScore() < score) { //set max score
                                we.setScore(score);
                            }
                        }
                        we.getVector().superpose(ov.getVector(), 1, null); //accumulate vector over time
                    }
                    i++;
                    if (i % 1000 == 0) {
                        System.out.print(".");
                        if (i % 100000 == 0) {
                            System.out.println(i);
                        }
                    }
                }
                vr.close();
                System.out.println();
            }
        }
        List<WordEntry> list = new ArrayList<>();
        for (WordEntry e : map.values()) {
            if (e.getYearCounter() > 0) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("d") && cmd.hasOption("t")) {
                List<WordEntry> list = computeWordMap(cmd.getOptionValue("d"), Double.parseDouble(cmd.getOptionValue("t")));
                Collections.sort(list);
                if (cmd.hasOption("o")) {
                    System.out.println("Save words...");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    for (WordEntry e : list) {
                        writer.write(e.toString());
                        writer.newLine();
                    }
                    writer.close();
                } else {
                    for (WordEntry e : list) {
                        System.out.println(e);
                    }
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Extract the list of words that don't change meaning", options, true);
            }
        } catch (ParseException ex) {
            Logger.getLogger(SpaceBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FindNoChangeWords.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class WordEntry implements Comparable<WordEntry> {

        private String word;

        private int yearCounter = 0;

        private double score;

        private Vector vector;

        public WordEntry(String word, Vector vector) {
            this.word = word;
            this.vector = vector;
        }

        public WordEntry(String word, Vector vector, double score) {
            this.word = word;
            this.vector = vector;
            this.score = score;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getYearCounter() {
            return yearCounter;
        }

        public void setYearCounter(int yearCounter) {
            this.yearCounter = yearCounter;
        }

        public Vector getVector() {
            return vector;
        }

        public void setVector(Vector vector) {
            this.vector = vector;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        @Override
        public int compareTo(WordEntry o) {
            int comp = Integer.compare(o.getYearCounter(), getYearCounter());
            if (comp == 0) {
                return Double.compare(o.getScore(), score);
            } else {
                return comp;
            }
        }

        @Override
        public String toString() {
            return getWord() + "\t" + getYearCounter() + "\t" + getScore();
        }

    }

}
