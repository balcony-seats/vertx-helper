package com.github.balconyseats.vertx.application.support.config;

import com.github.balconyseats.vertx.application.support.util.StringHelper;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration loader builder.
 */
public class ConfigurationLoaderBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoaderBuilder.class);

    private static final Map<FeatureType, Boolean> DEFAULT_FEATURES = Map.of(
            FeatureType.DEFAULT_VERTX_STORES, Boolean.FALSE,
            FeatureType.CLASSPATH_CONFIG, Boolean.TRUE
    );
    public static final String FILE_PREFIX = "file://";

    private final Map<FeatureType, Boolean> features = new HashMap<>(DEFAULT_FEATURES);

    private final List<ConfigStoreOptions> stores = new LinkedList<>();

    public ConfigurationLoaderBuilder disableFeature(FeatureType featureType) {
        features.replace(featureType, Boolean.FALSE);
        return this;
    }

    public ConfigurationLoaderBuilder enableFeature(FeatureType featureType) {
        features.replace(featureType, Boolean.TRUE);
        return this;
    }

    public ConfigurationLoaderBuilder addStore(ConfigStoreOptions store) {
        stores.add(store);
        return this;
    }

    public ConfigurationLoaderBuilder addConfigPath(String path) {
        stores.add(createFileStoreOptions(path));
        return this;
    }

    public ConfigurationLoader build() {
        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions();

        // 1. class path config if enabled
        if (features.get(FeatureType.CLASSPATH_CONFIG)) {
            addFileConfigStores(retrieverOptions, ConfigurationConstants.VERTX_APP_CLASSPATH_CONFIG);
        }
        // 2. default vertx stores if enabled
        if (features.get(FeatureType.DEFAULT_VERTX_STORES)) {
            retrieverOptions.setIncludeDefaultStores(true);
        }
        // 2. config files from system property
        addFileConfigStores(retrieverOptions, System.getProperty(ConfigurationConstants.VERTX_APP_SYSTEM_PROPERTY_NAME));
        // 3. config files from environment variable
        addFileConfigStores(retrieverOptions, System.getenv(ConfigurationConstants.VERTX_APP_ENV_VARIABLE_NAME));
        // 4. add additional stores
        stores.forEach(retrieverOptions::addStore);

        return new ConfigurationLoader(retrieverOptions);
    }

    private void addFileConfigStores(final ConfigRetrieverOptions retrieverOptions, final String locations) {
        if (StringUtils.isNotBlank(locations)) {
            List<String> configs = StringHelper.splitEnclosed(locations, ConfigurationConstants.PATH_SEPARATOR, "\"", "'");
            for (String config : configs) {
                LOGGER.info("Adding config store: {}", config);
                ConfigStoreOptions configStoreOptions = createFileStoreOptions(config);
                retrieverOptions.addStore(configStoreOptions);
            }
        }
    }

    private String normalizeFilePath(String config) {
        if (config.startsWith(FILE_PREFIX)) {
            return config.replace(FILE_PREFIX, "");
        }
        return config;
    }

    private ConfigStoreOptions createFileStoreOptions(String config) {
        return new ConfigStoreOptions()
                .setOptional(true)
                .setType("file")
                .setFormat(format(config))
                .setConfig(new JsonObject().put("path", normalizeFilePath(config)));
    }

    private String format(String config) {
        if (StringUtils.endsWithIgnoreCase(config, ".yml") || StringUtils.endsWithIgnoreCase(config, ".yaml")) {
            return "yaml";
        }
        if (StringUtils.endsWithIgnoreCase(config, ".properties")) {
            return "properties";
        }
        return "json";
    }

    public enum FeatureType {
        DEFAULT_VERTX_STORES,
        CLASSPATH_CONFIG
    }

}
