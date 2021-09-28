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

