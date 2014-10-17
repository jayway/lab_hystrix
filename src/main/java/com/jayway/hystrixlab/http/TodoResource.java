package com.jayway.hystrixlab.http;

import com.jayway.hystrixlab.repository.TodoRepository;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.jayway.hystrixlab.http.TodoResource.CommandKey.*;

@Path("/todos")
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {
    private static final Logger log = LoggerFactory.getLogger(TodoResource.class);

    private final TodoRepository todoRepository;

    public TodoResource(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @POST
    public Map<String, Object> create(@FormParam("todo") String todo) throws Exception {
        log.trace("Creating todo: {}", todo);
        return new MongoHystrixCommand<Map<String, Object>>(CREATE,
                () -> todoRepository.create(new HashMap<String, Object>() {{
                    put("todo", todo);
                }}), Collections::emptyMap).execute();
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> findById(@PathParam("id") String id) throws Exception {
        log.trace("Finding todo with id {}", id);
        return new MongoHystrixCommand<Map<String, Object>>(FIND_BY_ID, () -> todoRepository.findById(id), Collections::emptyMap).execute();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) throws Exception {
        log.trace("Deleting todo with id {}", id);
        new MongoHystrixCommand<Void>(DELETE, () -> {
            todoRepository.delete(id);
            return null;
        }, () -> null).execute();
    }

    @GET
    public List<Map<String, Object>> findAll() throws Exception {
        log.trace("Finding all todos");
        return new MongoHystrixCommand<List<Map<String, Object>>>(FIND_ALL, todoRepository::findAll, Collections::emptyList).execute();
    }

    enum CommandKey {
        CREATE, FIND_BY_ID, DELETE, FIND_ALL
    }

    static class MongoHystrixCommand<T> extends HystrixCommand<T> {
        private static final String GROUP_KEY = "todos-mongodb";

        private final Supplier<T> command;
        private final Supplier<T> fallback;

        protected MongoHystrixCommand(CommandKey commandKey, Supplier<T> command, Supplier<T> fallback) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUP_KEY)).andCommandKey(HystrixCommandKey.Factory.asKey(commandKey.toString())));
            this.command = command;
            this.fallback = fallback;
        }

        @Override
        protected T run() throws Exception {
            return command.get();
        }

        @Override
        protected T getFallback() {
            return fallback.get();
        }
    }
}
