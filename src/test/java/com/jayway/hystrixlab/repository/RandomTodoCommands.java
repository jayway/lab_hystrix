package com.jayway.hystrixlab.repository;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.filter.log.LogDetail;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.hystrixlab.Boot.HystrixLabServer.initializeTodoCollection;
import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.with;

public class RandomTodoCommands {

    private static final Map<Integer, Runnable> commands = new HashMap<Integer, Runnable>() {{
        RestAssured.basePath = "/todos";
        RestAssured.requestSpecification = new RequestSpecBuilder().log(LogDetail.ALL).build();

        put(0, () -> with().formParam("todo", RandomStringUtils.randomAscii(20)).post()); // Create
        put(1, () -> get(new ObjectId().toHexString())); // Find by id
        put(2, () -> delete(new ObjectId().toHexString())); // Delete
        put(3, RestAssured::get); // Find all
    }};

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> initializeTodoCollection().drop()));
        while (true) {
            commands.get(RandomUtils.nextInt(0, 4)).run();
            Thread.sleep(RandomUtils.nextInt(50, 500));
        }
    }
}
