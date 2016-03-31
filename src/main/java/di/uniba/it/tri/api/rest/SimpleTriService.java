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

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author pierpaolo
 */
public class SimpleTriService extends Thread {

    private final HttpServer server;

    /**
     * Start HTTP server
     *
     * @return @throws IOException
     */
    protected HttpServer startServer() throws IOException {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages(true, "di.uniba.it.tri.api.rest.v1").register(ResponseAllowOriginFilter.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(ServerConfig.getInstance().getProperty("bind.address")), rc);
    }

    /**
     *
     * @throws Exception
     */
    public SimpleTriService() throws Exception {
        this("./server.config");
    }

    /**
     *
     * @param propsFilename
     * @throws Exception
     */
    public SimpleTriService(String propsFilename) throws Exception {
        //init config
        System.out.println("Starting server using config file: " + propsFilename);
        ServerConfig.getInstance(propsFilename);
        //init wrapper
        System.out.println("Init wrapper");
        SimpleTriServerWrapper serviceWrapper = SimpleTriServerWrapper.getInstance();
        serviceWrapper.setBasedir(ServerConfig.getInstance().getProperty("basedir"));
        server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl", ServerConfig.getInstance().getProperty("bind.address")));
        //attach a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            //this method is run when the service is stopped (SIGTERM)
            @Override
            public void run() {
                try {
                    server.shutdownNow();
                } catch (Exception ex) {
                    Logger.getLogger(SimpleTriService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleTriService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            SimpleTriService service;
            if (args.length > 0) {
                service = new SimpleTriService(args[0]);
            } else {
                service = new SimpleTriService();
            }
            service.start();
        } catch (Exception ex) {
            Logger.getLogger(SimpleTriService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
