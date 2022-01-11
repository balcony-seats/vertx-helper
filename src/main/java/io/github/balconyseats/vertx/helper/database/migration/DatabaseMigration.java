package io.github.balconyseats.vertx.helper.database.migration;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface DatabaseMigration {
    Future<Void> migrate(JsonObject config);
    String type();
}
