package io.github.balconyseats.vertx.helper.database.migration;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.github.balconyseats.vertx.helper.application.configurer.InitializationHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.Map;

@ExtendWith(VertxExtension.class)
class ConfigDatabaseMigrationInitializationHandlerTest {

    @Test
    public void testHandle_shouldMigrateWhenMigrationEnabled(Vertx vertx, VertxTestContext vertxTestContext) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("migration", new JsonObject().put("enabled", true).put("type", "foo"))
            );

        DatabaseMigration databaseMigration = Mockito.mock(DatabaseMigration.class);
        DatabaseMigration disabledDatabaseMigration = Mockito.mock(DatabaseMigration.class);

        Mockito.when(databaseMigration.migrate(config.getJsonObject("database"))).thenReturn(Future.succeededFuture());
        Mockito.when(databaseMigration.type()).thenReturn("foo");
        Mockito.when(disabledDatabaseMigration.type()).thenReturn("bar");

        InitializationHandler handler = ConfigDatabaseMigrationInitializationHandler.instance(databaseMigration, disabledDatabaseMigration);


        handler.handle(vertx, new InitializationContext(), config)
            .onComplete(vertxTestContext.succeeding(__ -> vertxTestContext.verify(() -> {
                Mockito.verify(databaseMigration).migrate(config.getJsonObject("database"));
                Mockito.verify(databaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(databaseMigration);
                Mockito.verify(disabledDatabaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(disabledDatabaseMigration);
                vertxTestContext.completeNow();
            })));

    }

    @Test
    public void testHandle_shouldNotMigrateWhenMigrationIsDisabled(Vertx vertx, VertxTestContext vertxTestContext) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("migration", new JsonObject().put("enabled", false).put("type", "foo"))
            );

        DatabaseMigration databaseMigration = Mockito.mock(DatabaseMigration.class);
        DatabaseMigration disabledDatabaseMigration = Mockito.mock(DatabaseMigration.class);

        Mockito.when(databaseMigration.migrate(config.getJsonObject("database"))).thenReturn(Future.succeededFuture());
        Mockito.when(databaseMigration.type()).thenReturn("foo");
        Mockito.when(disabledDatabaseMigration.type()).thenReturn("bar");

        InitializationHandler handler = ConfigDatabaseMigrationInitializationHandler.instance(databaseMigration, disabledDatabaseMigration);


        handler.handle(vertx, new InitializationContext(), config)
            .onComplete(vertxTestContext.succeeding(__ -> vertxTestContext.verify(() -> {
                Mockito.verify(databaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(databaseMigration);
                Mockito.verify(disabledDatabaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(disabledDatabaseMigration);
                vertxTestContext.completeNow();
            })));

    }

    @Test
    public void testHandle_shouldFailWhenMigrationTypeIsUnsupported(Vertx vertx, VertxTestContext vertxTestContext) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("migration", new JsonObject().put("enabled", true).put("type", "unsupported"))
            );

        DatabaseMigration databaseMigration = Mockito.mock(DatabaseMigration.class);
        DatabaseMigration disabledDatabaseMigration = Mockito.mock(DatabaseMigration.class);

        Mockito.when(databaseMigration.migrate(config.getJsonObject("database"))).thenReturn(Future.succeededFuture());
        Mockito.when(databaseMigration.type()).thenReturn("foo");
        Mockito.when(disabledDatabaseMigration.type()).thenReturn("bar");

        InitializationHandler handler = ConfigDatabaseMigrationInitializationHandler.instance(databaseMigration, disabledDatabaseMigration);


        handler.handle(vertx, new InitializationContext(), config)
            .onComplete(vertxTestContext.failing(err -> vertxTestContext.verify(() -> {
                Mockito.verify(databaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(databaseMigration);
                Mockito.verify(disabledDatabaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(disabledDatabaseMigration);
                Assertions.assertThat(err).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Database migration type `unsupported` is not supported.");
                vertxTestContext.completeNow();
            })));

    }

    @Test
    public void testHandle_shouldFailWhenMigrationIsEnabledAndFailed(Vertx vertx, VertxTestContext vertxTestContext) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("migration", new JsonObject().put("enabled", true).put("type", "foo"))
            );

        DatabaseMigration databaseMigration = Mockito.mock(DatabaseMigration.class);
        DatabaseMigration disabledDatabaseMigration = Mockito.mock(DatabaseMigration.class);

        Mockito.when(databaseMigration.migrate(config.getJsonObject("database"))).thenReturn(Future.failedFuture(new IllegalArgumentException("Migration failed")));
        Mockito.when(databaseMigration.type()).thenReturn("foo");
        Mockito.when(disabledDatabaseMigration.type()).thenReturn("bar");

        InitializationHandler handler = ConfigDatabaseMigrationInitializationHandler.instance(databaseMigration, disabledDatabaseMigration);

        handler.handle(vertx, new InitializationContext(), config)
            .onComplete(vertxTestContext.failing(error -> vertxTestContext.verify(() -> {
                Mockito.verify(databaseMigration).migrate(config.getJsonObject("database"));
                Mockito.verify(databaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(databaseMigration);
                Mockito.verify(disabledDatabaseMigration, Mockito.times(1)).type();
                Mockito.verifyNoMoreInteractions(disabledDatabaseMigration);
                Assertions.assertThat(error).isInstanceOf(IllegalArgumentException.class).hasMessage("Migration failed");
                vertxTestContext.completeNow();
            })));

    }

    @Test
    public void shouldCreateDefault() {
        InitializationHandler instance = ConfigDatabaseMigrationInitializationHandler.instance();

        Assertions.assertThat(instance).isNotNull()
            .extracting("databaseMigrations").isNotNull()
            .isNotNull()
            .satisfies(db -> {
                Map<String, DatabaseMigration> databaseMigrations = (Map<String, DatabaseMigration>) db;
                Assertions.assertThat(databaseMigrations).hasEntrySatisfying("flyway", fw -> {
                   Assertions.assertThat(fw).isNotNull()
                       .isInstanceOf(ConfigFlywayDatabaseMigration.class);
                });
            });
    }


}