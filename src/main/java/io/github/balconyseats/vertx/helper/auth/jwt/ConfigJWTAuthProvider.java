package io.github.balconyseats.vertx.helper.auth.jwt;

import io.github.balconyseats.vertx.helper.http.HttpStatusCode;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.impl.jose.JWK;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/**
 * Validate jwt using configuration:
 *
 * <pre>
 *     clients:
 *       - issuer: jwks-uri-client
 *         jwks-uri:
 *           uri: 'http://localhost:8080/keys'
 *           proxy:
 *             host: 'localhost'`
 *             port: 8081
 *           cache:
 *             size: 2
 *             timeToLiveSeconds: 3600
 *       - issuer: jwks-pem-client
 *         jwks:
 *           - key-id: jwks-jwk-client-key-id
 *             algorithm: RS512
 *             public-key-pem: |
 *               -----BEGIN PUBLIC KEY-----
 *               ...
 *               -----END PUBLIC KEY-----
 *           - key-id: jwks-jwk-client-key-id-2
 *             algorithm: RS512
 *             public-key-pem: |
 *               -----BEGIN PUBLIC KEY-----
 *               ...
 *               -----END PUBLIC KEY-----
 * </pre>
 * <p>
 * On success authentication return {@link User}
 * with principal contains all attributes from JWT and plain jwt token in attribute 'access_token'
 * </p>
 */
