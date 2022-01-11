# Vert.X Helper Library

## About

A library to help create and configure Vert.X application with common options, like Metrics, Tracing, etc.

## Configuration Loading

Configuration is loaded from:
  * classpath files
  * system files provided by environment variable or java system property
  * default vertx stores (https://vertx.io/docs/vertx-config/java)
  * code defined stores.

Configuration data are loaded and overloaded from the following sources in the specified order
(see: https://vertx.io/docs/vertx-config/java/#_overloading_rules):

 * `application.properties`, `application.yml` or `application.json` classpath files
 * default vertx stores (disabled by default)
 * `vertx.configuration` system property
 * `VERTX_CONFIGURATION` environment variable
 * code defined stores.

Loading configuration example:

```java
ConfigurationLoader.builder()
    .enableFeature(ConfigurationLoaderBuilder.FeatureType.<some feature type>) // enable feature
    .addStore(customConfigStoreOptions) // custom config store options
    .addStore(filePath) // some file path
    .build() // create loader
    .load() // load config
    .onSuccess(conf -> {
        // do something with config
    })
    .onFailure(cause -> {
        // load failed    
    })
```

`ConfigurationLoader` can be created using `ConfigurationLoaderBuilder`, a configurable with features and custom stores.

### ConfigurationLoaderBuilder features

| Name               | Default   | Description                                                                                                       |
|--------------------|:---------:|-------------------------------------------------------------------------------------------------------------------|
|CLASSPATH_CONFIG    | enabled   | Use config files from classpath, if file exists:  `application.properties`,`application.yml`, `application.json`. |
|DEFAULT_VERTX_STORES| disabled  | Use default vertx stores (https://vertx.io/docs/vertx-config/java/#_using_the_retrieve_configuration).            |


### Configuration file paths

`vertx.configuration` system property and `VERTX_CONFIGURATION` environment variable can contain multiple configuration
files delimited with `:`.

E.g.: 

`/config/config-1.yml:/config/config-2.yml:'file://some/path/config.yml':file\://some/path/config.json`

If path contains `:` then it needs to be enclosed in single or double quotes or escaped with `\`.


## VertxApplication 

VertxApplication is a helper for configuring and creating `Vertx` instance and deploying verticles.

Initialization flow:

```
    ----------------------------------
    |      LOAD CONFIGURATION        |
    ----------------------------------
                   | 
                   V
    ----------------------------------
    |    CREATE VertX INSTANCE       |
    ----------------------------------
                   | 
                   V
    ----------------------------------
    | CREATE InitializationContext   |
    ----------------------------------
                   | 
                   V
    ----------------------------------
    |VERTICLE PRE-DEPLOYMENT HANDLER |
    ----------------------------------
                   | 
                   V
    ----------------------------------
    |       DEPLOY VERTICLES         |
    ----------------------------------
                   | 
                   V
    ----------------------------------
    |VERTICLE POST-DEPLOYMENT HANDLER|
    ----------------------------------
```

Creating application example:

```java

VertxApplication vertxApplication = VertxApplication.builder()
    .configurationLoader(ConfigurationLoader.builder().build()) // add configuration loader
    .vertxOptionsConfigurer(vertxOptionsConfigurer) // add vertx options configurer
    .initializationContextConfigurer(initializationContextConfigurer) // add application context configurer
    .preHandler(preHandler) // add pre-deployment handler
    .verticleConfigurers(httpServerVerticleConfigurer) // add verticle configurers
    .postHandler(postHandler) // add post-deployment handler
    .build();
```

### Loading configuration

Configuration loading is done with provided `ConfigurationLoader`.

### Creating VertX instance

VertX instance is configured with `VertxOptionsConfigurer` which accepts two arguments: `VertxOptions` and 
configuration as `JsonObject`. 
For `VertxOptions` details see: https://vertx.io/docs/vertx-core/java/.

```java
VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .vertxOptionsConfigurer((VertxOptions vo, JsonObject config) -> {
        //
    }) 
    ...
```

Multiple options configurers can be chained:

```java
VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .vertxOptionsConfigurer(
        VertxOptionsConfigurer.composite(
          configurer_1,
          configurer_2
        )
    )
    ...
```

### Initialization Context

Initilization context configurer step is used for creating singletons or other objects that can be used 
in verticle deployment stage or verticle pre and post deployment stages.
E.g. initialization context can contain reference to database pool instance.  

To configure `InitializationContext` implement `InitializationContextConfigurer` interface, as java lambda function or
as standard java class.

```java
VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .initializationContextConfigurer(
        InitializationContextConfigurer.composite(
            (initializationContext, vertx, config) -> initializationContext.put("foo", new Foo()), // add foo to context
            (initializationContext, vertx, config) -> initializationContext.put("bar", new Bar()) // add bar to context
        )
    )
    ...
```

### Verticle Pre-deployment Handler

This step adds possibility to do something before verticles are deployed, e.g. database migration.


### Verticle Deployment

Configure and instantiate `Verticle` instances and deploy them to VertX instance.

### Verticle Post-deployment Handler

This step adds possibility to do something after verticles are deployed.


## Components

### Sql pool from configuration

Initialize `io.vertx.sqlclient.Pool` from configuration. 

Supported implementations:
 * postgresql (https://vertx.io/docs/vertx-pg-client/java/)
 * jdbc (https://vertx.io/docs/vertx-jdbc-client/java/)

For creating use `io.github.balconyseats.vertx.helper.database.sql.ConfigSqlPoolHelper`

```java
var pool = io.github.balconyseats.vertx.helper.sql.ConfigSqlPoolHelper(vertx, config);
```

To create pool and add it to `InitializationContext` use `io.github.balconyseats.vertx.helper.database.sql.ConfigSqlPoolInitializationContextHandler`  
`instance()` method which will create pool and add it to context with `sqlpool` key   
or `instance("some_key")` method which will create pool and add it to context with `some_key` key.


```java
VertxApplication vertxApplication = VertxApplication.builder()
    .initializationContextConfigurer(
        InitializationContextConfigurer.composite(
            ConfigSqlPoolInitializationContextHandler.instance()
        )
    )
    ...
```


Configuration example in yaml:

**Postgresql**

```yaml
database:
    type: 'postgresql'
    host: 'host'
    port: 5432
    database: 'db'
    user: 'username'
    password: 'password'
    pool:
      max-size: 5
      max-wait-queue-size: -1
      connection-timeout: 30
```


**JDBC**

```yaml
database:
    type: 'postgresql'
    jdbc-url: 'jdbc:postgresql://localhost:5432/db'
    user: 'username'
    password: 'password'
    pool:
      max-size: 5
      max-wait-queue-size: -1
      connection-timeout: 30
```

