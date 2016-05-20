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
package di.uniba.it.tri.shell;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.api.TriResultObject;
import di.uniba.it.tri.vectors.ObjectVector;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used as the wrapper to interpret and execute commands from the
 * shell
 *
 * @author pierpaolo
 */
public class CommandWrapper {

    private final Tri tri = new Tri();

    private final Properties help = new Properties();

    /**
     *
     */
    public CommandWrapper() {
        initHelp();
    }

    /**
     * Execute a command given the command line
     *
     * @param command The command line
     * @throws Exception
     */
    public void executeCommand(String command) throws Exception {
        if (command.matches("(^indexelem$)|(^indexelem\\s+.*$)")) {
            indexelem();
        } else if (command.matches("(^index$)|(^index\\s+.*$)")) {
            index(command);
        } else if (command.matches("(^search$)|(^search\\s+.*$)")) {
            search(command);
        } else if (command.matches("(^set$)|(^set\\s+.*$)")) {
            set(command);
        } else if (command.matches("(^year$)|(^year\\s+.*$)")) {
            year(command);
        } else if (command.matches("(^load$)|(^load\\s+.*$)")) {
            load(command);
        } else if (command.matches("(^fload$)|(^fload\\s+.*$)")) {
            fload(command);
        } else if (command.matches("(^list$)|(^list\\s+.*$)")) {
            list(command);
        } else if (command.matches("(^clear$)|(^clear\\s+.*$)")) {
            clear(command);
        } else if (command.matches("(^get$)|(^get\\s+.*$)")) {
            get(command);
        } else if (command.matches("(^add$)|(^add\\s+.*$)")) {
            add(command);
        } else if (command.matches("(^addv$)|(^addv\\s+.*$)")) {
            addv(command);
        } else if (command.matches("(^near$)|(^near\\s+.*$)")) {
            near(command);
        } else if (command.matches("(^sim$)|(^sim\\s+.*$)")) {
            sim(command);
        } else if (command.matches("(^tri$)|(^tri\\s+.*$)")) {
            tri(command);
        } else if (command.matches("(^ftri$)|(^ftri\\s+.*$)")) {
            ftri(command);
        } else if (command.matches("(^compare$)|(^compare\\s+.*$)")) {
            compare(command);
        } else if (command.matches("(^cset$)|(^cset\\s+.*$)")) {
            cset(command);
        } else if (command.matches("(^rset$)|(^rset\\s+.*$)")) {
            rset(command);
        } else if (command.matches("(^dset$)|(^dset\\s+.*$)")) {
            dset(command);
        } else if (command.matches("(^aset$)|(^aset\\s+.*$)")) {
            aset(command);
        } else if (command.matches("(^sset$)|(^sset\\s+.*$)")) {
            sset(command);
        } else if (command.matches("(^pset$)|(^pset\\s+.*$)")) {
            pset(command);
        } else if (command.matches("(^vset$)|(^vset\\s+.*$)")) {
            vset(command);
        } else if (command.matches("(^count$)|(^count\\s+.*$)")) {
            count(command);
        } else if (command.matches("(^sims$)|(^sims\\s+.*$)")) {
            sims(command);
        } else if (command.matches("(^plot$)|(^plot\\s+.*$)")) {
            plot(command);
        } else if (command.matches("(^cluster$)|(^cluster\\s+.*$)")) {
            cluster(command);
        } else if (command.matches("(^rmean$)|(^rmean\\s+.*$)")) {
            rmean(command);
        } else {
            throw new Exception("Unknown command: " + command);
        }
    }

    //index words from elemental file
    private void indexelem() throws Exception {
        tri.indexelem();
    }

