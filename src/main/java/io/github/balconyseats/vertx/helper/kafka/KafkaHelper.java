package io.github.balconyseats.vertx.helper.kafka;

import io.github.balconyseats.vertx.helper.exception.IllegalConfigurationException;
import io.github.balconyseats.vertx.helper.util.ConfigUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Helper to create consumer and producer from configuration
 * <pre>
 * kafka:
 *   enabled: true
 *   consumers:
 *     config:
 *       key.deserializer : 'org.apache.kafka.common.serialization.StringDeserializer'
 *       value.deserializer : 'org.apache.kafka.common.serialization.StringDeserializer'
 *     foo:
 *       client.id: foo
 *       topic: foo_topic
 *       group.id: foo
 *     bar:
 *       client.id: bar
 *       topic: bar_topic
 *       group.id: bar
 *   producers:
 *     config:
 *       key.serializer : 'org.apache.kafka.common.serialization.StringSerializer'
 *       value.serializer : 'org.apache.kafka.common.serialization.StringSerializer'
 *     foo:
 *       client.id: foo
 *       topic: foo
 *     bar:
 *       client.id: bar
 *       topic: bar
 *   config:
 *     bootstrap.servers: 'server-1:9092,server-2:9092'
 *     security.protocol : 'SSL'
 *     ssl.enabled.protocols: 'TLSv1.2,TLSv1.1,TLSv1'
 *     ssl.keystore.location: 'keystore.jks'
 *     ssl.keystore.password: 'changeit'
 *     ssl.keystore.type: 'JKS'
 *     ssl.truststore.location: 'truststore.jks'
 *     ssl.truststore.password: 'password'
 *     ssl.truststore.type: 'JKS'
 * </pre>
 */
public class KafkaHelper {

    /**
     * Is kafka enabled in configuration 'kafka.enabled' property
     * @param config configuration
     * @return true if is enabled or false if not
     */
    public static boolean isKafkaEnabled(JsonObject config) {
        return ConfigUtil.getBoolean("/kafka/enabled", config);
    }

    /**
     * Creates {@link KafkaConsumer} from config object
     * @param vertx vertx instance
     * @param consumerName consumer name in config
     * @param config configuration
     * @param <K> key type
     * @param <V> value type
     * @return {@link KafkaConsumer} instance
     */
    public static <K, V> KafkaConsumer<K, V> createConsumer(Vertx vertx, String consumerName, JsonObject config) {
        return createConsumer(vertx, consumerName, config, null);
    }

    /**
     * Creates {@link KafkaConsumer} from config object with custom kafka configuration map
     * @param vertx vertx instance
     * @param consumerName consumer name in config
     * @param config configuration
     * @param customKafkaConfig custom kafka config map
     * @param <K> key type
     * @param <V> value type
     * @return {@link KafkaConsumer} instance
     */
    public static <K, V> KafkaConsumer<K, V> createConsumer(Vertx vertx, String consumerName, JsonObject config, Map<String, String> customKafkaConfig) {

        JsonObject kafkaConfig = kafkaConfig(config);

        Map<String, String> commonKafkaConfig = commonKafkaConfig(kafkaConfig);
        Map<String, String> consumerConfig = consumerConfig(consumerName, kafkaConfig);

        Map<String, String> finalConfig = new HashMap<>(commonKafkaConfig);
        finalConfig.putAll(consumerConfig);
        finalConfig.putAll(Optional.ofNullable(customKafkaConfig).orElse(Map.of()));

        return KafkaConsumer.create(vertx, finalConfig);
    }

    /**
     * Creates {@link KafkaProducer} from config object
     * @param vertx vertx instance
     * @param producerName producer name in config
     * @param config configuration
     * @param <K> key type
     * @param <V> value type
     * @return {@link KafkaProducer} instance
     */
    public static <K, V> KafkaProducer<K, V> createProducer(Vertx vertx, String producerName, JsonObject config) {
        return createProducer(vertx, producerName, config, null);
    }

