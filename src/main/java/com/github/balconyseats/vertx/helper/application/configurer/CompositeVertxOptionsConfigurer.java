package com.github.balconyseats.vertx.helper.application.configurer;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompositeVertxOptionsConfigurer implements VertxOptionsConfigurer {

    private final List<VertxOptionsConfigurer> configurers;

    public CompositeVertxOptionsConfigurer(List<VertxOptionsConfigurer> configurers) {
        this.configurers = Objects.requireNonNullElseGet(configurers, ArrayList::new);
    }

    @Override
    public VertxOptions configure(VertxOptions vertxOptions, JsonObject config) {
        this.configurers.forEach(c -> c.configure(vertxOptions, config));
        return vertxOptions;
    }
}
