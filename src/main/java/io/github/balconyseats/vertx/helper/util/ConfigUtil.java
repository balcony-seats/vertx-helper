package io.github.balconyseats.vertx.helper.util;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

public class ConfigUtil {

    public static Boolean getBoolean(String path, JsonObject config) {
        return (Boolean) getObject(path, config, Boolean.FALSE);
    }

    public static String getString(String path, JsonObject config) {
        return getString(path, config, null);
    }

    public static String getString(String path, JsonObject config, String defaultValue) {
        return (String) getObject(path, config, defaultValue);
    }

    public static Integer getInteger(String path, JsonObject config) {
        return getInteger(path, config, null);
    }

    public static Integer getInteger(String path, JsonObject config, Integer defaultValue) {
        Number number = (Number) getObject(path, config, defaultValue);
        if (number == null) {
            return null;
        } else if (number instanceof Integer) {
            return (Integer) number;
        } else {
            return number.intValue();
        }
    }

    private static Object getObject(String path, JsonObject config, Object defaultValue) {
        return JsonPointer.from(path).queryJsonOrDefault(config, defaultValue);
    }

}

