package io.github.balconyseats.vertx.helper.database.sql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    public static final String ORACLE_TYPE = "oracle";
    public static final String JDBC_TYPE = "jdbc";

    @FunctionalInterface
    interface PoolFunction {
        Pool apply(Vertx vertx, PoolOptions poolOptions, JsonObject dbConfig);
    }

    public static Map<String, PoolFunction> POOL_FUNCTIONS = Map.of(
      POSTGRESQL_TYPE, ConfigSqlPoolHelper::pgPool,
      ORACLE_TYPE, ConfigSqlPoolHelper::oraclePool,
      JDBC_TYPE, ConfigSqlPoolHelper::jdbcPool
    );

    public static Pool create(Vertx vertx, JsonObject config) {
        JsonObject dbConfig = config.getJsonObject("database");
        String type = dbConfig.getString("type");
        PoolOptions poolOptions = poolOptions(dbConfig);

        return Optional.ofNullable(POOL_FUNCTIONS.get(type))
            .map(f -> f.apply(vertx, poolOptions, dbConfig))
            .orElseThrow(() -> new IllegalArgumentException(String.format("Database type '%s' is not supported", type)));
    }

    private static PoolOptions poolOptions(JsonObject dbConfig) {
        return Optional.of(dbConfig)
            .map(o -> o.getJsonObject("pool"))
            .map(PoolOptions::new)
            .orElseGet(PoolOptions::new);
    }

    private static Pool jdbcPool(Vertx vertx, PoolOptions poolOptions, JsonObject dbConfig) {
        JDBCConnectOptions jdbcConnectOptions = new JDBCConnectOptions(dbConfig);
        return JDBCPool.pool(vertx, jdbcConnectOptions, poolOptions);
    }

    private static Pool pgPool(Vertx vertx, PoolOptions poolOptions, final JsonObject dbConfig) {
        PgConnectOptions connectOptions = new PgConnectOptions(dbConfig);
        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

    private static Pool oraclePool(Vertx vertx, PoolOptions poolOptions, final JsonObject dbConfig) {
        OracleConnectOptions connectOptions = new OracleConnectOptions(dbConfig);
        return OraclePool.pool(vertx, connectOptions, poolOptions);
    }

}
