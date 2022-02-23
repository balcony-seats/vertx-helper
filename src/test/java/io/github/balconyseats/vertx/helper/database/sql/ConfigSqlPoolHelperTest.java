package io.github.balconyseats.vertx.helper.database.sql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.oracleclient.OraclePool;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class ConfigSqlPoolHelperTest {

    @Test
    public void shouldCreatePostgresPool(Vertx vertx) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("type", "postgresql")
                .put("host", "localhost")
                .put("port", 54320)
                .put("database", "database")
                .put("user", "user")
                .put("password", "password")
                .put("pool", new JsonObject()
                    .put("maxSize", 5)
                )
            );

        Pool pool = ConfigSqlPoolHelper.create(vertx, config);

        Assertions.assertThat(pool).isNotNull()
            .isInstanceOf(PgPool.class);

    }

    @Test
    public void shouldCreateOraclePool(Vertx vertx) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("type", "oracle")
                .put("host", "localhost")
                .put("port", 1521)
                .put("database", "database")
                .put("user", "user")
                .put("password", "password")
                .put("pool", new JsonObject()
                    .put("maxSize", 5)
                )
            );

        Pool pool = ConfigSqlPoolHelper.create(vertx, config);

        Assertions.assertThat(pool).isNotNull()
            .isInstanceOf(OraclePool.class);

    }

    @Test
    public void shouldCreateJdbcPool(Vertx vertx) {
        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("type", "jdbc")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/omte")
                .put("user", "user")
                .put("password", "password")
                .put("pool", new JsonObject()
                    .put("maxSize", 5)
                )
            );

        Pool pool = ConfigSqlPoolHelper.create(vertx, config);

        Assertions.assertThat(pool).isNotNull()
            .isInstanceOf(JDBCPool.class);


    }

    @Test
    public void shouldThrowIllegalArgumentException_whenUnsupportedTypeIsProvided(Vertx vertx) {

        JsonObject config = new JsonObject()
            .put("database", new JsonObject()
                .put("type", "foo")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/omte")
                .put("user", "user")
                .put("password", "password")
                .put("pool", new JsonObject()
                    .put("maxSize", 5)
                )
            );

        Assertions.assertThatThrownBy(() -> ConfigSqlPoolHelper.create(vertx, config))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Database type 'foo' is not supported");

    }
}