/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.gbooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author pierpaolo
 */
public class GbooksLuceneSearcher {

    private static final Logger LOG = Logger.getLogger(GbooksLuceneSearcher.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Index directory");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i")) {
                DirectoryReader dr = DirectoryReader.open(FSDirectory.open(new File(cmd.getOptionValue("i"))));
                IndexSearcher searcher = new IndexSearcher(dr);
                QueryParser qp = new QueryParser("ngram", new WhitespaceAnalyzer());
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                boolean read = true;
                int topn = 25;
                String s;
                while (read) {
                    System.out.print("> ");
                    s = reader.readLine();
                    if (s.matches("(^search$)|(^search\\s+.*$)")) {
                        try {
                            String querytext = s.substring(s.indexOf("h") + 1);
                            Query query = qp.parse(querytext);
                            System.out.println(query.toString());
                            TopDocs topdocs = searcher.search(query, topn);
                            for (int i = 0; i < topdocs.scoreDocs.length; i++) {
                                System.out.println(searcher.doc(topdocs.scoreDocs[i].doc).get("ngram") + "\t" + searcher.doc(topdocs.scoreDocs[i].doc).get("count"));
                            }
                            System.out.println("===(" + topdocs.scoreDocs.length + ")========================================");
                        } catch (Exception ex) {
                            System.err.println("Error to execute search command: " + ex.getMessage());
                        }
                    } else if (s.matches("(^set$)|(^!s\\s+.*$)")) {
                        try {
                            String[] split = s.split("\\s+");
                            if (split.length > 1) {
                                topn = Integer.parseInt(split[1]);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error to execute set command: " + ex.getMessage());
                        }
                    } else if (s.equals("!q")) {
                        System.out.println("Goodbye");
                        read = false;
                        dr.close();
                    } else {
                        System.out.println("Command not valid: " + s);
                    }
                }
                reader.close();
            }
        } catch (IOException | ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
