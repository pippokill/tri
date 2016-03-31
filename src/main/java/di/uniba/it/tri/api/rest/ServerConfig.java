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
package di.uniba.it.tri.api.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author pierpaolo
 */
public class ServerConfig {

    private static ServerConfig instance;

    private Properties props;

    private ServerConfig(String propsFilename) throws IOException {
        props = new Properties();
        props.load(new FileInputStream(propsFilename));
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public synchronized static ServerConfig getInstance() throws IOException {
        return getInstance("./server.config");
    }

    /**
     * 
     *
     * @param propsFilename 
     * @return
     * @throws IOException
     */
    public synchronized static ServerConfig getInstance(String propsFilename) throws IOException {
        if (instance == null) {
            instance = new ServerConfig(propsFilename);
        }
        return instance;
    }

    /**
     *
     * @param key 
     * @return 
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     *
     * @param key 
     * @return
     */
    public int getInt(String key) throws NumberFormatException {
        return Integer.parseInt(getProperty(key));
    }

}
