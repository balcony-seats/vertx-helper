# Vert.X Application support

## About

Library to help create and configure Vert.X application with common options, for example Metrics, Tracing, etc...

## Configuration loading

Configuration is loaded from classpath files, system files provided by environment variable or java system property, 
from default vertx stores (https://vertx.io/docs/vertx-config/java) and code defined stores.

Configuration data are loaded and overloaded in the following order (see: https://vertx.io/docs/vertx-config/java/#_overloading_rules):

 * classpath files: `application.properties`, `application.yml` or `application.json`
 * default vertx stores (this is disabled by default)
 * from system property variable: `vertx.configuration`
 * from env property variable: `VERTX_CONFIGURATION`
 * from code defined stores

Load configuration example:

```java
ConfigurationLoader.builder()
    .enableFeature(ConfigurationLoaderBuilder.FeatureType.<some feature type>) //enable feature
    .addStore(customConfigStoreOptions) //custom config store options
    .addStore(filePath) // some filepath
    .build() // create loader
    .load() // load config
    .onSuccess(conf -> {
        //do something with config
    })
    .onFailure(cause -> {
        //load failed    
    })
```

`ConfigurationLoader` is created with `ConfigurationLoaderBuilder` which is configurable with features and custom stores.

### Configuration loader builder features

| Name               | Default   | Description                                                                                                |
|--------------------|:---------:|------------------------------------------------------------------------------------------------------------|
|CLASSPATH_CONFIG    | enabled   | Use config files from classpath if exist:  `application.properties`, `application.yml`, `application.json` |
|DEFAULT_VERTX_STORES| disabled  | Use default vertx stores https://vertx.io/docs/vertx-config/java/#_using_the_retrieve_configuration        |


### Configuration files paths

System property and environment variable can contain multiple configuration files delimited with `:`.

For example: 

`/config/config-1.yml:/config/config-2.yml:'file://some/path/config.yml':file\://some/path/config.json`

If path contains `:` then it needs to be enclosed in single or double quotes or escaped with `\`.


## Vertx application 

Vertx application is helper for configure and create `Vertx` instance and deploy verticles.

Initialization flow:

```
    --------------------------------
    |      LOAD CONFIGURATION      |
    --------------------------------
                   | 
                   V
    --------------------------------
    |    CREATE VertX INSTANCE     |
    --------------------------------
                   | 
                   V
    --------------------------------
    | CREATE InitializationContext |
    --------------------------------
                   | 
                   V
    --------------------------------
    |          PRE HANDLE          |
    --------------------------------
                   | 
                   V
    --------------------------------
    |       DEPLOY VERTICLES       |
    --------------------------------
                   | 
                   V
    --------------------------------
    |         POST HANDLER         |
    --------------------------------
```

Example how to create application:

```java

VertxApplication vertxApplication = VertxApplication.builder()
    .configurationLoader(ConfigurationLoader.builder().build()) // add configuration loader
    .vertxOptionsConfigurer(vertxOptionsConfigurer) // add vertxOptionsConfigurer
    .initializationContextConfigurer(initializationContextConfigurer) // add application context configurer
    .preHandler(preHandler) // add pre handler
    .verticleConfigurers(httpServerVerticleConfigurer) // add verticle configurers
    .postHandler(postHandler) // add post handler
    .build();

```

### Load configuration

Load configuration with provided `ConfigurationLoader`.

### Create VertX instance

Configures VertX instance with `VertxOptionsConfigurer` which accept two argument `VertxOptions` and 
configuration as `JsonObject`. 
For details how to configure `VertxOptions` see: https://vertx.io/docs/vertx-core/java/
V

```java

VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .vertxOptionsConfigurer((VertxOptions vo, JsonObject config) -> {
        //
    }) 
    ...

```

To chain multiple configurers use:

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

### Initialization context

Initilization context configurer step is for creating singletons or other objects that can be used 
in verticle deployment stage or processor stages. For example application context can have reference to
database pool instance.  
To configure `InitializationContext` implement `InitializationContextConfigurer` interface as java lambda function or standard 
java class.

```java
VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .initializationContextConfigurer(
        InitializationContextConfigurer.composite(
            (initializationContext, vertx, config) -> initializationContext.put("foo", new Foo()), //adds foo to context
            (initializationContext, vertx, config) -> initializationContext.put("bar", new Bar()) //adds bar to context
        )
    )
    ...
```

### Pre verticles deployment handler

This step adds possibility to do something before verticles deployment for example database migration
or something similar.


### Verticle deployments

Configures and instantiate `Verticle` instances and deploy it to vertx instance.

### Post verticles deployment handler

This step adds possibility to do something after verticles deployment for example database migration
or something similar.

