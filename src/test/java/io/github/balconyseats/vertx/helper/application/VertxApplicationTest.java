package io.github.balconyseats.vertx.helper.application;

import io.github.balconyseats.vertx.helper.application.configurer.options.ConfigMetricsOptionsConfigurer;
import io.github.balconyseats.vertx.helper.application.configurer.options.ConfigTracingOptionsConfigurer;
import io.github.balconyseats.vertx.helper.http.handlers.ConfigHealthCheckHandler;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationContextConfigurer;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.github.balconyseats.vertx.helper.application.configurer.VerticleConfigurer;
import io.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import io.github.balconyseats.vertx.helper.http.HttpServerVerticle;
import io.github.balconyseats.vertx.helper.http.handlers.ContextualLoggingRouterHandler;
import io.github.balconyseats.vertx.helper.http.handlers.ConfigMetricsHandler;
import io.github.balconyseats.vertx.helper.application.tracing.EventBusContextualDataHandler;
import io.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import io.github.balconyseats.vertx.helper.config.ConfigurationLoader;
import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.CompositeFuture;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ExtendWith(VertxExtension.class)
class VertxApplicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertxApplicationTest.class);


    @Test
    public void testConstructor_whenAllArgsIsNull_expectDefaults() {

        VertxApplication vertxApplication = new VertxApplication(null, null, null, null, null, null);

        Assertions.assertThat(vertxApplication).isNotNull();
        Assertions.assertThat(vertxApplication).extracting("configurationLoader").isNotNull();
        Assertions.assertThat(vertxApplication).extracting("verticleConfigurers").isNotNull();
        Assertions.assertThat(vertxApplication).extracting("vertxOptionsConfigurer").isNotNull();
        Assertions.assertThat(vertxApplication).extracting("initializationContextConfigurer").isNotNull();
        Assertions.assertThat(vertxApplication).extracting("preHandler").isNotNull();
        Assertions.assertThat(vertxApplication).extracting("postHandler").isNotNull();

    }

    @Test
    public void testCreate_whenDefaultBuilder_expectDefaults(VertxTestContext testContext) {

        VertxApplication vertxApplication = VertxApplication.builder().build();

        vertxApplication.create().onComplete(testContext.succeeding(vertxContext -> testContext.verify(() -> {
            Assertions.assertThat(vertxContext.getVertx()).isNotNull();
            Assertions.assertThat(vertxContext.getConfiguration()).isNotNull();
            Assertions.assertThat(vertxContext.getInitializationContext()).isNotNull();
            testContext.completeNow();

        })));
    }

    @Test
    public void testCreate_shouldConfigureAll(VertxTestContext testContext) {

        VerticleConfigurer httpServerVerticleConfigurer = (vertx, ctx, config) ->
            HttpServerVerticle.builder()
                .config(config)
                .routerHandlers(List.of(
                        new ContextualLoggingRouterHandler(),
                        (v, router, conf) -> {
                            if (LOGGER.isDebugEnabled()) {
                                router.route().handler(rc -> {
                                    LOGGER.debug("Received request: {} {}, headers: {}, body: '{}'",
                                        rc.request().method(), rc.normalizedPath(), rc.request().headers(), rc.getBodyAsString());
                                    Assertions.assertThat(ContextualData.get(TracingConfigHelper.TRACE_ID_KEY)).isNotBlank();
                                    Assertions.assertThat(ContextualData.get(TracingConfigHelper.SPAN_ID_KEY)).isNotBlank();
                                    rc.next();
                                });
                            }
                        },
                        new ConfigHealthCheckHandler(),
                        new ConfigMetricsHandler(),
                        (v, router, conf) -> router.get("/ping").handler(rc -> rc.response().setStatusCode(200).end("pong"))
                    )
                )
                .build();


        VertxApplication vertxApplication = VertxApplication.builder()
            .configurationLoader(ConfigurationLoader.builder().build())
            .vertxOptionsConfigurer(VertxOptionsConfigurer.composite(
                new ConfigMetricsOptionsConfigurer(),
                new ConfigTracingOptionsConfigurer()
                )
            )
            .preHandler(InitializationHandler.composite(new EventBusContextualDataHandler()))
            .postHandler(InitializationHandler.composite())
            .initializationContextConfigurer(InitializationContextConfigurer.composite())
            .verticleConfigurers(httpServerVerticleConfigurer)
            .build();


        vertxApplication.create()
            .onComplete(testContext.succeeding(vertxContext -> testContext.verify(() -> {
                Assertions.assertThat(vertxContext.getVertx()).isNotNull();
                Assertions.assertThat(vertxContext.getInitializationContext()).isNotNull();
                Assertions.assertThat(vertxContext.getConfiguration()).isNotNull();

                WebClient webClient = WebClient.create(vertxContext.getVertx());
                CompositeFuture.join(List.of(
                        webClient.get(8080, "localhost", "/health").send().map(resp -> Pair.of("health", resp)),
                        webClient.get(8080, "localhost", "/metrics").send().map(resp -> Pair.of("metrics", resp)),
                        webClient.get(8080, "localhost", "/ping").send().map(resp -> Pair.of("ping", resp))

                    ))
                    .onComplete(testContext.succeeding(res -> testContext.verify(() -> {
                        Pair<String, HttpResponse<Buffer>> healthResult = res.resultAt(0);
                        Pair<String, HttpResponse<Buffer>> metricsResult = res.resultAt(1);
                        Pair<String, HttpResponse<Buffer>> pingResult = res.resultAt(2);

                        Assertions.assertThat(healthResult.getLeft()).isEqualTo("health");
                        Assertions.assertThat(healthResult.getRight().statusCode()).isEqualTo(200);
                        Assertions.assertThat(healthResult.getRight().bodyAsString()).isEqualTo("{\"status\":\"UP\",\"checks\":[{\"id\":\"status\",\"status\":\"UP\"}],\"outcome\":\"UP\"}");

                        Assertions.assertThat(metricsResult.getLeft()).isEqualTo("metrics");
                        Assertions.assertThat(metricsResult.getRight().statusCode()).isEqualTo(200);
                        Assertions.assertThat(metricsResult.getRight().bodyAsString()).isNotBlank();

                        Assertions.assertThat(pingResult.getLeft()).isEqualTo("ping");
                        Assertions.assertThat(pingResult.getRight().statusCode()).isEqualTo(200);
                        Assertions.assertThat(pingResult.getRight().bodyAsString()).isEqualTo("pong");

                        testContext.completeNow();

                    })));

            })));

    }

}