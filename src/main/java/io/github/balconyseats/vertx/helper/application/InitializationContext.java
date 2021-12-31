package io.github.balconyseats.vertx.helper.application;

import java.util.HashMap;
import java.util.Map;

/**
 * Intialization context is used for singleton objects that are then used by {@link io.vertx.core.Verticle} instances.
 * E.g. initialization context can contain reference to database pool instance.
 */
public class InitializationContext {

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Add object to context.
     * @param key context object key
     * @param value context object instance
     * @return this {@link InitializationContext} instance
     */
    public InitializationContext add(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Retrieve object from context.
     * @param key object key
     * @param <T> returned object type
     * @return object from context
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) context.get(key);
    }

}
