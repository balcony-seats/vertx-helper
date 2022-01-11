package io.github.balconyseats.vertx.helper.database.migration;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.commons.text.StringSubstitutor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Configures and migrates database using flyway library.
 *
 * <br>
 * <pre>
 * database:
 *   host: 'host'
 *   port: 5432
 *   database: 'db'
 *   user: 'user'
 *   password: 'password'
 *   migration:
 *     enabled: true
 *     type: 'flyway'
 *     jdbcUrl: jdbc:postgresql://${host}:${port}/${database}
 *
 * </pre>
 *
 */
public class ConfigFlywayDatabaseMigration implements DatabaseMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFlywayDatabaseMigration.class);

    public static final String FLYWAY_TYPE = "flyway";

    /**
     *
     * @param config {@link JsonObject} with `database` object from configuration
     * @return {@link Future} with migration result
     */
    public Future<Void> migrate(JsonObject config) {
        Promise<Void> promise = Promise.promise();

        JsonObject migrationConf = config.getJsonObject("migration");

        try {
            String jdbcUrl = StringSubstitutor.replace(migrationConf.getString("jdbcUrl"), config.getMap());
            MigrateResult migrateResult = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(jdbcUrl, config.getString("user"), config.getString("password"))
                .load()
                .migrate();

            LOGGER.info("Migration finished. Initial schema version: '{}', target schema version '{}'. Migrations executed: '{}'",
                migrateResult.initialSchemaVersion,
                migrateResult.targetSchemaVersion,
                migrateResult.migrationsExecuted
                );

            promise.complete();

        } catch (Exception e) {
            promise.fail(e);
        }

        return promise.future();
    }

    @Override
    public String type() {
        return FLYWAY_TYPE;
    }
}
