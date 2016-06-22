/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script.gbooks;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class Ngram {

    private List<String> tokens;

    private int year;

    private int count;

    private int bookCount;

    public Ngram() {
    }

    public Ngram(List<String> tokens, int year, int count, int bookCount) {
        this.tokens = tokens;
        this.year = year;
        this.count = count;
        this.bookCount = bookCount;
    }

    public Ngram(int year, int count, int bookCount) {
        this.tokens = new ArrayList<>();
        this.year = year;
        this.count = count;
        this.bookCount = bookCount;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public void write(Writer writer) throws IOException {
        for (int i = 0; i < tokens.size(); i++) {
            writer.append(tokens.get(i));
            if (i < tokens.size() - 1) {
                writer.append(" ");
            }
        }
        writer.append("\t");
        writer.append(String.valueOf(count));
    }

}
