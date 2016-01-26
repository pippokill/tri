/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.ir;

import di.uniba.it.tri.occ.OccUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author pierpaolo
 */
public class Searcher {

    private final IndexSearcher searcher;

    private Analyzer analyzer;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CEST' yyyy", Locale.ENGLISH);

    private String idFieldname = "id";

    private String[] textFieldname = new String[]{"title", "content"};

    private String sourceFieldname = "source";

    private String dateFieldname = "published";

    public Searcher(File inputIndex) throws IOException {
        DirectoryReader dr = DirectoryReader.open(FSDirectory.open(inputIndex));
        searcher = new IndexSearcher(dr);
        analyzer = new StandardAnalyzer();
    }

    public Searcher(File inputIndex, String analyzerType, String stopWordFilename) throws Exception {
        DirectoryReader dr = DirectoryReader.open(FSDirectory.open(inputIndex));
        searcher = new IndexSearcher(dr);
        if (analyzerType.equals("st")) {
            if (stopWordFilename != null) {
                Set<String> stopWords = OccUtils.loadSet(new File(stopWordFilename));
                analyzer = new StandardAnalyzer(CharArraySet.copy(stopWords));
            } else {
                analyzer = new StandardAnalyzer();
            }
        } else if (analyzerType.equals("en")) {
            if (stopWordFilename != null) {
                Set<String> stopWords = OccUtils.loadSet(new File(stopWordFilename));
                analyzer = new EnglishAnalyzer(CharArraySet.copy(stopWords));
            } else {
                analyzer = new EnglishAnalyzer();
            }
        } else {
            throw new IllegalArgumentException("Not valid analyzer type: " + analyzerType);
        }
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getIdFieldname() {
        return idFieldname;
    }

    public void setIdFieldname(String idFieldname) {
        this.idFieldname = idFieldname;
    }

    public String[] getTextFieldname() {
        return textFieldname;
    }

    public void setTextFieldname(String[] textFieldname) {
        this.textFieldname = textFieldname;
    }

    public String getSourceFieldname() {
        return sourceFieldname;
    }

    public void setSourceFieldname(String sourceFieldname) {
        this.sourceFieldname = sourceFieldname;
    }

    public String getDateFieldname() {
        return dateFieldname;
    }

    public void setDateFieldname(String dateFieldname) {
        this.dateFieldname = dateFieldname;
    }

    public List<SearchResult> search(String query, int topn) throws ParseException, IOException {
        List<SearchResult> results = new ArrayList<>();
        query = QueryParser.escape(query);
        QueryParser qp = new MultiFieldQueryParser(textFieldname, analyzer);
        Query q = qp.parse(query);
        TopDocs topDocs = searcher.search(q, topn);
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            SearchResult sr = new SearchResult(topDocs.scoreDocs[i].doc, topDocs.scoreDocs[i].score);
            Document doc = searcher.doc(sr.getDocid());
            sr.setId(doc.get(idFieldname));
            sr.setSource(doc.get(sourceFieldname));
            StringBuilder sb = new StringBuilder();
            for (String fn : textFieldname) {
                sb.append(doc.get(fn)).append("\n");
            }
            sr.setText(sb.toString());
            results.add(sr);
            try {
                Date date = dateFormat.parse(doc.get(dateFieldname));
                sr.setDate(date);
            } catch (java.text.ParseException ex) {
                Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

    public List<SearchResult> search(String query, int topn, Date start, Date end) throws ParseException, IOException {
        List<SearchResult> results = new ArrayList<>();
        query = QueryParser.escape(query);
        QueryParser qp = new MultiFieldQueryParser(textFieldname, analyzer);
        Query qtext = qp.parse(query);
        Query qtime = NumericRangeQuery.newLongRange("time", start.getTime(), end.getTime(), true, true);
        BooleanQuery q = new BooleanQuery();
        q.add(qtext, BooleanClause.Occur.MUST);
        q.add(qtime, BooleanClause.Occur.MUST);
        TopDocs topDocs = searcher.search(q, topn);
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            SearchResult sr = new SearchResult(topDocs.scoreDocs[i].doc, topDocs.scoreDocs[i].score);
            Document doc = searcher.doc(sr.getDocid());
            sr.setId(doc.get(idFieldname));
            sr.setSource(doc.get(sourceFieldname));
            StringBuilder sb = new StringBuilder();
            for (String fn : textFieldname) {
                sb.append(doc.get(fn)).append("\n");
            }
            sr.setText(sb.toString());
            results.add(sr);
            try {
                Date date = dateFormat.parse(doc.get(dateFieldname));
                sr.setDate(date);
            } catch (java.text.ParseException ex) {
                Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

}
