package io.github.balconyseats.vertx.helper.application.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import io.github.balconyseats.vertx.helper.application.tracing.TracingConfigHelper;
import io.reactiverse.contextual.logging.ContextualData;
import net.logstash.logback.composite.AbstractJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;

public class TracingJsonProvider extends AbstractJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator jsonGenerator, ILoggingEvent iLoggingEvent) throws IOException {
        for (String traceKey : TracingConfigHelper.TRACE_KEYS) {
            String val = ContextualData.get(traceKey);
            if (val != null) {
                JsonWritingUtils.writeStringField(jsonGenerator, traceKey, val);
            }
        }

    }
}
