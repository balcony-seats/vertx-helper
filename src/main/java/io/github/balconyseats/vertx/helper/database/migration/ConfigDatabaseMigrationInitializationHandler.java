package io.github.balconyseats.vertx.helper.database.migration;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configure and run migrations on database from configured migration type of {@link DatabaseMigration}.
 *
 * <br>
 *     <pre>
 *         x§§
 *
 *     </pre>
 */
public class ConfigDatabaseMigrationInitializationHandler implements InitializationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDatabaseMigrationInitializationHandler.class);

    private final Map<String, DatabaseMigration> databaseMigrations;

    private ConfigDatabaseMigrationInitializationHandler(DatabaseMigration... databaseMigrations) {
        this.databaseMigrations = Set.of(databaseMigrations)
            .stream()
            .collect(Collectors.toMap(DatabaseMigration::type, dm -> dm));
    }

    @Override
    public Future<Void> handle(Vertx vertx, InitializationContext initializationContext, JsonObject config) {
        JsonObject dbConfig = config.getJsonObject("database");
        JsonObject migrationConf = dbConfig.getJsonObject("migration");
        Boolean migrationEnabled = migrationConf.getBoolean("enabled", false);
        String type = migrationConf.getString("type");

        Promise<Void> promise = Promise.promise();
        if (migrationEnabled && StringUtils.isNotBlank(type)) {

            Optional<DatabaseMigration> databaseMigration = resolve(type);

            if (databaseMigration.isPresent()) {
                databaseMigration.get()
                    .migrate(dbConfig)
                    .onSuccess(__ -> promise.complete())
                    .onFailure(error -> {
                        LOGGER.error("Migration failed.", error);
                        promise.fail(error);
                    });
            } else {
                LOGGER.error("Database migration type `{}` is not supported.", type);
                promise.fail(new IllegalArgumentException(String.format("Database migration type `%s` is not supported.", type)));
            }

        } else {
            LOGGER.warn("Migration skipped. Configurations `database.migration.enabled` is not `true` or `database.migration.type` is not set.");
            promise.complete();
        }

        return promise.future();
    }

    private Optional<DatabaseMigration> resolve(String type) {
        return Optional.ofNullable(databaseMigrations.get(type));
    }

    /**
     * Default ConfigDatabaseMigrationInitializationHandler with known database migration instances
     * @return new instance of ConfigDatabaseMigrationInitializationHandler
     */
    public static InitializationHandler instance() {
        return instance(
            new ConfigFlywayDatabaseMigration() //Flyway database migration
        );
    }

    /**
     * ConfigDatabaseMigrationInitializationHandler with provided {@link DatabaseMigration} instances
     * @param databaseMigrations {@link DatabaseMigration} instances
     * @return new instance of ConfigDatabaseMigrationInitializationHandler
     */
    public static InitializationHandler instance(DatabaseMigration... databaseMigrations) {
        return new ConfigDatabaseMigrationInitializationHandler(databaseMigrations);
    }

}
