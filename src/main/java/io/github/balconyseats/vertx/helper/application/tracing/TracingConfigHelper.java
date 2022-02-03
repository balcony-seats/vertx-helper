package io.github.balconyseats.vertx.helper.application.tracing;

import io.github.balconyseats.vertx.helper.util.JsonObjectHelper;
import io.opentracing.Span;
import io.reactiverse.contextual.logging.ContextualData;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.tracing.opentracing.OpenTracingOptions;
import io.vertx.tracing.opentracing.OpenTracingUtil;
import io.vertx.tracing.zipkin.HttpSenderOptions;
import io.vertx.tracing.zipkin.ZipkinTracer;
import io.vertx.tracing.zipkin.ZipkinTracingOptions;

import java.util.Optional;

/**
 * Helper for tracing configuration.
 *
 * It uses the following configuration data:
 *
 * <pre>
 *     tracing:
 *       zipkin:
 *         enabled: true
 *         serviceName: 'serviceName'
 *         endpoint: 'http://localhost:8082
 *       opentracing:
 *         enabled: true
 * </pre>
 */
public class TracingConfigHelper {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";
    public static final String PARENT_ID_KEY = "parentId";
    public static final String[] TRACE_KEYS = new String[] {TRACE_ID_KEY, SPAN_ID_KEY, PARENT_ID_KEY};

    public static final String CONFIG_TRACING_ZIPKIN_ENABLED = "/tracing/zipkin/enabled";
    public static final String CONFIG_TRACING_ZIPKIN_ENDPOINT = "/tracing/zipkin/endpoint";
    public static final String CONFIG_TRACING_ZIPKIN_SERVICE_NAME = "/tracing/zipkin/serviceName";
    public static final String CONFIG_TRACING_OPENTRACING_ENABLED = "/tracing/opentracing/enabled";

    /**
     * Creates tracing options for {@link Vertx} instance.
     * @param config configuration
     * @return {@link TracingOptions} instance
     */
    public static TracingOptions createTracingOptions(JsonObject config) {
        if (isZipkinEnabled(config)) {
            final HttpSenderOptions httpSenderOptions = new HttpSenderOptions()
                .setSenderEndpoint( JsonObjectHelper.getString(CONFIG_TRACING_ZIPKIN_ENDPOINT, config));
            httpSenderOptions.setTracingPolicy(TracingPolicy.ALWAYS);

            return new ZipkinTracingOptions()
                .setServiceName(JsonObjectHelper.getString(CONFIG_TRACING_ZIPKIN_SERVICE_NAME, config))
                .setSenderOptions(httpSenderOptions);
        } else if (isOpenTracindEnabled(config)) {
            return new OpenTracingOptions();
        }
        return null;
    }

    /**
     * Propagates contextual data over the EventBus using interceptors.
     * @param vertx {@link Vertx} instance
     * @param config configuration
     */
    public static void contextualDataForEventBus(Vertx vertx, JsonObject config) {
        // Propagate contextual data over the EventBus using interceptors.
        // https://reactiverse.io/reactiverse-contextual-logging/#_propagation
        if (isZipkinEnabled(config) || isOpenTracindEnabled(config)) {
            vertx.eventBus().addOutboundInterceptor(event -> {
                for (String traceKey : TRACE_KEYS) {
                    String val = ContextualData.get(traceKey);
                    if (val != null) {
                        event.message().headers().add(traceKey, val);
                    }
                }
                event.next();
            });

            vertx.eventBus().addInboundInterceptor(event -> {
                for (String traceKey : TRACE_KEYS) {
                    String val = event.message().headers().get(traceKey);
                    if (val != null) {
                        ContextualData.put(traceKey, val);
                    }
                }
                event.next();
            });
        }
    }

    /**
     * Sets contextual data for logging based on configuration.
     * @param config configuration object
     */
    public static void setContextualData(JsonObject config) {
        if (isZipkinEnabled(config)) {
            Optional.ofNullable(ZipkinTracer.activeContext())
                .ifPresent(tc -> {
                    setIfNotNull(TRACE_ID_KEY, tc.traceIdString());
                    setIfNotNull(SPAN_ID_KEY, tc.spanIdString());
                    setIfNotNull(PARENT_ID_KEY, tc.parentIdString());
                });
        } else if (isOpenTracindEnabled(config)) {
            Optional.ofNullable(OpenTracingUtil.getSpan())
                .map(Span::context)
                .ifPresent(c -> {
                    setIfNotNull(TRACE_ID_KEY, c.toTraceId());
                    setIfNotNull(SPAN_ID_KEY, c.toSpanId());
                });
        }
    }

    private static Boolean isOpenTracindEnabled(JsonObject config) {
        return JsonObjectHelper.getBoolean(CONFIG_TRACING_OPENTRACING_ENABLED, config);
    }

    private static Boolean isZipkinEnabled(JsonObject config) {
        return JsonObjectHelper.getBoolean(CONFIG_TRACING_ZIPKIN_ENABLED, config);
    }

    private static void setIfNotNull(String key, String value) {
        if (value != null) {
            ContextualData.put(key, value);
        }
    }

}
