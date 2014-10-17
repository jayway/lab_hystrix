package com.jayway.hystrixlab.repository;

import org.junit.Ignore;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.hystrixlab.Boot.HystrixLabServer;

@Ignore("Not a test")
public class HystrixLabServerRule implements TestRule {

    private static final int RANDOM_PORT = 0;

    private final AtomicReference<HystrixLabServer> server = new AtomicReference<>();

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                HystrixLabServer hystrixLabServer = new HystrixLabServer(RANDOM_PORT);
                hystrixLabServer.startServer();
                server.set(hystrixLabServer);
                try {
                    base.evaluate();
                } finally {
                    todoRepository().clear();
                    hystrixLabServer.stopServer();
                    server.set(null);
                }
            }
        };
    }

    public int getPort() {
        return server.get().getPort();
    }

    public TodoRepository todoRepository() {
        return server.get().todoRepository();
    }
}
