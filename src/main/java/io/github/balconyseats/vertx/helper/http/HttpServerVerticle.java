package io.github.balconyseats.vertx.helper.http;

import io.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates http server verticle using configuration data:
 * <pre>
 *     http:
 *       server:
 *          port: 8080
 * </pre>
 */
public class HttpServerVerticle extends AbstractVerticle {

    // Configurations
    public static final String CONFIG_HTTP_SERVER_PORT = "/http/server/port";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
    private static final Integer DEFAULT_PORT = 8080;

    private final JsonObject config;
    private final List<RouterConfigurer> subrouterConfigurers;
    private final List<RouterHandler> routerHandlers;
    private final List<Handler<Router>> simpleRouterHandlers;
    private final Configurer<HttpServerOptions> httpServerOptionsConfigurer;

    public HttpServerVerticle(JsonObject config,
                              List<RouterConfigurer> subrouterConfigurers,
                              List<RouterHandler> routerHandlers,
                              List<Handler<Router>> simpleRouterHandlers,
                              Configurer<HttpServerOptions> httpServerOptionsConfigurer) {
        this.config = config;
        this.subrouterConfigurers = subrouterConfigurers;
        this.routerHandlers = routerHandlers;
        this.simpleRouterHandlers = simpleRouterHandlers;
        this.httpServerOptionsConfigurer = Objects.requireNonNullElseGet(httpServerOptionsConfigurer, () -> c -> new HttpServerOptions());
    }

    public static HttpServerVerticleBuilder builder() {
        return new HttpServerVerticleBuilder();
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        subrouters()
            .flatMap(this::commonRouter)
            .onSuccess(router ->
                this.vertx.createHttpServer(this.httpServerOptionsConfigurer.configure(this.config))
                    .requestHandler(router)
                    .listen(ConfigUtil.getInteger(CONFIG_HTTP_SERVER_PORT, config, DEFAULT_PORT))
                    .onSuccess(server -> {
                        LOGGER.info("Http server started on port {}", server.actualPort());
                        startPromise.complete();
                    })
                    .onFailure(t -> {
                        LOGGER.error("Error starting http server", t);
                        startPromise.fail(t);
                    })
            )
            .onFailure(t -> {
                LOGGER.error("Error starting http server", t);
                startPromise.fail(t);
            });

    }

    private Future<List<Pair<String, Router>>> subrouters() {
        Promise<List<Pair<String, Router>>> promise = Promise.promise();

        if (subrouterConfigurers != null) {
            List<Future<Pair<String, Router>>> routers = subrouterConfigurers.stream()
                .map(routerConfigurer -> routerConfigurer.configure(this.config))
                .collect(Collectors.toList());
            CompositeFuture.join(Collections.unmodifiableList(routers))
                .onComplete(future -> {
                    if (future.failed()) {
                        LOGGER.error("Http server initialization failed while configuring routes", future.cause());
                        promise.fail(future.cause());
                    } else {
                        promise.complete(future.result().list());
                    }
                });
        } else {
            promise.complete(List.of());
        }

        return promise.future();
    }

    private Future<Router> commonRouter(List<Pair<String, Router>> routers) {
        Promise<Router> promise = Promise.promise();
        Router router = Router.router(vertx);

        //add custom route customizers
        if (this.routerHandlers != null) {
            this.routerHandlers.forEach(rh -> rh.apply(this.vertx, router, config));
        }

        //add simple route handlers
        if (this.simpleRouterHandlers != null) {
            this.simpleRouterHandlers.forEach(h -> h.handle(router));
        }

        //mount created subrouters
        for (Pair<String, Router> routerPair : routers) {
            router.mountSubRouter(routerPair.getKey(), routerPair.getRight());
        }

        promise.complete(router);

        return promise.future();
    }
}