public class ConfigJWTAuthProvider implements JWTAuth {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigJWTAuthProvider.class);

    private final Vertx vertx;

    private final Map<String, JsonObject> clientOptionsByIssuer = new HashMap<>();

    public ConfigJWTAuthProvider(Vertx vertx, JsonObject clientsConfig) {
        this.vertx = vertx;
        JsonArray clients = clientsConfig.getJsonArray("clients");
        if (clients != null) {
            clients.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEach(c -> Optional.of(c.getString("issuer")).ifPresent(iss -> clientOptionsByIssuer.put(iss, c)));
        }
    }

    /**
     * Authenticates user and calls result handler with {@link User} which has
     * <br>
     * <ul>
     *  <li>principal - {@link JsonObject} with claims from token and plain token value in 'access_token'</li>
     *  <li>attributes - {@link JsonObject} with claims from received JWT.</li>
     * </ul>
     *
     * @param credentials credentials with token
     * @param resultHandler result handler for authentication result
     */
    @Override
    public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
        try {
            TokenCredentials tokenCredentials = (TokenCredentials) credentials;
            tokenCredentials.checkValid(null);

            //retrieve issuer
            JsonObject parsedJwt = JWT.parse(tokenCredentials.getToken());
            JsonObject payload = parsedJwt.getJsonObject("payload");
            String issuer = payload.getString("iss");
            if (StringUtils.isBlank(issuer)) {
                resultHandler.handle(Future.failedFuture("Invalid JWT payload. Missing 'issuer'."));
                return;
            }

            //retrieve clientOptions
            JsonObject clientOption = clientOptionsByIssuer.get(issuer);
            if (clientOption == null) {
                resultHandler.handle(Future.failedFuture(String.format("Invalid JWT issuer: %s.", issuer)));
                return;
            }
            //validate jwt
            jwt(clientOption)
                    .onSuccess(jwt -> {
                        JsonObject jwtToken = jwt.decode(tokenCredentials.getToken());
                        User user = createUser(tokenCredentials.getToken(), jwtToken);
                        if (user.expired()) {
                            resultHandler.handle(Future.failedFuture("Invalid JWT token: token expired."));
                            return;
                        }
                        resultHandler.handle(Future.succeededFuture(user));
                        return;
                    })
                    .onFailure(t -> resultHandler.handle(Future.failedFuture(t)));

        } catch (Exception e) {
            resultHandler.handle(Future.failedFuture(e));
        }
    }

    private Future<JWT> jwt(JsonObject clientOptions) {
        String issuer = clientOptions.getString("issuer");
        JsonObject jwksUriConfig = clientOptions.getJsonObject("jwks-uri");
        JsonArray jwksConfig = clientOptions.getJsonArray("jwks");

        // if both configurations are empty then fail
        if (jwksUriConfig == null && (jwksConfig == null || jwksConfig.size() == 0)) {
            return Future.failedFuture(String.format("Invalid configuration for issuer: %s. Missing both 'jwks-uri' and 'jwks'.", issuer));
        }

        return Future.succeededFuture(new JWT())
                .compose(configureJWTWithJwks(issuer, jwksConfig))
                .compose(configureJWTWithJwksUri(issuer, jwksUriConfig));

    }

    private Function<JWT, Future<JWT>> configureJWTWithJwks(String issuer, JsonArray config) {
        return jwt -> {
            if (config != null) {
                config.stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .forEach(jwk -> {
                            PubSecKeyOptions pubSecKeyOptions = new PubSecKeyOptions()
                                    .setId(jwk.getString("key-id"))
                                    .setAlgorithm(jwk.getString("algorithm"))
                                    .setBuffer(jwk.getString("public-key-pem"));
                            jwt.addJWK(new JWK(pubSecKeyOptions));
                        });
            }
            return Future.succeededFuture(jwt);
        };
    }

    private Function<JWT, Future<JWT>> configureJWTWithJwksUri(String issuer, JsonObject config) {
        return jwt -> {
            if (config != null) {
                String uriValue = config.getString("uri");
                if (StringUtils.isBlank(uriValue)) {
                    return Future.failedFuture(
                        String.format("Invalid configuration for issuer: %s. Missing 'uri' for 'jwks-uri'.", issuer));
                }

                URI uri;
                try {
                    uri = URI.create(uriValue);
                } catch (IllegalArgumentException e) {
                    return Future.failedFuture(
                        String.format("Invalid configuration for issuer: %s. Invalid 'uri' property for 'jwks-uri'.",
                            issuer));
                }

                WebClientOptions webClientOptions = new WebClientOptions();

                // setup proxy if needed
                JsonObject proxy = config.getJsonObject("proxy");
                if (proxy != null) {
                    String host = proxy.getString("host");
                    Integer port = proxy.getInteger("port");
                    LOGGER.debug("Configuring proxy for issuer: {} and uri: {} with host: {} and port: {}", issuer, uri, host, port);
                    if (host != null && port != null) {
                        webClientOptions.setProxyOptions(new ProxyOptions(proxy));
                    }
                }

                return WebClient.create(vertx, webClientOptions)
                        .get(uri.getPort(), uri.getHost(), uri.getPath())
                        .send()
                        .flatMap(resp -> {
                            if (resp.statusCode() == HttpStatusCode.OK.code()) {
                                JsonObject response = resp.bodyAsJsonObject();
                                var keys = response.getJsonArray("keys");

                                keys.stream()
                                        .filter(JsonObject.class::isInstance)
                                        .map(JsonObject.class::cast)
                                        .forEach(jwk -> jwt.addJWK(new JWK(jwk)));

                                return Future.succeededFuture(jwt);
                            }
                            LOGGER.error("Error retrieving jwks for issuer: {} from uri: {}, status: {}, response: {}",
                                issuer, uri, resp.statusCode(), resp.bodyAsString());
                            return Future.failedFuture(
                                String.format("Error retrieving jwks for issuer: %s. Status: %s.",
                                    issuer, resp.statusCode()));
                        });
            }
            return Future.succeededFuture(jwt);
        };
    }

    private User createUser(String token, JsonObject jwt) {
        JsonObject principal = jwt.copy()
            .put("access_token", token);
        User user = User.create(principal, new JsonObject().put("accessToken", jwt).mergeIn(jwt));
        return user;
    }

    @Override
    public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
        authenticate(new TokenCredentials(credentials.getString("token")), resultHandler);
    }

    @Override
    public String generateToken(JsonObject claims, JWTOptions options) {
        throw new UnsupportedOperationException("Unsupported operation: generateToken.");
    }

    @Override
    public String generateToken(JsonObject claims) {
        return generateToken(claims, null);
    }

    public static ConfigJWTAuthProvider instance(Vertx vertx, JsonObject clients) {
        return new ConfigJWTAuthProvider(vertx, clients);
    }

}
