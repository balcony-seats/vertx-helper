package io.github.balconyseats.vertx.helper.application;

import io.github.balconyseats.vertx.helper.application.configurer.InitializationContextConfigurer;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.github.balconyseats.vertx.helper.application.configurer.VerticleConfigurer;
import io.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import io.github.balconyseats.vertx.helper.config.ConfigurationLoader;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common Vert.X application creator.
 *
 * <p></p>
 * Initialization flow:
 * <pre>
 *     load configuration
 *            |
 *            V
 *    create vertx instance
 *            |
 *            V
 *  create initialization context
 *            |
 *            V
 *  verticle pre-deployment handler
 *            |
 *            V
 *     deploy verticles
 *            |
 *            V
 *  verticle post-deployment handler
 * </pre>
 *
 */
public class VertxApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertxApplication.class);

    private final ConfigurationLoader configurationLoader;
    private final List<VerticleConfigurer> verticleConfigurers;
    private final VertxOptionsConfigurer vertxOptionsConfigurer;
    private final InitializationContextConfigurer initializationContextConfigurer;
    private final InitializationHandler preHandler;
    private final InitializationHandler postHandler;

    private Vertx vertx;

    public VertxApplication(ConfigurationLoader configurationLoader,
                            List<VerticleConfigurer> verticleConfigurers,
                            VertxOptionsConfigurer vertxOptionsConfigurer,
                            InitializationContextConfigurer initializationContextConfigurer,
                            InitializationHandler beforeHandler,
                            InitializationHandler afterHandler
    ) {
        this.configurationLoader = configurationLoader;
        this.verticleConfigurers = verticleConfigurers;
        this.vertxOptionsConfigurer = vertxOptionsConfigurer;
        this.initializationContextConfigurer = initializationContextConfigurer;
        this.preHandler = beforeHandler;
        this.postHandler = afterHandler;
    }

    /**
     * Configures and creates Vertx instance, and configures and deploy verticles.
     *
     * @return future with {@link VertxContext}
     */
    public Future<VertxContext> create() {
        Promise<VertxContext> promise = Promise.promise();
        if (configurationLoader != null) {
            configurationLoader.load()
                .onSuccess(config ->
                    configureAndDeploy(config)
                        .onSuccess(res -> promise.complete(VertxContext.of(res.getLeft(), res.getRight(), config)))
                        .onFailure(promise::fail)
                )
                .onFailure(error -> {
                    LOGGER.error("Error while loading configuration", error);
                    promise.fail(error);
                });
        } else {
            JsonObject config = new JsonObject();
            configureAndDeploy(config)
                .onSuccess(res -> promise.complete(VertxContext.of(res.getLeft(), res.getRight(), config)))
                .onFailure(promise::fail);
        }
        return promise.future();
    }

    /**
     * Closes Vertx instance.
     * @return {@link Future} with completed result
     */
    public Future<Void> close() {
        if (this.vertx != null) {
            return vertx.close();
        }
        Promise<Void> promise = Promise.promise();
        promise.complete();
        return promise.future();
    }

    /**
     * Configures VertX, and configures and deploys verticles.
     * @param config configuration
     * @return {@link Future} with VertX instance
     */
    private Future<Pair<Vertx, InitializationContext>> configureAndDeploy(JsonObject config) {
        Promise<Pair<Vertx, InitializationContext>> promise = Promise.promise();

        this.vertx = this.createVertx(config); // create vertx

        createInitializationContext(this.vertx, config) // create context
            .flatMap(ictx -> preHandler.handle(this.vertx, ictx, config).map(__ -> ictx)) // before deployment
            .flatMap(ictx ->
                deployVerticles(this.vertx, ictx, config)
                    .map(di -> {
                        LOGGER.info("Deployed verticles: {}", di);
                        return ictx;
                    })
            )
            .flatMap(ictx -> postHandler.handle(this.vertx, ictx, config).map(__ -> ictx)) // after deployment
            .onSuccess(ictx -> promise.complete(Pair.of(this.vertx, ictx)))
            .onFailure(promise::fail);

        return promise.future();
    }

    /**
     * Configures and creates new {@link Vertx} instance.
     *
     * @param configuration configuration object
     * @return {@link Vertx} instance
     */
    protected Vertx createVertx(JsonObject configuration) {
        VertxOptions vertxOptions = new VertxOptions();

        if (vertxOptionsConfigurer != null) {
            this.vertxOptionsConfigurer.configure(vertxOptions, configuration);
        }

        return Vertx.vertx(vertxOptions);
    }

    /**
     * Creates {@link InitializationContext} future.
     *
     * @param vertx      vertx instance
     * @param jsonObject configuration
     * @return {@link Future} with {@link InitializationContext}
     */
    protected Future<InitializationContext> createInitializationContext(Vertx vertx, JsonObject jsonObject) {
        if (this.initializationContextConfigurer != null) {
            return this.initializationContextConfigurer.configure(new InitializationContext(), vertx, jsonObject);
        }
        Promise<InitializationContext> promise = Promise.promise();
        promise.complete(new InitializationContext());
        return promise.future();
    }

    /**
     * Creates and deploys verticles.
     *
     * Result is a {@link Future} of deployment results as {@link Triple}:
     * first element is verticle class name,
     * second element is deployment id,
     * and third element is deployment result flag.
     *
     * @param vertx              Vertx instance
     * @param initializationContext initializtion context object
     * @param config             configuration
     * @return {@link Future} with deployment result
     */
    protected Future<List<Triple<String, String, Boolean>>> deployVerticles(Vertx vertx, InitializationContext initializationContext, JsonObject config) {
        Promise<List<Triple<String, String, Boolean>>> promise = Promise.promise();

        Map<String, Future<String>> deployments = verticleConfigurers.stream()
            .map(c -> c.create(initializationContext, config))
            .collect(Collectors.toMap(ClassUtils::getSimpleName, vertx::deployVerticle));

        CompositeFuture.join(List.copyOf(deployments.values()))
            .onComplete(r -> {

                List<Triple<String, String, Boolean>> result = deployments.entrySet()
                    .stream()
                    .map(e -> {
                        String name = e.getKey();
                        Future<String> future = e.getValue();
                        if (future.failed()) {
                            LOGGER.error("Starting verticle '{}' failed", name, future.cause());
                            return Triple.<String, String, Boolean>of(name, null, false);
                        } else {
                            LOGGER.debug("Verticle '{}' ('{}') started", name, future.result());
                            return Triple.of(name, future.result(), true);
                        }
                    })
                    .collect(Collectors.toList());

                if (result.stream().allMatch(Triple::getRight)) {
                    promise.complete(result);
                } else {
                    promise.fail("Some verticles were not started successfully");
                }

            });

        return promise.future();
    }

    public static VertxApplicationBuilder builder() {
        return new VertxApplicationBuilder();
    }

}
