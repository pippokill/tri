/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.data.h2;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author pierpaolo
 */
public class H2Storage {

    private JdbcConnectionPool cp;

    private static final String CR_TABLE_LEX = "CREATE TABLE lex (id BIGINT PRIMARY KEY, word VARCHAR(128), totCount INTEGER)";

    private static final String CR_TABLE_OCC = "CREATE TABLE occ (lexid1 BIGINT, lexid2 BIGINT, year INTEGER, count INTEGER)";

    private static final Logger LOG = Logger.getLogger(H2Storage.class.getName());

    private int cacheSize = 1024 * 32;

    public H2Storage(String storageDirname) throws IOException, SQLException {
        init(storageDirname);
    }

    public H2Storage(String storageDirname, int cacheSize) throws IOException, SQLException {
        this.cacheSize = cacheSize;
        init(storageDirname);
    }

    private void deleteFile(String storageDirname) throws IOException {
        File dir = new File(storageDirname);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    private void createTable() throws SQLException {
        Connection connection = cp.getConnection();
        try {
            Statement stm = connection.createStatement();
            stm.executeUpdate(CR_TABLE_LEX);
            stm.close();
            stm = connection.createStatement();
            stm.executeUpdate(CR_TABLE_OCC);
            stm.close();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            connection.close();
        }
    }

    private void setCacheSize(int cacheSizeInKB) throws SQLException {
        Connection connection = cp.getConnection();
        String queryString = "SET CACHE_SIZE " + cacheSizeInKB + ';';
        try {
            Statement statement = connection.createStatement();
            statement.execute(queryString);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw ex;
            }
        }
    }

    private void init(String storageDirname) throws IOException, SQLException {
        deleteFile(storageDirname);
        cp = JdbcConnectionPool.create("jdbc:h2:" + storageDirname, "h2stri", "h2stri");
        createTable();
        setCacheSize(cacheSize);
    }

    public void addLex(long id, String word, int totCount) throws SQLException, IllegalArgumentException {
        if (word.length() > 128) {
            throw new IllegalArgumentException("Word exceeds 128 characters");
        }
        Connection connection = cp.getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO lex(id, word, totCount) VALUES (?, ?, ?)");
            pstm.setLong(1, id);
            pstm.setString(2, word);
            pstm.setInt(3, totCount);
            pstm.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            connection.close();
        }
    }

    public void addOcc(long id1, long id2, int year, int occ) throws SQLException {
        Connection connection = cp.getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO occ(lexid1, lexid2, year, count) VALUES (?, ?, ?, ?)");
            pstm.setLong(1, id1);
            pstm.setLong(2, id2);
            pstm.setInt(3, year);
            pstm.setInt(4, occ);
            pstm.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            connection.close();
        }
    }

    public void close() {
        cp.dispose();
    }

    public void commit() throws SQLException {
        Connection connection = cp.getConnection();
        try {
            connection.commit();
        } catch (SQLException ex) {
            throw ex;
        } finally {
            connection.close();
        }
    }

}
