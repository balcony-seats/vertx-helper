package io.github.balconyseats.vertx.helper.database.sql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.util.Optional;

/**
 * Configures {@link Pool} from configuration object.
 *
 * <p>
 * For postgres ({@link PgConnectOptions}):
 * <pre>
 *     database:
 *       type: 'postgresql'
 *       host: 'host'
 *       port: port
 *       database: 'db'
 *       user: 'username'
 *       password: 'password'
 * </pre>
 *
 *
 * <p>
 *
 * For jdbc ({@link JDBCConnectOptions}:
 * <pre>
 *     database:
 *       type: 'jdbc'
 *       jdbc-url: 'jdbc:postgresql://localhost:5432/db'
 *       user: 'user'
 *       password: 'password'
 *
 * </pre>
 *
 * <p>
 *
 * Pool options ({@link PoolOptions})
 *
 * <pre>
 *     database:
 *       pool:
 *         max-size: 5
 *         max-wait-queue-size: -1
 *         connection-timeout: 30
 * </pre>
 *
 *
 */
public class ConfigSqlPoolHelper {

    public static final String POSTGRESQL_TYPE = "postgresql";
    public static final String JDBC_TYPE = "jdbc";

    public static Pool create(Vertx vertx, JsonObject config) {
        JsonObject dbConfig = config.getJsonObject("database");
        String type = dbConfig.getString("type");
        switch (type) {
            case POSTGRESQL_TYPE:
                return pgPool(vertx, dbConfig);
            case JDBC_TYPE:
                return jdbcPool(vertx, dbConfig);
            default:
                throw new IllegalArgumentException(String.format("Database type '%s' is not supported", type));
        }
    }

    private static Pool jdbcPool(Vertx vertx, JsonObject dbConfig) {
        JDBCConnectOptions jdbcConnectOptions = new JDBCConnectOptions(dbConfig)
            .setMetricsEnabled(true);

        return JDBCPool.pool(vertx, jdbcConnectOptions, poolOptions(dbConfig));
    }

    private static Pool pgPool(Vertx vertx, final JsonObject dbConfig) {
        PoolOptions poolOptions = poolOptions(dbConfig);
        PgConnectOptions connectOptions = new PgConnectOptions(dbConfig);
        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

    private static PoolOptions poolOptions(JsonObject dbConfig) {
        return Optional.of(dbConfig)
            .map(o -> o.getJsonObject("pool"))
            .map(PoolOptions::new)
            .orElseGet(PoolOptions::new);
    }

}
