/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.aan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author pierpaolo
 */
public class AAN2file {

    public static final int[] yearsdir = new int[]{2008, 2009, 2010, 2011, 2012, 2013};

    private String getValue(String line) {
        int s = line.indexOf("{");
        int e = line.indexOf("}");
        if (e < 0) {
            e = line.length();
        }
        if (e > s && s > -1 && e > -1) {
            return line.substring(s + 1, e);
        } else {
            Logger.getLogger(AAN2file.class.getName()).log(Level.WARNING, "No valid line: {0}", line);
            return null;
        }
    }

    private List<Paper> loadPaperList(File metadatafile, String paperdirname) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(metadatafile));
        String id = null;
        String year = null;
        List<Paper> list = new ArrayList<>();
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.startsWith("id = {")) {
                id = getValue(line);
            } else if (line.startsWith("year = {")) {
                year = getValue(line);
            }
            if (line.length() == 0) {
                if (id != null && year != null) {
                    Paper paper = new Paper(id);
                    paper.setYear(Integer.parseInt(year));
                    paper.setFile(new File(paperdirname + "/" + id + ".txt"));
                    list.add(paper);
                }

            }
        }
        return list;
    }

    public void build(String anndirname, String outputdirname) throws Exception {
        for (int i = 0; i < yearsdir.length; i++) {
            File metadatafile = new File(anndirname + "/release/" + yearsdir[i] + "/acl-metadata.txt");
            List<Paper> list = loadPaperList(metadatafile, anndirname + "/papers_text/");
            for (Paper paper : list) {
                if (paper.getFile().exists()) {
                    File destFile = new File(outputdirname + "/" + paper.getId() + "_" + paper.getYear());
                    if (!destFile.exists()) {
                        FileUtils.copyFile(paper.getFile(), destFile);
                    }
                } else {
                    Logger.getLogger(AAN2file.class.getName()).log(Level.WARNING, "Paper not found: {0}", paper.getFile());
                }
            }
        }
    }

    /**
     * Convert ACL dataset in a single file for each paper with year reference
     * aan_dir output_dir
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length > 1) {
                AAN2file ann = new AAN2file();
                ann.build(args[0], args[1]);
            } else {
                throw new Exception("Illegal arguments");
            }
        } catch (Exception ex) {
            Logger.getLogger(AAN2file.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
