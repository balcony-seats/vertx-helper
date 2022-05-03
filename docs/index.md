# Vert.X Helper Library

## About

A library to help create and configure Vert.X application with common options, like Metrics, Tracing, etc.


## Content

 1. [Configuration](configuration.md)
 2. [VertX Application](application.md)
 3. [Logging](logging.md)


## Maven dependencies

To start using vertx helper add this dependency to pom.xml but be aware that only:
 * vertx-core
 * vertx-config
 * vertx-config-yaml

will be resolved transiently, other vertx modules such as `vertx-web`, etc. needs to be added to 
pom.xml manually if is used in application.


```
<dependency>
    <groupId>io.github.balcony-seats</groupId>
    <artifactId>vertx-helper</artifactId>
    <version>${vertx-helper.version}</version>
</dependency>
```

