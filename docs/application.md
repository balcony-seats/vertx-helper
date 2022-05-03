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
* oracle (https://vertx.io/docs/vertx-oracle-client/java/)
* mssql (https://vertx.io/docs/vertx-mssql-client/java/)
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

**Oracle**

```yaml
database:
    type: 'oracle'
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

**MSSQL**

```yaml
database:
    type: 'mssql'
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
    type: 'jdbc'
    jdbc-url: 'jdbc:postgresql://localhost:5432/db'
    user: 'username'
    password: 'password'
    pool:
      max-size: 5
      max-wait-queue-size: -1
      connection-timeout: 30
```


### Database migration

Initialize and migrate database using provided and configured `io.github.balconyseats.vertx.helper.database.migration.DatabaseMigration`.  
Flyway database migration is currently implemented with `io.github.balconyseats.vertx.helper.database.migration.ConfigFlywayDatabaseMigration`.

To use database migration create `io.github.balconyseats.vertx.helper.database.migration.ConfigDatabaseMigrationInitializationHandler`  
and add it to `VertxApplication` as pre or post handler.

```java

VertxApplication vertxApplication = VertxApplication.builder()
    ...
    .preHandler(InitializationHandler.composite(
            ConfigDatabaseMigrationInitializationHandler.instance()
        )
    ) // add here 
    ...
    .postHandler(InitializationHandler.composite(
            ConfigDatabaseMigrationInitializationHandler.instance()
        )
    ) // or here
    .build();

```

It uses user, password, host, etc. properties from database configuration properties:

```yaml
database:
  type: 'postgresql'
  host: 'host'
  port: 5432
  database: 'db'
  user: 'username'
  password: 'password'
  migration:
    enabled: true
    type: flyway
    jdbcUrl: 'jdbc:postgresql://${host}:${port}/${database}'

```