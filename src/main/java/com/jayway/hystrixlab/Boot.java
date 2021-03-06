package com.jayway.hystrixlab;

import com.jayway.hystrixlab.http.GlobalExceptionMapper;
import com.jayway.hystrixlab.http.TodoResource;
import com.jayway.hystrixlab.repository.TodoRepository;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Boot {
    private static final Logger log = LoggerFactory.getLogger(Boot.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("--port") || args[0].equalsIgnoreCase("-p")) {
                port = Integer.parseInt(args[1]);
            }
        }

        HystrixLabServer hystrixLabServer = new HystrixLabServer(port);
        try {
            Server server = hystrixLabServer.startServer();
            server.join();
        } finally {
            hystrixLabServer.stopServer();
        }
    }

    public static class HystrixLabServer {
        private final Server server;
        private final TodoRepository todoRepository;

        public HystrixLabServer(int port) {
            this.server = new Server(port);
            todoRepository = new TodoRepository(initializeTodoCollection());
        }

        public static DBCollection initializeTodoCollection() {
            DB mongoDB;
            try {
                mongoDB = new MongoClient("localhost").getDB("hystrixLab");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            return mongoDB.getCollection("todos");
        }

        public int getPort() {
            return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        }

        public Server startServer() {
            ServletContextHandler externalContext = createContextHandler();

            final HandlerCollection collection = new HandlerCollection();
            collection.addHandler(externalContext);


            server.setHandler(collection);
            try {
                server.start();
                log.info("{} started on port {}", HystrixLabServer.class.getSimpleName(), getPort());
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
            resourceConfig.register(new TodoResource(todoRepository));
            resourceConfig.register(new GlobalExceptionMapper());
            resourceConfig.register(JacksonFeature.class);

            ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
            ctx.addServlet(servletHolder, "/*");
            return ctx;

        }

        public TodoRepository todoRepository() {
            return todoRepository;
        }
    }

}
