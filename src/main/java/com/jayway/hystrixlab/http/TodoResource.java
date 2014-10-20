package com.jayway.hystrixlab.http;

import com.jayway.hystrixlab.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return todoRepository.create(new HashMap<String, Object>() {{
            put("todo", todo);
        }});
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> findById(@PathParam("id") String id) throws Exception {
        log.trace("Finding todo with id {}", id);
        return todoRepository.findById(id);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") String id) throws Exception {
        log.trace("Deleting todo with id {}", id);
        todoRepository.delete(id);
    }

    @GET
    public List<Map<String, Object>> findAll() throws Exception {
        log.trace("Finding all todos");
        return todoRepository.findAll();
    }
}
