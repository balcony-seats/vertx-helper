package io.github.balconyseats.vertx.helper.database.sql;

import io.github.balconyseats.vertx.helper.exception.IllegalConfigurationException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.util.Map;
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
 * <p>
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
 * <p>
 * Pool options ({@link PoolOptions})
 *
 * <pre>
 *     database:
 *       pool:
 *         max-size: 5
 *         max-wait-queue-size: -1
 *         connection-timeout: 30
 * </pre>
 */
public class ConfigSqlPoolHelper {

    public static final String POSTGRESQL_TYPE = "postgresql";
    public static final String ORACLE_TYPE = "oracle";
    public static final String MSSQL_TYPE = "mssql";
    public static final String JDBC_TYPE = "jdbc";

    public static final String DEFAULT_DATABASE_CONFIG_ROOT = "database";
    public static final String DATABASE_TYPE_KEY = "type";
    public static final String DATABASE_POOL_KEY = "pool";

    public static Map<String, PoolFunction> POOL_FUNCTIONS = Map.of(
        POSTGRESQL_TYPE, ConfigSqlPoolHelper::pgPool,
        ORACLE_TYPE, ConfigSqlPoolHelper::oraclePool,
        MSSQL_TYPE, ConfigSqlPoolHelper::mssqlPool,
        JDBC_TYPE, ConfigSqlPoolHelper::jdbcPool
    );

    public static Pool create(Vertx vertx, JsonObject config) {
        return ConfigSqlPoolHelper.create(vertx, config, DEFAULT_DATABASE_CONFIG_ROOT);
    }

    public static Pool create(Vertx vertx, JsonObject config, String root) {
        JsonObject dbConfig = config.getJsonObject(root);

        if (dbConfig == null) {
            throw new IllegalConfigurationException(String.format("Database configuration for root '%s' not exists.", root));
        }

        String type = dbConfig.getString(DATABASE_TYPE_KEY);
        PoolOptions poolOptions = poolOptions(dbConfig);

        return Optional.ofNullable(POOL_FUNCTIONS.get(type))
            .map(f -> f.apply(vertx, poolOptions, dbConfig))
            .orElseThrow(() -> new IllegalArgumentException(String.format("Database type '%s' is not supported.", type)));
    }

    private static PoolOptions poolOptions(JsonObject dbConfig) {
        return Optional.of(dbConfig)
            .map(o -> o.getJsonObject(DATABASE_POOL_KEY))
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

    private static Pool mssqlPool(Vertx vertx, PoolOptions poolOptions, final JsonObject dbConfig) {
        MSSQLConnectOptions connectOptions = new MSSQLConnectOptions(dbConfig);
        return MSSQLPool.pool(vertx, connectOptions, poolOptions);
    }

    @FunctionalInterface
    interface PoolFunction {
        Pool apply(Vertx vertx, PoolOptions poolOptions, JsonObject dbConfig);
    }

}
