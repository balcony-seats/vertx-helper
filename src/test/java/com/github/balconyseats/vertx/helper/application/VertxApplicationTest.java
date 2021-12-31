package com.github.balconyseats.vertx.helper.application;

import com.github.balconyseats.vertx.helper.application.configurer.options.ConfigurationMetricsOptionsConfigurer;
import com.github.balconyseats.vertx.helper.application.configurer.options.ConfigurationTracingOptionsConfigurer;
import com.github.balconyseats.vertx.helper.http.handlers.ConfigurationHealthCheckHandler;
import com.github.balconyseats.vertx.helper.application.configurer.InitializationContextConfigurer;
import com.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import com.github.balconyseats.vertx.helper.application.configurer.VerticleConfigurer;
import com.github.balconyseats.vertx.helper.application.configurer.VertxOptionsConfigurer;
import com.github.balconyseats.vertx.helper.http.HttpServerVerticle;
import com.github.balconyseats.vertx.helper.http.handlers.ContextualLoggingRouterHandler;
import com.github.balconyseats.vertx.helper.http.handlers.MetricsHandler;
import com.github.balconyseats.vertx.helper.application.tracing.EventBusContextualDataProcessor;
import com.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import com.github.balconyseats.vertx.helper.config.ConfigurationLoader;
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
    public void testCreate_shouldConfigureAll(VertxTestContext testContext) {

        VerticleConfigurer httpServerVerticleConfigurer = (ctx, config) ->
            HttpServerVerticle.builder()
                .config(config)
                .routerHandlers(List.of(
                        new ContextualLoggingRouterHandler(),
                        (vertx, router, conf) -> {
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
                        new ConfigurationHealthCheckHandler(),
                        new MetricsHandler(),
                        (vertx, router, conf) -> router.get("/ping").handler(rc -> rc.response().setStatusCode(200).end("pong"))
                    )
                )
                .build();


        VertxApplication vertxApplication = VertxApplication.builder()
            .configurationLoader(ConfigurationLoader.builder().build())
            .vertxOptionsConfigurer(VertxOptionsConfigurer.composite(
                new ConfigurationMetricsOptionsConfigurer(),
                new ConfigurationTracingOptionsConfigurer()
                )
            )
            .preHandler(InitializationHandler.composite(new EventBusContextualDataProcessor()))
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