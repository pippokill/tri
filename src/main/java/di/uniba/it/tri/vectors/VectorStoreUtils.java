/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.vectors;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 *
 * @author pierpaolo
 */
public class VectorStoreUtils {

    public static String createHeader(VectorType type, int dimension, int seed) {
        StringBuilder sb = new StringBuilder();
        sb.append("-type\t").append(type.name());
        sb.append("\t-dim\t").append(String.valueOf(dimension));
        sb.append("\t-seed\t").append(String.valueOf(seed));
        return sb.toString();
    }

    public static Properties readHeader(String line) throws IllegalArgumentException {
        Properties props = new Properties();
        String[] split = line.split("\t");
        if (split.length % 2 == 0) {
            for (int i = 0; i < split.length; i = i + 2) {
                props.put(split[i], split[i + 1]);
            }
        } else {
            throw new IllegalArgumentException("Not valid header: " + line);
        }
        return props;
    }

    public static void saveSpace(File outputFile, Map<String, Vector> vectors, VectorType type, int dimension, int seed) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed);
        outputStream.writeUTF(header);
        for (Entry<String, Vector> entry : vectors.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().writeToStream(outputStream);
        }
        outputStream.close();
    }

}
