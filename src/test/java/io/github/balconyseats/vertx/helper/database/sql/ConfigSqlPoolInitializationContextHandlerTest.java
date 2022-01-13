package io.github.balconyseats.vertx.helper.database.sql;

import io.github.balconyseats.vertx.helper.application.InitializationContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


@ExtendWith(VertxExtension.class)
class ConfigSqlPoolInitializationContextHandlerTest {

    @Test
    public void testHandle_expectSqlPoolInContextWithDefaultKey(Vertx vertx, VertxTestContext testContext) {

        Pool pool = Mockito.mock(Pool.class);

        JsonObject config = new JsonObject();
        MockedStatic<ConfigSqlPoolHelper> poolHelperMockedStatic = Mockito.mockStatic(ConfigSqlPoolHelper.class);
        poolHelperMockedStatic.when(() -> ConfigSqlPoolHelper.create(vertx, config))
                .thenReturn(pool);


        InitializationContext initializationContext = new InitializationContext();
        ConfigSqlPoolInitializationContextHandler.instance().handle(vertx, initializationContext, config)
            .onComplete(testContext.succeeding(__ -> testContext.verify(() -> {
                Assertions.assertThat(initializationContext.<Pool>get("sqlpool")).isEqualTo(pool);
                poolHelperMockedStatic.verify(() -> ConfigSqlPoolHelper.create(vertx, config));
                poolHelperMockedStatic.close();
                testContext.completeNow();
            })));
    }

    @Test
    public void testHandle_expectSqlPoolInContextWithCustomKey(Vertx vertx, VertxTestContext testContext) {

        Pool pool = Mockito.mock(Pool.class);

        JsonObject config = new JsonObject();
        MockedStatic<ConfigSqlPoolHelper> poolHelperMockedStatic = Mockito.mockStatic(ConfigSqlPoolHelper.class);
        poolHelperMockedStatic.when(() -> ConfigSqlPoolHelper.create(vertx, config))
            .thenReturn(pool);


        InitializationContext initializationContext = new InitializationContext();
        ConfigSqlPoolInitializationContextHandler.instance("dbpool").handle(vertx, initializationContext, config)
            .onComplete(testContext.succeeding(__ -> testContext.verify(() -> {
                Assertions.assertThat(initializationContext.<Pool>get("dbpool")).isEqualTo(pool);
                poolHelperMockedStatic.verify(() -> ConfigSqlPoolHelper.create(vertx, config));
                poolHelperMockedStatic.close();
                testContext.completeNow();
            })));
    }

}