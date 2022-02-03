package io.github.balconyseats.vertx.helper.auth.jwt;

import io.github.balconyseats.vertx.helper.http.HttpStatusCode;
import io.github.balconyseats.vertx.helper.test.util.JWTUtil;
import io.github.balconyseats.vertx.helper.test.util.RandomPort;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(VertxExtension.class)
class ConfigJWTAuthProviderTest {

    private String JWKS_URI_KEYS = "{\n" +
        "    \"keys\" : [\n" +
        "        {\n" +
        "            \"kty\": \"RSA\",\n" +
        "            \"n\": \"xyibm0s184kuSgzCX9NEuNxvPZQpxb2jk8hU34EaCoh-WfnQLmPnwJZwisLkVcrg3nu4ikEZQnHd9Mqrj2ofUO8_9uMhSeBOQUZTIi3_jnZHsNMTsmIrJ56WqSZhpf8Nv4YZByw53OaEcVxslwYH58pp9VR7dB3DRQNEKp0_hVpcEhLQN402WT-kEp8JPNX_gBv9L0LlSHc7FxX1tlQWzvRfWGgfkeNBTURA_k9JJheqVSXXGqDjHBhei9wMkR_4w-udYVeG9k6IlUw6WrXvCAoQbrA8xC3nm2ln0vSNgmhZHaqe2HnsiZbthzZK8FwajC6D4ylsZ8r3x4JVFYkhVQ\",\n" +
        "            \"e\": \"AQAB\",\n" +
        "            \"alg\": \"RS512\",\n" +
        "            \"kid\": \"foo-jwk-uri-key-id\",\n" +
        "            \"use\": \"sig\"\n" +
        "          }\n" +
        "    ]\n" +
        "}";

    @Test
    public void test_authenticate_whenJwksConfigurationIsProvided_expectSuccess(Vertx vertx, VertxTestContext testContext) {

        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(-1));

        String jwt = generateJWT(vertx, "foo", "foo-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.succeeding(u -> testContext.verify(() -> {
                Assertions.assertThat(u).isNotNull();
                Assertions.assertThat(u.principal().getString("access_token")).isEqualTo(jwt);
                Assertions.assertThat(u.attributes().getString("iss")).isEqualTo("foo");
                Assertions.assertThat(u.attributes().getString("sub")).isEqualTo("foo");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenJwksUriConfigurationIsProvided_expectSuccess(Vertx vertx, VertxTestContext testContext) {

        createHttpServer(vertx, JWKS_URI_KEYS, HttpStatusCode.OK)
            .onSuccess(httpServer -> {

                ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(httpServer.actualPort()));

                String jwt = generateJWT(vertx, "bar", "foo-jwk-uri-key-id", 60);

                provider.authenticate(new TokenCredentials(jwt),
                    testContext.succeeding(u -> testContext.verify(() -> {
                        Assertions.assertThat(u).isNotNull();
                        Assertions.assertThat(u.principal().getString("access_token")).isEqualTo(jwt);
                        Assertions.assertThat(u.attributes().getString("iss")).isEqualTo("bar");
                        Assertions.assertThat(u.attributes().getString("sub")).isEqualTo("bar");
                        testContext.completeNow();
                    }))
                );
            })
            .onFailure(testContext::failNow);

    }

