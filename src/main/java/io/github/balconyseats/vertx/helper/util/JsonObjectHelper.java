package io.github.balconyseats.vertx.helper.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

public class JsonObjectHelper {

    public static Boolean getBoolean(String path, JsonObject jsonObject) {
        return (Boolean) getObject(path, jsonObject, Boolean.FALSE);
    }

    public static String getString(String path, JsonObject jsonObject) {
        return getString(path, jsonObject, null);
    }

    public static String getString(String path, JsonObject jsonObject, String defaultValue) {
        return (String) getObject(path, jsonObject, defaultValue);
    }

    public static Integer getInteger(String path, JsonObject config) {
        return getInteger(path, config, null);
    }

    public static Integer getInteger(String path, JsonObject jsonObject, Integer defaultValue) {
        Number number = (Number) getObject(path, jsonObject, defaultValue);
        if (number == null) {
            return null;
        } else if (number instanceof Integer) {
            return (Integer) number;
        } else {
            return number.intValue();
        }
    }

    public static JsonObject getJsonObject(String path, JsonObject jsonObject) {
        return (JsonObject) getObject(path, jsonObject, null);
    }

    public static JsonArray getJsonArray(String path, JsonObject jsonObject) {
        return (JsonArray) getObject(path, jsonObject, null);
    }

    private static Object getObject(String path, JsonObject jsonObject, Object defaultValue) {
        return JsonPointer.from(path).queryJsonOrDefault(jsonObject, defaultValue);
    }

}

