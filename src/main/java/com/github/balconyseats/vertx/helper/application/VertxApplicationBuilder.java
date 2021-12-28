package com.github.balconyseats.vertx.helper.application;

import com.github.balconyseats.vertx.helper.config.ConfigurationLoader;
import com.github.balconyseats.vertx.helper.application.configurer.InitializationContextConfigurer;
import com.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import com.github.balconyseats.vertx.helper.application.configurer.VerticleConfigurer;
import com.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;

import java.util.List;

public class VertxApplicationBuilder {
    private ConfigurationLoader configurationLoader;
    private List<VerticleConfigurer> verticleConfigurers;
    private VertxOptionsConfigurer vertxOptionsConfigurer;
    private InitializationContextConfigurer initializationContextConfigurer;
    private InitializationHandler preHandler;
    private InitializationHandler postHandler;

    public VertxApplicationBuilder configurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
        return this;
    }

    public VertxApplicationBuilder verticleConfigurers(VerticleConfigurer... verticleConfigurers) {
        this.verticleConfigurers = List.of(verticleConfigurers);
        return this;
    }

    public VertxApplicationBuilder vertxOptionsConfigurer(VertxOptionsConfigurer vertxOptionsConfigurer) {
        this.vertxOptionsConfigurer = vertxOptionsConfigurer;
        return this;
    }

    public VertxApplicationBuilder initializationContextConfigurer(InitializationContextConfigurer initializationContextConfigurer) {
        this.initializationContextConfigurer = initializationContextConfigurer;
        return this;
    }

    public VertxApplicationBuilder preHandler(InitializationHandler handler) {
        this.preHandler = handler;
        return this;
    }

    public VertxApplicationBuilder postHandler(InitializationHandler handler) {
        this.postHandler = handler;
        return this;
    }

    public VertxApplication build() {
        return new VertxApplication(configurationLoader, verticleConfigurers, vertxOptionsConfigurer, initializationContextConfigurer, preHandler, postHandler);
    }
}