    /**
     * Creates {@link KafkaProducer} from config object with custom kafka configuration map
     * @param vertx vertx instance
     * @param producerName producer name in config
     * @param config configuration
     * @param customKafkaConfig custom kafka config map
     * @param <K> key type
     * @param <V> value type
     * @return {@link KafkaProducer} instance
     */
    public static <K, V> KafkaProducer<K, V> createProducer(Vertx vertx, String producerName, JsonObject config, Map<String, String> customKafkaConfig) {

        JsonObject kafkaConfig = kafkaConfig(config);

        Map<String, String> commonKafkaConfig = commonKafkaConfig(kafkaConfig);
        Map<String, String> producerConfig = producerConfig(producerName, kafkaConfig);

        Map<String, String> finalConfig = new HashMap<>(commonKafkaConfig);
        finalConfig.putAll(producerConfig);
        finalConfig.putAll(Optional.ofNullable(customKafkaConfig).orElse(Map.of()));

        return KafkaProducer.create(vertx, finalConfig);
    }

    /**
     * Retrieves consumer topic from configuration
     * @param consumerName consumer name
     * @param config configuration
     * @return topic name
     */
    public static String consumerTopic(String consumerName, JsonObject config) {
        String topicPath = String.format("/kafka/consumers/%s/topic", consumerName);
        return Optional.ofNullable(ConfigUtil.getString(topicPath, config))
            .orElseThrow(() -> new IllegalConfigurationException(String.format("Kafka configuration for topic '%s' does not exist.", topicPath)));
    }

    /**
     * Retrieves producer topic from configuration
     * @param producerName producer name
     * @param config configuration
     * @return topic name
     */
    public static String producerTopic(String producerName, JsonObject config) {
        String topicPath = String.format("/kafka/producers/%s/topic", producerName);
        return Optional.ofNullable(ConfigUtil.getString(topicPath, config))
            .orElseThrow(() -> new IllegalConfigurationException(String.format("Kafka configuration for topic '%s' does not exist.", topicPath)));
    }

    private static Map<String, String> consumerConfig(String consumerName, JsonObject kafkaConfig) {
        return extractConfig("consumers", consumerName, kafkaConfig);
    }

    private static Map<String, String> producerConfig(String consumerName, JsonObject kafkaConfig) {
        return extractConfig("producers", consumerName, kafkaConfig);
    }

    private static Map<String, String> extractConfig(String path, String name, JsonObject kafkaConfig) {
        JsonObject pathConfig = Optional.ofNullable(kafkaConfig)
            .map(kc -> kc.getJsonObject(path))
            .orElseThrow(() -> new IllegalConfigurationException(String.format("Kafka configuration does not exist 'kafka.%s'.", path)));

        JsonObject config = Optional.ofNullable(pathConfig.getJsonObject("config")).orElse(new JsonObject());

        JsonObject nameConfig = Optional.ofNullable(pathConfig.getJsonObject(name))
            .orElseThrow(() -> new IllegalConfigurationException(String.format("Kafka configuration does not exist 'kafka.%s.%s'.", path, name)));

        return new JsonObject()
            .mergeIn(config)
            .mergeIn(nameConfig)
            .stream()
            .filter(e -> e.getValue() instanceof String && !"topic".equals(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
    }

    private static Map<String, String> commonKafkaConfig(JsonObject kafkaConfig) {
        JsonObject config = Optional.ofNullable(kafkaConfig)
            .map(k -> k.getJsonObject("config"))
            .orElseThrow(() -> new IllegalConfigurationException("Kafka configuration does not exist 'kafka.config'"));

        return config.stream()
            .filter(e -> e.getValue() instanceof String)
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
    }

    private static JsonObject kafkaConfig(JsonObject config) {
        return Optional.ofNullable(config.getJsonObject("kafka"))
            .orElseThrow(() -> new IllegalConfigurationException("Kafka configuration does not exist 'kafka'"));
    }

}
