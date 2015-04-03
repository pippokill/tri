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
package di.uniba.it.tri.occ;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import di.uniba.it.tri.extractor.Extractor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author pierpaolo
 */
public class BuildOccurrence {

    private int winsize = 5;

    private File outputDir = new File("./");

    private static final Logger logger = Logger.getLogger(BuildOccurrence.class.getName());

    private Extractor extractor;

    private String filenameRegExp = "^.+$";

    /**
     * Get the RegExp used to fetch files
     *
     * @return The RegExp
     */
    public String getFilenameRegExp() {
        return filenameRegExp;
    }

    /**
     * Set the RegExp used to fetch files
     *
     * @param filenameRegExp The RegExp
     */
    public void setFilenameRegExp(String filenameRegExp) {
        this.filenameRegExp = filenameRegExp;
    }

    /**
     * Get the window size
     *
     * @return The window size
     */
    public int getWinsize() {
        return winsize;
    }

    /**
     * Set the window size
     *
     * @param winsize The window size
     */
    public void setWinsize(int winsize) {
        this.winsize = winsize;
    }

    /**
     * Get the output directory
     *
     * @return The output directory
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Set the output directory
     *
     * @param outputDir The output directory
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Get the extractor
     *
     * @return The extractor
     */
    public Extractor getExtractor() {
        return extractor;
    }

    /**
     * Set the extractor
     *
     * @param extractor The extractor
     */
    public void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }

    private Map<String, Multiset<String>> count(File startingDir, int year) throws IOException {
        Map<String, Multiset<String>> map = new HashMap<>();
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().matches(filenameRegExp) && file.getName().lastIndexOf("_") > -1 && file.getName().endsWith(String.valueOf(year))) {
                logger.log(Level.INFO, "Working file: {0}", file.getName());
                StringReader reader = extractor.extract(file);
                List<String> tokens = getTokens(reader);
                for (int i = 0; i < tokens.size(); i++) {
                    int start = Math.max(0, i - winsize);
                    int end = Math.min(tokens.size() - 1, i + winsize);
                    for (int j = start; j < end; j++) {
                        if (i != j) {
                            Multiset<String> multiset = map.get(tokens.get(i));
                            if (multiset == null) {
                                multiset = HashMultiset.create();
                                map.put(tokens.get(i), multiset);
                            }
                            multiset.add(tokens.get(j));
                        }
                    }
                }
            }
        }
        return map;
    }

    private List<String> getTokens(Reader reader) throws IOException {
        List<String> tokens = new ArrayList<>();
        Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
        TokenStream tokenStream = analyzer.tokenStream("text", reader);
        tokenStream.reset();
        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            String[] split = token.split("'");
            if (split.length == 1) {
                tokens.add(token);
            } else {
                int max = 0;
                int index = 0;
                for (int i = 0; i < split.length; i++) {
                    if (split[i].length() > max) {
                        max = split[i].length();
                        index = i;
                    }
                }
                tokens.add(split[index]);
            }
        }
        tokenStream.end();
        return tokens;
    }

    /**
     * Build the co-occurrences matrix
     *
     * @param startingDir The corpus directory containing files with year
     * metadata
     * @throws Exception
     */
    public void process(File startingDir) throws Exception {
        logger.log(Level.INFO, "Starting dir: {0}", startingDir.getAbsolutePath());
        logger.log(Level.INFO, "Output dir: {0}", outputDir.getAbsolutePath());
        logger.log(Level.INFO, "Window size: {0}", winsize);
        File[] listFiles = startingDir.listFiles();
        int minYear = Integer.MAX_VALUE;
        int maxYear = -Integer.MAX_VALUE;
        for (File file : listFiles) {
            int i = file.getName().lastIndexOf("_");
            if (i > -1 && file.getName().substring(0, i).matches(filenameRegExp)) {
                //fix year to consider only the last 4 chars
                //old year int year = Integer.parseInt(file.getName().substring(i + 1));
                int year = Integer.parseInt(file.getName().substring(file.getName().length() - 4, file.getName().length()));
                if (year < minYear) {
                    minYear = year;
                }
                if (year > maxYear) {
                    maxYear = year;
                }
            }
        }
        logger.log(Level.INFO, "Form year: {0}", minYear);
        logger.log(Level.INFO, "To year: {0}", maxYear);
        for (int k = minYear; k <= maxYear; k++) {
            logger.log(Level.INFO, "Counting year: {0}", k);
            Map<String, Multiset<String>> count = count(startingDir, k);
            if (!count.isEmpty()) {
                save(count, k);
            }
        }
    }

    private void save(Map<String, Multiset<String>> count, int year) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/count_" + year));
        Iterator<String> keys = count.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            writer.append(key);
            Set<Multiset.Entry<String>> entrySet = count.get(key).entrySet();
            for (Entry<String> entry : entrySet) {
                writer.append("\t").append(entry.getElement()).append("\t").append(String.valueOf(entry.getCount()));
            }
            writer.newLine();
        }
        writer.close();
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("c", true, "The corpus directory containing files with year metadata")
                .addOption("o", true, "Output directory where output will be stored")
                .addOption("w", true, "The window size used to compute the co-occurrences (optional, default 5)")
                .addOption("e", true, "The class used to extract the content from files")
                .addOption("r", true, "Regular expression used to fetch files (optional, default \".+\")");
    }

    /**
     * Build the co-occurrences matrix given the set of files with year metadata
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("c") && cmd.hasOption("o") && cmd.hasOption("e")) {
                try {
                    Extractor extractor = (Extractor) Class.forName("di.uniba.it.tri.extractor." + cmd.getOptionValue("e")).newInstance();
                    BuildOccurrence builder = new BuildOccurrence();
                    builder.setOutputDir(new File(cmd.getOptionValue("o")));
                    builder.setWinsize(Integer.parseInt(cmd.getOptionValue("w", "5")));
                    builder.setExtractor(extractor);
                    builder.setFilenameRegExp(cmd.getOptionValue("r", "^.+$"));
                    builder.process(new File(cmd.getOptionValue("c")));
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build the co-occurrences matrix given the set of files with year metadata", options, true);
            }
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

}