    //index a vector file
    private void index(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            switch (split[1]) {
                case "file":
                    File file = new File(split[2]);
                    tri.indexFile(file);
                    break;
                case "mem":
                    tri.indexFileInMemory(split[2]);
                    break;
                default:
                    throw new Exception("index type error");
            }
        } else {
            throw new Exception("index syntax error");
        }
    }

    private void printTriResults(List<TriResultObject> list) {
        for (TriResultObject r : list) {
            TriShell.println(r.toConsoleString());
        }
    }

    //seach in loaded index
    private void search(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            if (!tri.readerReay()) {
                throw new Exception("no index in memory");
            } else {
                if (!split[1].matches("[0-9]+")) {
                    throw new Exception("no valid number of results");
                }
                StringBuilder qs = new StringBuilder();
                for (int i = 2; i < split.length; i++) {
                    qs.append(split[i]).append(" ");
                }
                List<TriResultObject> rs = tri.search(qs.toString(), Integer.parseInt(split[1]));
                printTriResults(rs);
            }
        } else {
            throw new Exception("search syntax error");
        }
    }

    //set main dir
    private void set(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 1) {
            tri.setMaindir(split[1]);
        } else {
            throw new Exception("set syntax error");
        }
    }

    //list available year
    private void year(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 1) {
            List<String> availableYears = tri.year(-Integer.MAX_VALUE, Integer.MAX_VALUE);
            Collections.sort(availableYears);
            for (String year : availableYears) {
                TriShell.print(year);
                TriShell.print(" ");
            }
            TriShell.println("");
        } else if (split.length > 2) {
            int start = Integer.parseInt(split[1]);
            int end = Integer.parseInt(split[2]);
            List<String> availableYears = tri.year(start, end);
            Collections.sort(availableYears);
            for (String year : availableYears) {
                TriShell.print(year);
                TriShell.print(" ");
            }
            TriShell.println("");

        } else {
            throw new Exception("year syntax error");
        }

    }

    //load a VectorReader
    private void load(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 2) {
            tri.load(split[1], null, null);
        } else if (split.length > 3) {
            if (split[3].matches("[0-9]+")) {
                tri.load(split[1], split[2], split[3]);
            } else {
                throw new Exception("not valid year");
            }
        } else {
            throw new Exception("load syntax error");
        }

    }

    //load a VectorReader
    private void fload(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            tri.fload(split[1], split[2], split[3]);
        } else {
            throw new Exception("load syntax error");
        }

    }

    private void list(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 2) {
            switch (split[1]) {
                case "stores":
                    List<String> list = tri.listStores();
                    for (String e : list) {
                        TriShell.println(e);
                    }
                    TriShell.println(list.size() + " stores loaded.");
                    break;
                case "vectors":
                    list = tri.listVectors();
                    for (String e : list) {
                        TriShell.println(e);
                    }
                    TriShell.println(list.size() + " vectors loaded.");
                    break;
                case "sets":
                    list = tri.listSets();
                    for (String e : list) {
                        TriShell.println(e);
                    }
                    TriShell.println(list.size() + " sets loaded.");
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        }

    }

    private void clear(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 2) {
            switch (split[1]) {
                case "stores":
                    tri.clearStores();
                    break;
                case "vectors":
                    tri.clearVectors();
                    break;
                case "index":
                    tri.clearIndex();
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        } else if (split.length > 2) {
            switch (split[1]) {
                case "stores":
                    tri.clearStore(split[2]);
                    break;
                case "vectors":
                    tri.clearVector(split[2]);
                    break;
                case "index":
                    tri.clearIndex();
                    break;
                default:
                    throw new Exception("clear syntax error");
            }
        } else {
            throw new Exception("clear syntax error");
        }
    }

    private void add(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            Set<String> set = tri.add(split[1], split[2], Arrays.copyOfRange(split, 3, split.length));
            if (set.isEmpty()) {
                TriShell.println("No vectors found");
            } else {
                TriShell.print("Added vector for: ");
                for (String s : set) {
                    TriShell.print(s);
                    TriShell.print(" ");
                }
                TriShell.println("");
            }
        } else {
            throw new Exception("add syntax error");
        }
    }

    private void addv(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            Set<String> set = tri.addv(split[1], Arrays.copyOfRange(split, 2, split.length));
            if (set.isEmpty()) {
                TriShell.println("No vectors found");
            } else {
                TriShell.print("Added vector for: ");
                for (String s : set) {
                    TriShell.print(s);
                    TriShell.print(" ");
                }
                TriShell.println("");
            }
        } else {
            throw new Exception("addv syntax error");
        }
    }

    private void get(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            tri.get(split[1], split[2], split[3]);
        } else {
            throw new Exception("add syntax error");
        }
    }

    /**
     * Close all vector readers and clear stores
     *
     * @throws Exception
     */
    public void close() throws Exception {
        tri.close();
    }

    /**
     * Print the full help on the shell
     */
    public void fullHelp() {
        if (help != null) {
            List<String> commands = new ArrayList<>(help.stringPropertyNames());
            Collections.sort(commands);
            for (String cmd : commands) {
                TriShell.println(cmd + "\t" + help.getProperty(cmd));
            }
        }
    }

    /**
     * Print the command help on the shell
     *
     * @param cmd The command
     */
    public void help(String cmd) {
        if (help != null) {
            if (cmd.equals("*")) {
                fullHelp();
            } else {
                String s = help.getProperty(cmd);
                if (s != null) {
                    TriShell.println(s);
                } else {
                    TriShell.println("no valid command: " + cmd);
                }
            }
        }
    }

    private void near(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 3) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            List<ObjectVector> nv = tri.near(split[2], split[3], Integer.parseInt(split[1]));
            for (ObjectVector ov : nv) {
                TriShell.println(ov.getKey() + "\t" + ov.getScore());
            }
        } else {
            throw new Exception("near syntax error");
        }
    }

    private void sim(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            double sim = tri.sim(split[1], split[2]);
            TriShell.println("Sim(" + split[1] + ", " + split[2] + ")=" + sim);
        } else {
            throw new Exception("sim syntax error");
        }
    }

    private void tri(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (split[2].matches("[0-9]+") && split[3].matches("[0-9]+")) {
                tri.tri(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            } else {
                throw new Exception("tri syntax error");
            }
        } else {
            throw new Exception("tri syntax error");
        }
    }

    private void ftri(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (split[2].matches("[0-9]+") && split[3].matches("[0-9]+")) {
                tri.ftri(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            } else {
                throw new Exception("ftri syntax error");
            }
        } else {
            throw new Exception("ftri syntax error");
        }
    }

    private void compare(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 5) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            List<TriResultObject> rs = tri.compare(split[2], split[3], split[4], split[5], Integer.parseInt(split[1]));
            printTriResults(rs);
        } else {
            throw new Exception("compare syntax error");
        }
    }

    private void count(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 1) {
            int countVectors = tri.count(split[1]);
            TriShell.println(split[1] + " contains " + countVectors + ".");
        } else {
            throw new Exception("compare syntax error");
        }
    }

    private void cset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            tri.cset(split[1]);
        } else {
            throw new Exception("cset syntax error");
        }
    }

    private void rset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            tri.rset(split[1], Arrays.copyOfRange(split, 2, split.length));
        } else {
            throw new Exception("rset syntax error");
        }
    }

    private void dset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            tri.dset(split[1]);
        } else {
            throw new Exception("dset syntax error");
        }
    }

    private void aset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 2) {
            tri.aset(split[1], Arrays.copyOfRange(split, 2, split.length));
        } else {
            throw new Exception("aset syntax error");
        }
    }

    private void sset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 3) {
            if (!tri.readerReay()) {
                throw new Exception("no index in memory");
            } else {
                if (!split[2].matches("[0-9]+")) {
                    throw new Exception("no valid number of results");
                }
                StringBuilder qs = new StringBuilder();
                for (int i = 3; i < split.length; i++) {
                    qs.append(split[i]).append(" ");
                }
                List<TriResultObject> rs = tri.sset(split[1], qs.toString(), Integer.parseInt(split[2]));
                printTriResults(rs);
            }
        } else {
            throw new Exception("sset syntax error");
        }
    }

    private void pset(String command) throws Exception {
        String[] split = command.split("\\s+");
        if (split.length > 1) {
            Set<String> set = tri.getSet(split[1]);
            if (set != null) {
                for (String word : set) {
                    TriShell.println(word);
                }
            } else {
                TriShell.println("no set for: " + split[1]);
            }
        } else {
            throw new Exception("pset syntax error");
        }
    }

    private void vset(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            tri.vset(split[1], split[2], split[3]);
        } else {
            throw new Exception("vset syntax error");
        }
    }

    private void sims(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 3) {
            if (!split[1].matches("[0-9]+")) {
                throw new Exception("no valid number of results");
            }
            double min = -0.5;
            double max = 1.5;
            if (split.length > 5) {
                min = Double.parseDouble(split[4]);
                max = Double.parseDouble(split[5]);
            }
            List<ObjectVector> sims = tri.sims(split[2], split[3], Integer.parseInt(split[1]), min, max);
            for (ObjectVector ov : sims) {
                TriShell.println(ov.getKey() + "\t" + ov.getScore());
            }
        } else {
            throw new Exception("sims syntax error");
        }
    }

    private void plot(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length > 2) {
            if (split[1].equals("word")) {
                List<TriResultObject> rs = tri.plotWord(Arrays.copyOfRange(split, 2, split.length));
                printTriResults(rs);
            } else if (split[1].equals("words") && split.length > 3) {
                List<TriResultObject> rs = tri.plotWords(split[2], split[3]);
                printTriResults(rs);
            } else {
                throw new Exception("No valid plot command");
            }
        } else {
            throw new Exception("No valid plot command");
        }
    }

    private void cluster(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 4) {
            tri.cluster(split[1], Integer.parseInt(split[2]), split[3]);
        } else {
            throw new Exception("No valid cluster command");
        }
    }

    private void rmean(String cmd) throws Exception {
        String[] split = cmd.split("\\s+");
        if (split.length == 3) {
            tri.removeMean(split[1], split[2]);
        } else {
            throw new Exception("No valid rmean command");
        }
    }

    private void initHelp() {
        help.setProperty("set", "set <main dir> - set the main directory in which WordSpaces are stored");
        help.setProperty("index", "index <file|mem> <name> - create a words index from a vector reader using a filename (file) or a previous reader loaded in memory (mem)");
        help.setProperty("search", "search <number of resutls> <query> - search in the current words index");
        help.setProperty("indexelem", "indexelem - create the words index of the elemental vector");
        help.setProperty("year", "year (<start> <end>)* - list the available years");
        help.setProperty("load", "load <file|mem> (<name> <year>)* - load a vector reader of the specified type (mem or file) and year. If no name and year are provided the elemental vector reader is loaded");
        help.setProperty("fload", "fload <file|mem> <name> <filename> - load a vector reader (name) of the specified type (mem or file) from a file");
        help.setProperty("list", "list <stores|vectors> - list stores or vectors available in memory");
        help.setProperty("clear", "clear <stores|vectors|index> <name>* - remove a vector reader (stores) or a vector (vectors) or the index. If no name is provided all the elements are removed");
        help.setProperty("get", "get <vector reader name> <vector name> <word> - get the word vector from the vector reader and store it in memory using the vector name");
        help.setProperty("add", "add <vector reader name> <vector name> <word>+ - get and sum multiple word vectors from the vector reader and store the result in memory using the vector name");
        help.setProperty("addv", "addv <vector name> <vectors>+ - get and sum multiple vectors in memory and store the result in memory using the vector name");
        help.setProperty("near", "near <number of results> <vector reader name> <vector name> - print nearest vectors");
        help.setProperty("sim", "sim <vector name 1> <vector name 2> - print vectors similarity");
        help.setProperty("count", "count <vector reader name> - return the number of vectors in the vector reader");
        help.setProperty("tri", "tri <vector reader name> <start year> <end year> - create a new temporal space named vector reader name form start_year to end_year");
        help.setProperty("ftri", "ftri <output filename> <start year> <end year> - create a new temporal space form start_year to end_year and save it on disk");
        help.setProperty("sims", "sims <number of results> <vector reader name1> <vector reader name2> <min>? <max>?  - find words that change meaning between two WordSpaces. Min and max are used as thresholds for filtering results (optional).");
        help.setProperty("compare", "compare <number of results> <vector reader name1> <vector reader name1> <vector name1> <vector name2> - compare nearest vectors of vector name1 in vector reader name1 and vector name2 in vector reader name2");
        help.setProperty("rmean", "rmean <vector reader> <vector name> - remove mean component from vector");
        //help of commands related to sets
        help.setProperty("cset", "cset <name> - create a new set");
        help.setProperty("aset", "aset <name> <word>+ - add words to a set");
        help.setProperty("rset", "rset <name> <word>+ - remove words from a set");
        help.setProperty("dset", "dset <name> - remove a set");
        help.setProperty("sset", "sset <name> <number of results> <query> - search in the words index and save results in a set");
        help.setProperty("pset", "pset <name> print set");
        help.setProperty("vset", "vset <vector reader name> <set name> <vector name> - convert a set into a vector fetching vectors from the vector reader");
        //plot command
        help.setProperty("plot", "plot word (word)+ OR plot words word1 word2 - 'plot word' plots meaning variation over the time for all the (word)+, while 'plot words' plots similarity between word1 and word2 over the time");
        //cluster
        help.setProperty("cluster", "cluster <vector name> <k> <output file> - Create k clusters from the vector reader and save the results in the output file");
    }

}
