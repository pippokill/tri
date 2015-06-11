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
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
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

    private final String indexDir;

    private final List<IndexSearcher> slist = new ArrayList<>();

    public GbooksLuceneSearcher(String indexDir) throws IOException {
        this.indexDir = indexDir;
    }

    private void init() throws IOException {
        File[] listFiles = new File(indexDir).listFiles();
        for (File idxdir : listFiles) {
            if (idxdir.isDirectory() && idxdir.getName().startsWith("idx_")) {
                LOG.log(Level.INFO, "Open index: {0}", idxdir.getAbsolutePath());
                DirectoryReader dr = DirectoryReader.open(FSDirectory.open(idxdir));
                IndexSearcher searcher = new IndexSearcher(dr);
                slist.add(searcher);
            }
        }
        LOG.log(Level.INFO, "Indices: {0}", slist.size());
    }

    public List<NgramSearchResult> search(String querytext, int n) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        QueryParser qp = new QueryParser("ngram", new WhitespaceAnalyzer());
        Query query = qp.parse(querytext);
        System.out.println(query.toString());
        PriorityQueue<NgramSearchResult> queue = new PriorityQueue<>();
        for (IndexSearcher is : slist) {
            TopDocs topdocs = is.search(query, n);
            for (int i = 0; i < topdocs.scoreDocs.length; i++) {
                Document doc = is.doc(topdocs.scoreDocs[i].doc);
                NgramSearchResult r = new NgramSearchResult(doc.get("ngram"), topdocs.scoreDocs[i].score, doc.getField("count").numericValue().intValue());
                if (queue.size() < n) {
                    queue.offer(r);
                } else {
                    queue.poll();
                    queue.offer(r);
                }
            }
        }
        return new ArrayList<>(queue);
    }

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
                GbooksLuceneSearcher searcher = new GbooksLuceneSearcher(cmd.getOptionValue("i"));
                searcher.init();
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
                            List<NgramSearchResult> search = searcher.search(querytext, topn);
                            for (NgramSearchResult r : search) {
                                System.out.println(r);
                            }
                            System.out.println("===(" + search.size() + ")========================================");
                        } catch (Exception ex) {
                            System.err.println("Error to execute search command: " + ex.getMessage());
                        }
                    } else if (s.matches("(^set$)|(^set\\s+.*$)")) {
                        try {
                            String[] split = s.split("\\s+");
                            if (split.length > 1) {
                                topn = Integer.parseInt(split[1]);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error to execute set command: " + ex.getMessage());
                        }
                    } else if (s.equals("exit") || s.equals("quit")) {
                        System.out.println("Goodbye");
                        read = false;
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
