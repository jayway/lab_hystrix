package com.jayway.hystrixlab.repository;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.HttpClientConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.filter.log.LogDetail;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.hystrixlab.Boot.HystrixLabServer.initializeTodoCollection;
import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.with;
import static com.jayway.restassured.config.HttpClientConfig.httpClientConfig;
import static com.jayway.restassured.config.RestAssuredConfig.config;

public class RandomTodoCommands {
    private static final Logger log = LoggerFactory.getLogger(RandomTodoCommands.class);

    private static final Map<Integer, Runnable> COMMANDS = new HashMap<Integer, Runnable>() {{
        RestAssured.basePath = "/todos";
        RestAssured.requestSpecification = new RequestSpecBuilder().log(LogDetail.ALL).build();
        RestAssured.config = config().httpClient(httpClientConfig().reuseHttpClientInstance());

        put(0, () -> with().formParam("todo", RandomStringUtils.randomAscii(20)).post()); // Create
        put(1, () -> RestAssured.get(new ObjectId().toHexString())); // Find by id
        put(2, () -> delete(new ObjectId().toHexString())); // Delete
        put(3, RestAssured::get); // Find all
    }};

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(RandomTodoCommands::clearTodos));
        while (true) {
            COMMANDS.get(RandomUtils.nextInt(0, 4)).run();
            Thread.sleep(RandomUtils.nextInt(50, 200));
        }
    }

    private static void clearTodos() {
        log.info("Clearing todos");
        initializeTodoCollection().drop();
    }
}
