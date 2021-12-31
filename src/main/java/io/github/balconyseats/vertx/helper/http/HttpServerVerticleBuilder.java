package io.github.balconyseats.vertx.helper.http;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.List;

/**
 * Builder for {@link HttpServerVerticle}
 */
public class HttpServerVerticleBuilder {
    private JsonObject config;
    private List<RouterConfigurer> subrouterConfigurers;
    private List<RouterHandler> routerHandlers;
    private List<Handler<Router>> simpleRouterHandlers;
    private Configurer<HttpServerOptions> httpServerOptionsConfigurer;

    /**
     * Add configuration object
     * @param config configuration object
     * @return this instance
     */
    public HttpServerVerticleBuilder config(JsonObject config) {
        this.config = config;
        return this;
    }

    /**
     * Add subrouter configurers {@link RouterConfigurer}
     * @param subrouterConfigurers list of configurers
     * @return this instance
     */
    public HttpServerVerticleBuilder subrouterConfigurers(List<RouterConfigurer> subrouterConfigurers) {
        this.subrouterConfigurers = subrouterConfigurers;
        return this;
    }

    public HttpServerVerticleBuilder routerHandlers(List<RouterHandler> routerHandlers) {
        this.routerHandlers = routerHandlers;
        return this;
    }

    public HttpServerVerticleBuilder setSimpleRouterHandlers(List<Handler<Router>> simpleRouterHandlers) {
        this.simpleRouterHandlers = simpleRouterHandlers;
        return this;
    }

    public HttpServerVerticleBuilder httpServerOptionsConfigurer(Configurer<HttpServerOptions> httpServerOptionsConfigurer) {
        this.httpServerOptionsConfigurer = httpServerOptionsConfigurer;
        return this;
    }

    public HttpServerVerticle build() {
        return new HttpServerVerticle(config, subrouterConfigurers, routerHandlers, simpleRouterHandlers, httpServerOptionsConfigurer);
    }
}