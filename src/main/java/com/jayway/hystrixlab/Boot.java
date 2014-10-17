package com.jayway.hystrixlab;

import com.jayway.hystrixlab.http.GlobalExceptionMapper;
import com.jayway.hystrixlab.http.TodoResource;
import com.jayway.hystrixlab.repository.TodoRepository;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Boot {
    private static final Logger log = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) throws Exception {
        HystrixLabServer hystrixLabServer = new HystrixLabServer();
        try {
            Server server = hystrixLabServer.startServer();
            server.join();
        } finally {
            hystrixLabServer.stopServer();
        }
    }


    static class HystrixLabServer {
        private final Server server;
        private final DB mongoDB;

        HystrixLabServer() {
            this.server = new Server();
            try {
                mongoDB = new MongoClient("localhost").getDB("hystrixLab");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        public Server startServer() {
            ServletContextHandler externalContext = createContextHandler();

            final HandlerCollection collection = new HandlerCollection();
            collection.addHandler(externalContext);


            server.setHandler(collection);
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return server;
        }

        public void stopServer() {
            try {
                if (server.isRunning()) {
                    server.stop();
                    server.join();
                }
            } catch (Exception e) {
                log.error("Caught exception when stopping server", e);
            }
        }

        private ServletContextHandler createContextHandler() {
            ResourceConfig resourceConfig = new ResourceConfig();
            TodoRepository todoRepository = new TodoRepository(mongoDB.getCollection("todos"));
            resourceConfig.register(new TodoResource(todoRepository));
            resourceConfig.register(new GlobalExceptionMapper());

            ServletContextHandler externalContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
            externalContext.addServlet(servletHolder, "/");
            return externalContext;

        }
    }

}
