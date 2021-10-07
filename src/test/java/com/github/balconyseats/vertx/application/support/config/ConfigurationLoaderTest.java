package com.github.balconyseats.vertx.application.support.config;

import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class ConfigurationLoaderTest {


    @Test
    public void shouldLoadClassPathConfigurations() throws Throwable {
        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).isEqualTo("{\"a\":10,\"b\":{\"b\":2},\"y\":{\"a\":20},\"j\":{\"a\":1,\"b\":\"b\"}}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    public void shouldGetEmptyConfiguration_whenClasspathConfigurationIsDisabledAndOtherConfigurationsNotExists() throws Throwable {
        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).isEqualTo("{}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    public void shouldLoadDefaultVertxConfiguration() throws Throwable {

        createFile("conf/config.json", "{\n" +
                "    \"vertx\": {\n" +
                "        \"a\": 1,\n" +
                "        \"b\": \"b\"\n" +
                "    }\n" +
                "}");

        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .enableFeature(ConfigurationLoaderBuilder.FeatureType.DEFAULT_VERTX_STORES)
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).contains("\"vertx\":{\"a\":1,\"b\":\"b\"}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        deleteFile("conf");
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    public void shouldLoadConfigurationFromSystemPropertyValue() throws Throwable {

        createFile("conf/sysprops.json", "{\n" +
                "    \"sys\": {\n" +
                "        \"a\": 1,\n" +
                "        \"b\": \"b\"\n" +
                "    }\n" +
                "}");

        createFile("conf/sysprops.yml", "sys:\n" +
                "  a: 2\n" +
                "  c: 10\n" +
                "  d:\n" +
                "    a: sysprops");

        createFile("conf/sysprops2.yaml", "sys:\n" +
                "  z: 2\n");

        Path pwd = Paths.get("").toAbsolutePath();

        String sysProps = pwd + "/conf/sysprops.yml";
        String sysProps2 = pwd + "/conf/sysprops2.yaml";

        System.setProperty(ConfigurationConstants.VERTX_APP_SYSTEM_PROPERTY_NAME,
                String.format("conf/sysprops.json:'file://%s':\"file://%s\"", sysProps, sysProps2)
        );

        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).contains("{\"sys\":{\"a\":2,\"b\":\"b\",\"c\":10,\"d\":{\"a\":\"sysprops\"},\"z\":2}}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        System.clearProperty(ConfigurationConstants.VERTX_APP_SYSTEM_PROPERTY_NAME);
        deleteFile("conf");

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

    @Test
    public void shouldLoadConfigurationFromCustomConfigStoreOptions() throws Throwable {
        createFile("conf/sysprops.json", "{\n" +
                "    \"sys\": {\n" +
                "        \"a\": 1,\n" +
                "        \"b\": \"b\"\n" +
                "    }\n" +
                "}");

        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .addStore(
                        new ConfigStoreOptions()
                                .setType("file")
                                .setFormat("json")
                                .setConfig(new JsonObject().put("path", "conf/sysprops.json"))
                )
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).contains("{\"sys\":{\"a\":1,\"b\":\"b\"}}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        deleteFile("conf");

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }

    }

    @Test
    public void shouldLoadConfigurationFromCustomConfigPath() throws Throwable {
        createFile("conf/sysprops.json", "{\n" +
                "    \"sys\": {\n" +
                "        \"a\": 1,\n" +
                "        \"b\": \"b\"\n" +
                "    }\n" +
                "}");

        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .addConfigPath("conf/sysprops.json")
                .build()
                .load()
                .onComplete(testContext.succeeding(c -> testContext.verify(() -> {
                            Assertions.assertThat(c.encode()).contains("{\"sys\":{\"a\":1,\"b\":\"b\"}}");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        deleteFile("conf");

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }

    }

    @Test
    public void shouldFail() throws Throwable {

        var testContext = new VertxTestContext();
        ConfigurationLoader.builder()
                .disableFeature(ConfigurationLoaderBuilder.FeatureType.CLASSPATH_CONFIG)
                .addStore(
                        new ConfigStoreOptions()
                                .setType("file")
                                .setFormat("json")
                                .setConfig(new JsonObject().put("path", "conf/sysprops.json"))
                )
                .build()
                .load()
                .onComplete(testContext.failing(t -> testContext.verify(() -> {
                            Assertions.assertThat(t).isInstanceOf(FileSystemException.class)
                                    .hasMessage("java.nio.file.NoSuchFileException: conf/sysprops.json");
                            testContext.completeNow();
                        })
                ));

        Assertions.assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();

        deleteFile("conf");

        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }

    }

    private void createFile(String path, String content) throws Throwable {
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            deleteFile(filePath);
        }

        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        Files.createFile(filePath);
        Files.writeString(filePath, content);
    }

    private void deleteFile(String path) throws Throwable {
        deleteFile(Path.of(path));
    }

    private void deleteFile(Path path) throws Throwable {
        if (Files.isDirectory(path)) {
            FileUtils.deleteDirectory(path.toFile());
        } else {
            Files.deleteIfExists(path);
        }


    }

}