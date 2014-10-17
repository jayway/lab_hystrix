package com.jayway.hystrixlab.http;

import com.jayway.hystrixlab.repository.TodoRepository;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/todos")
public class TodoResource {

    private final TodoRepository todoRepository;

    public TodoResource(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @POST
    public Map<String, Object> create(@FormParam("todo") String todo) {
        return todoRepository.create(new HashMap<String, Object>() {{
            put("todo", todo);
        }});
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> findById(@PathParam("id") String id) {
        return todoRepository.findById(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) {
        todoRepository.delete(id);
    }

    @GET
    public List<Map<String, Object>> findAll() {
        return todoRepository.findAll();
    }
}
