package io.github.balconyseats.vertx.helper.application.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.LogstashFormatter;
import net.logstash.logback.composite.AbstractCompositeJsonFormatter;
import net.logstash.logback.encoder.LogstashEncoder;

public class TracingLogstashEncoder extends LogstashEncoder {

    @Override
    protected AbstractCompositeJsonFormatter<ILoggingEvent> createFormatter() {
        LogstashFormatter logstashFormatter = new LogstashFormatter(this);
        logstashFormatter.addProvider(new TracingJsonProvider());
        return logstashFormatter;
    }

}
