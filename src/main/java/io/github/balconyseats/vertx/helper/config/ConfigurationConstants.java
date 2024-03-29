package io.github.balconyseats.vertx.helper.config;

import java.util.Set;

public final class ConfigurationConstants {

    public static final Set<String> VERTX_APP_CLASSPATH_CONFIG = Set.of("application.properties", "application.yml", "application.json");
    public static final String VERTX_APP_SYSTEM_PROPERTY_NAME = "vertx.configuration";
    public static final String VERTX_APP_ENV_VARIABLE_NAME = "VERTX_CONFIGURATION";
    public static final String VERTX_APP_CONFIGURATION_CLASSPATH_DISABLED_SYSTEM_PROPERTY = "vertx.configuration.classpath.disabled";
    public static final String VERTX_APP_CONFIGURATION_CLASSPATH_DISABLED_ENV_VARIABLE = "VERTX_CONFIGURATION_CLASSPATH_DISABLED";

    public static final char PATH_SEPARATOR = ':';


    private ConfigurationConstants() {
        //helper class
    }
}
