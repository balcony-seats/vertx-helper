package io.github.balconyseats.vertx.helper.config;

public final class ConfigurationConstants {

    public static final String VERTX_APP_CLASSPATH_CONFIG = "application.properties:application.yml:application.json";
    public static final String VERTX_APP_SYSTEM_PROPERTY_NAME = "vertx.configuration";
    public static final String VERTX_APP_ENV_VARIABLE_NAME = "VERTX_CONFIGURATION";

    public static final char PATH_SEPARATOR = ':';


    private ConfigurationConstants() {
        //helper class
    }
}
