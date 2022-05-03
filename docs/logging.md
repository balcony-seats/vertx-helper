## Logging

### Pattern with tracing information

Use:

```xml
<conversionRule conversionWord="vcl" converterClass="io.reactiverse.contextual.logging.LogbackConverter"/>

<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{ISO8601} %highlight(%-5level) [%blue(%t)] %yellow(%C{1.}) [traceId: %vcl{traceId}] [spanId: %vcl{spanId}]: %msg%n%throwable</pattern>
    </encoder>
</appender>
```

### Logstash with tracing information

Use:

```xml
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="io.github.balconyseats.vertx.helper.application.logging.TracingLogstashEncoder"/>
    </appender>
```