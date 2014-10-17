package com.jayway.hystrixlab.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TodoRepository {

    private static final String ID = "_id";
    private final DBCollection todos;

    public TodoRepository(DBCollection todos) {
        this.todos = todos;
    }

    public Map<String, Object> create(Map<String, Object> todo) {
        BasicDBObject dbObject = new BasicDBObject(todo);
        dbObject.put(ID, new ObjectId().toHexString());
        todos.insert(dbObject);
        return findById(dbObject.getString(ID));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findById(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put(ID, id);
        DBObject dbObj = todos.findOne(query);
        if (dbObj == null) {
            return null;
        }
        return (Map<String, Object>) dbObj.toMap();
    }

    public void delete(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put(ID, new ObjectId(id).toHexString());
        todos.remove(query);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findAll() {
        try (DBCursor cursor = todos.find()) {
            return StreamSupport.stream(cursor.spliterator(), false).map(DBObject::toMap).map(todo -> (Map<String, Object>) todo).collect(Collectors.toList());
        }
    }

    public void clear() {
        todos.drop();
    }

    public long count() {
        return todos.count();
    }

    public boolean hasTodo(String id) {
        return findById(id) != null;
    }
}
