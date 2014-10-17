package com.jayway.hystrixlab.http;

import com.jayway.hystrixlab.repository.TodoRepository;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

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

    private final TodoRepository todoRepository;

    public TodoResource(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @POST
    public Map<String, Object> create(@FormParam("todo") String todo) throws Exception {
        return new MongoHystrixCommand<Map<String, Object>>(CREATE,
                () -> todoRepository.create(new HashMap<String, Object>() {{
                    put("todo", todo);
                }}), Collections::emptyMap).run();
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> findById(@PathParam("id") String id) throws Exception {
        return new MongoHystrixCommand<Map<String, Object>>(FIND_BY_ID,
                () -> todoRepository.findById(id),
                Collections::emptyMap).run();
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) throws Exception {
        new MongoHystrixCommand<Void>(DELETE, () -> {
            todoRepository.delete(id);
            return null;
        }, () -> null).run();
    }

    @GET
    public List<Map<String, Object>> findAll() {
        return todoRepository.findAll();
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