    @Test
    public void test_authenticate_whenJwksAndJwksUriConfigurationIsProvided_expectSuccess(Vertx vertx, VertxTestContext testContext) {

        createHttpServer(vertx, JWKS_URI_KEYS, HttpStatusCode.OK)
            .onSuccess(httpServer -> {
                ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(httpServer.actualPort()));

                String jwt = generateJWT(vertx, "bar", "foo-jwk-uri-key-id", 60);

                provider.authenticate(new TokenCredentials(jwt),
                    testContext.succeeding(u -> testContext.verify(() -> {
                        Assertions.assertThat(u).isNotNull();
                        Assertions.assertThat(u.principal().getString("access_token")).isEqualTo(jwt);
                        Assertions.assertThat(u.attributes().getString("iss")).isEqualTo("bar");
                        Assertions.assertThat(u.attributes().getString("sub")).isEqualTo("bar");
                        testContext.completeNow();
                    }))
                );
            })
            .onFailure(testContext::failNow);

    }

    @Test
    public void test_authenticate_whenJwksUriConfigurationIsProvided_expectFailure_onUnsuccessJwksRetrieve(Vertx vertx, VertxTestContext testContext) {

        createHttpServer(vertx, "", HttpStatusCode.NOT_FOUND)
            .onSuccess(httpServer -> {

                ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(httpServer.actualPort()));

                String jwt = generateJWT(vertx, "bar", "foo-jwk-uri-key-id", 60);

                provider.authenticate(new TokenCredentials(jwt),
                    testContext.failing(t -> testContext.verify(() -> {
                        Assertions.assertThat(t).hasMessage("Error retrieving jwks for issuer: bar. Status: 404.");
                        testContext.completeNow();
                    }))
                );
            })
            .onFailure(testContext::failNow);

    }

    @Test
    public void test_authenticate_whenJwksConfigurationIsProvided_expectFailure_invalidJWTPayloadIssuerMissing(Vertx vertx, VertxTestContext testContext) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(-1));

        String jwt = generateJWT(vertx, null, "foo-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid JWT payload. Missing 'issuer'.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenInvalidJWT_expectFailure_invalidJWT(Vertx vertx, VertxTestContext testContext) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(-1));

        String jwt = "some_invalid_jwt";

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Not enough or too many segments [1]");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenJwksConfigurationIsProvided_expectFailure_invalidJWTIssuer(Vertx vertx, VertxTestContext testContext) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(10000));

        String jwt = generateJWT(vertx, "invalid", "bar-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid JWT issuer: invalid.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenJwksConfigurationIsProvided_expectFailure_whenTokenExpired(Vertx vertx, VertxTestContext testContext) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(-1));


        String jwt = generateJWT(vertx, "foo", "foo-key-id", -60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid JWT token: token expired.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenNoJwkConfigIsProvided_expectFailure_invalidConfiguration(Vertx vertx, VertxTestContext testContext) {

        JsonObject config = new JsonObject()
            .put("clients", new JsonArray().add(
                new JsonObject()
                    .put("issuer", "foo")
            ));


        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, config);

        String jwt = generateJWT(vertx, "foo", "foo-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid configuration for issuer: foo. Missing both 'jwks-uri' and 'jwks'.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenJwksUriConfigurationIsProvidedWithEmptyUri_expectFailure_invalidConfigurationForIssuerMissingUri(Vertx vertx, VertxTestContext testContext) {

        JsonObject config = new JsonObject()
            .put("clients", new JsonArray().add(
                new JsonObject()
                    .put("issuer", "foo")
                    .put("jwks-uri", new JsonObject())
            ));

        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, config);

        String jwt = generateJWT(vertx, "foo", "foo-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid configuration for issuer: foo. Missing 'uri' for 'jwks-uri'.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticate_whenJwksUriConfigurationIsProvidedWithEmptyInvalidUri_expectFailure_invalidConfigurationForIssuerInvalidUri(Vertx vertx, VertxTestContext testContext) {

        JsonObject config = new JsonObject()
            .put("clients", new JsonArray().add(
                new JsonObject()
                    .put("issuer", "foo")
                    .put("jwks-uri", new JsonObject().put("uri", "some_invalid_uri|"))
            ));
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, config);

        String jwt = generateJWT(vertx, "foo", "foo-key-id", 60);

        provider.authenticate(new TokenCredentials(jwt),
            testContext.failing(t -> testContext.verify(() -> {
                Assertions.assertThat(t).hasMessage("Invalid configuration for issuer: foo. Invalid 'uri' property for 'jwks-uri'.");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_authenticateJsonObjectArgument_whenJwksConfigurationIsProvided_expectSuccess(Vertx vertx, VertxTestContext testContext) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, clientsConfig(-1));

        String jwt = generateJWT(vertx, "foo", "foo-key-id", 60);

        provider.authenticate(new JsonObject().put("token", jwt),
            testContext.succeeding(u -> testContext.verify(() -> {
                Assertions.assertThat(u).isNotNull();
                Assertions.assertThat(u.principal().getString("access_token")).isEqualTo(jwt);
                Assertions.assertThat(u.attributes().getString("iss")).isEqualTo("foo");
                Assertions.assertThat(u.attributes().getString("sub")).isEqualTo("foo");
                testContext.completeNow();
            }))
        );

    }

    @Test
    public void test_generateToken_withClaimsAndOptions_expectUnsupportedOperationException(Vertx vertx) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, new JsonObject());

        Assertions.assertThatThrownBy(() -> provider.generateToken(new JsonObject(), new JWTOptions()))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Unsupported operation: generateToken.");

    }

    @Test
    public void test_generateToken_withClaims_expectUnsupportedOperationException(Vertx vertx) {
        ConfigJWTAuthProvider provider = new ConfigJWTAuthProvider(vertx, new JsonObject());

        Assertions.assertThatThrownBy(() -> provider.generateToken(new JsonObject()))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Unsupported operation: generateToken.");

    }


    private String generateJWT(Vertx vertx, String issuer, String keyId, int expiresInSeconds) {
        return JWTUtil.builder(vertx)
            .setKeyId(keyId)
            .setAlgorithm("RS512")
            .setPrivateKeyPem(JWTUtil.JWK_RSA_PRIVATE_KEY)
            .setIssuer(issuer)
            .setSubject(issuer)
            .setExpiresInSeconds(expiresInSeconds)
            .setClaims(new JsonObject()
                .put("roles", new JsonArray().add("role_1").add("role_2"))
            )
            .build()
            .jwt();
    }

    private JsonObject clientsConfig(int port) {
        return new JsonObject()
            .put("clients", new JsonArray()
                .add(new JsonObject()
                    .put("issuer", "foo")
                    .put("jwks", new JsonArray()
                        .add(new JsonObject()
                            .put("key-id", "foo-key-id")
                            .put("algorithm", "RS512")
                            .put("public-key-pem", JWTUtil.JWK_RSA_PUBLIC_KEY)
                        )
                    )
                )
                .add(new JsonObject()
                    .put("issuer", "bar")
                    .put("jwks-uri", new JsonObject()
                        .put("uri", String.format("http://localhost:%d/keys", port))
                        .put("cache", new JsonObject().put("size", 2).put("timeToLiveSeconds", 60))
                    )
                )
            );

    }


    private Future<HttpServer> createHttpServer(Vertx vertx, String response, HttpStatusCode statusCode) {
        return vertx.createHttpServer()
            .requestHandler(request -> {
                if (request.method() != HttpMethod.GET) {
                    request.response().setStatusCode(HttpStatusCode.METHOD_NOT_ALLOWED.code()).end(String.format("Method %s not allowed", request.method()));
                }
                request.response().setStatusCode(statusCode.code()).putHeader("Content-Type", "application/json").end(response);
            })
            .listen(RandomPort.port());
    }

}