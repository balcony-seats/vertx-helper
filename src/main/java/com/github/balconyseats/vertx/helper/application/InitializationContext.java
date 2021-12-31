package com.github.balconyseats.vertx.helper.application;

import java.util.HashMap;
import java.util.Map;

/**
 * Intialization context is used for singleton objects which is then used by {@link io.vertx.core.Verticle} instances or some configurers.
 * <br/>
 * For example application context can have database pool instance.
 */
public class InitializationContext {

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Add object to context
     * @param key key of object in context
     * @param value instance of object in context
     * @return this instance of {@link InitializationContext}
     */
    public InitializationContext add(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Retrieve object from context
     * @param key key of object
     * @param <T> type of returned object
     * @return object from context
     */
    public <T> T get(String key) {
        return (T) context.get(key);
    }

}
