package com.github.balconyseats.vertx.helper.application.http;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class HttpServerVerticleBuilder {
    private JsonObject config;
    private List<RouterConfigurer> subrouterConfigurers;
    private List<RouterHandler> routerHandlers;
    private Configurer<HttpServerOptions> httpServerOptionsConfigurer;

    public HttpServerVerticleBuilder config(JsonObject config) {
        this.config = config;
        return this;
    }

    public HttpServerVerticleBuilder subrouterConfigurers(List<RouterConfigurer> subrouterConfigurers) {
        this.subrouterConfigurers = subrouterConfigurers;
        return this;
    }

    public HttpServerVerticleBuilder routerHandlers(List<RouterHandler> routerHandlers) {
        this.routerHandlers = routerHandlers;
        return this;
    }

    public HttpServerVerticleBuilder httpServerOptionsConfigurer(Configurer<HttpServerOptions> httpServerOptionsConfigurer) {
        this.httpServerOptionsConfigurer = httpServerOptionsConfigurer;
        return this;
    }

    public HttpServerVerticle build() {
        return new HttpServerVerticle(config, subrouterConfigurers, routerHandlers, httpServerOptionsConfigurer);
    }
}