# Configuration

This is guide how to configure configuration sources and load configuration.

Configuration loader `io.github.balconyseats.vertx.helper.config.ConfigurationLoader` purpose is to load configuration
from defined `io.vertx.config.ConfigRetrieverOptions` which can be defined through constructor or
using `io.github.balconyseats.vertx.helper.config.ConfigurationLoaderBuilder` to configure and instantiate
configuration loader.

## How to use it

Below is example hot to load configuration and configure it through builder.
For Vertx configuration details see [https://vertx.io/docs/vertx-config](https://vertx.io/docs/vertx-config)

```java
ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
    .setType("file")
    .setFormat("json")
    .setConfig(new JsonObject().put("path", "conf/sysprops.json"));

ConfigurationLoader.builder()
    .enableFeature(ConfigurationLoaderBuilder.FeatureType.DEFAULT_VERTX_STORES) // enable feature
    .disableFeature(ConfigurationLoaderBuilder.CLASSPATH_CONFIG) // disable feature
    .addStore(customConfigStoreOptions) // custom config store options
    .addConfigPath(filePath) // some file path relative or absolute
    .build() // create loader
    .load() // load config
    .onSuccess(conf -> {
        // do something with conf JsonObject
    })
    .onFailure(cause -> {
        // load failed    
    })

```

## Configuration loader

Configuration loader usage example without builder:

```java

ConfigRetrieverOptions configRetrieverOptions = createConfigRetrieverOptions();

new ConfigurationLoader(configRetrieverOptions)
    .load()
    .onSuccess(conf -> {
    // do something with config
    })
    .onFailure(cause -> {
    // load failed    
    })

```

For `io.vertx.config.ConfigRetrieverOptions` details and how to use it see 
[https://vertx.io/docs/vertx-config/java/#_using_the_retrieve_configuration](https://vertx.io/docs/vertx-config/java/#_using_the_retrieve_configuration).


## Configuration loader builder

Convenient way to configure and create configuration loader with default stores and providing additional 
configuration stores or disable/enable defaults.
 

### How it works

Configuration can be load from multiple different locations:
 
 * classpath files
 * system files provided by environment variable or java system property
 * default vertx stores, see[https://vertx.io/docs/vertx-config/java](https://vertx.io/docs/vertx-config/java)
 * user defined stores

When multiple stores are used then the configuration data is overloaded in specified order 
(lower ordinal is overloaded by higher ordinal):

 1. `application.properties`, `application.yml` or `application.json` classpath files 
 2. default vertx stores (disabled by default)
 3. `vertx.configuration` system property
 4. `VERTX_CONFIGURATION` environment variable 
 5. user defined stores

See [https://vertx.io/docs/vertx-config/java/#_overloading_rules](https://vertx.io/docs/vertx-config/java/#_overloading_rules)
for details.


#### Classpath files

When classpath config is enabled then configuration is loaded from below files if file exist in overload order:

 1. application.properties
 2. application.yml
 3. application.json

Classpath configuration can be disabled by:
 * disable feature `CLASSPATH_CONFIG` in `ConfigurationLoaderBuilder`
 * with system property `vertx.configuration.classpath.disabled` set to `false`
 * with environment variable `VERTX_CONFIGURATION_CLASSPATH_DISABLED` set to `false`

#### System files

To define system file locations use system property `vertx.configuration` 
or environment variable `VERTX_CONFIGURATION` which can contain multiple configuration files delimited with `:`.
If path contains `:` then it needs to be enclosed in single or double quotes or escaped with `\`.


For system property:

`vertx.configuration=/config/config-1.yml:/config/config-2.yml:'file://some/path/config.yml':file\://some/path/config.json`

For environment variable:
`VERTX_CONFIGURATION=/config/config-1.yml:/config/config-2.yml:'file://some/path/config.yml':file\://some/path/config.json`


If path contains `:` then it needs to be enclosed in single or double quotes or escaped with `\`.

#### Default vertx stores

Default vertx stores is enabled by feature `DEFAULT_VERTX_STORES`.

#### User defined stores

User defined stores are defined by providing `io.vertx.config.ConfigStoreOptions` for details and how to use it see
[https://vertx.io/docs/vertx-config/java](https://vertx.io/docs/vertx-config/java/).


### ConfigurationLoaderBuilder features

| Name               | Default   | Description                                                                                                       |
|--------------------|:---------:|-------------------------------------------------------------------------------------------------------------------|
|CLASSPATH_CONFIG    | enabled   | Use config files from classpath, if file exists:  `application.properties`,`application.yml`, `application.json`. |
|DEFAULT_VERTX_STORES| disabled  | Use default vertx stores (https://vertx.io/docs/vertx-config/java/#_using_the_retrieve_configuration).            |

