package io.github.balconyseats.vertx.helper.test.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.time.Instant;
import java.util.List;

public class JWTUtil {

    public static final String JWK_RSA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxyibm0s184kuSgzCX9NE\n" +
            "uNxvPZQpxb2jk8hU34EaCoh+WfnQLmPnwJZwisLkVcrg3nu4ikEZQnHd9Mqrj2of\n" +
            "UO8/9uMhSeBOQUZTIi3/jnZHsNMTsmIrJ56WqSZhpf8Nv4YZByw53OaEcVxslwYH\n" +
            "58pp9VR7dB3DRQNEKp0/hVpcEhLQN402WT+kEp8JPNX/gBv9L0LlSHc7FxX1tlQW\n" +
            "zvRfWGgfkeNBTURA/k9JJheqVSXXGqDjHBhei9wMkR/4w+udYVeG9k6IlUw6WrXv\n" +
            "CAoQbrA8xC3nm2ln0vSNgmhZHaqe2HnsiZbthzZK8FwajC6D4ylsZ8r3x4JVFYkh\n" +
            "VQIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    public static final String JWK_RSA_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDHKJubSzXziS5K\n" +
            "DMJf00S43G89lCnFvaOTyFTfgRoKiH5Z+dAuY+fAlnCKwuRVyuDee7iKQRlCcd30\n" +
            "yquPah9Q7z/24yFJ4E5BRlMiLf+Odkew0xOyYisnnpapJmGl/w2/hhkHLDnc5oRx\n" +
            "XGyXBgfnymn1VHt0HcNFA0QqnT+FWlwSEtA3jTZZP6QSnwk81f+AG/0vQuVIdzsX\n" +
            "FfW2VBbO9F9YaB+R40FNRED+T0kmF6pVJdcaoOMcGF6L3AyRH/jD651hV4b2ToiV\n" +
            "TDpate8IChBusDzELeebaWfS9I2CaFkdqp7YeeyJlu2HNkrwXBqMLoPjKWxnyvfH\n" +
            "glUViSFVAgMBAAECggEAfGGOHUSxozGPqUuisXoet11kVA8Or6KqYIeDCjt06hMo\n" +
            "5kSwkehvOsY570+cj0AvhIO8UwwpHGhE+LcCwoTCnidtyWE8zwi5lua20OftbSnN\n" +
            "yE9rQ8MvtITFD0sbPTK3gV/cjbA/Swjkum/Amel4UnkGB+QFXhWgeJ97mSmwHDWi\n" +
            "l3MNErXgLO3R39KhVlD596TPMg9r32ZpFP0kBAssSxb4n8Xq752I8m4kF2aiAYfP\n" +
            "L+FbfVTCmBQKpuQE83pp3xILr0Zeu0sB+AXpRVV7ofOBzO0/V30anMEfDARVwqr9\n" +
            "sf1UM6pwcoqQVYhdcr9J4+Td4mlOG94LRJIiZ5O+wQKBgQDoTBQ4F1sb+SlUBedV\n" +
            "+sIWt0EnTQjnNtvvGmmbXpnuajgzrJAKHHiS7fWJ+Dw0v9fEdv9wHOqV0yjso/kx\n" +
            "UYVw+K7/O+KcnCnTrdl12LIaUKbilhkeykPhkUhnU7jQBGgueM6iTASpTfNjz8V1\n" +
            "QjXssNnz1c4EjzVTnV9ffWyxiQKBgQDbeud8jpuv9HIjLnoF1nJtQLNDhWRmIJp0\n" +
            "qHXOggNiYmNOqtCfubBUIaScsd7uxVU4t8fv4xQbyx08hfw52785UIwlcxz9DjB7\n" +
            "fjwILLXUi3XrW235CDWXPy6TXCYmmbQonIJh13V/5XHCLqx45JJnh3Of1KusdsZB\n" +
            "3R8TTiS6bQKBgAqOOnT6CrfQsKISyppLlLo3hYxlobvv8DqDNr1KpTACP5+JfdoX\n" +
            "xRrYGUo1r3auKP3ScnUDu10nhTqLkO0+yRODGWCbtgZryjLGOZVzTVSIyayOKYIO\n" +
            "qtr7IREg6sr5CErkTm9tiOCwnBiy/kP/yDeDKcQ8uOnYIBI2mnjaWDARAoGAW1e0\n" +
            "PY9wVfNrCR9Po1xTdQqnUKOUXDiXb5Ooq9BKLkj6QYQvnTn/m5QA2UHhoQe5Zwh9\n" +
            "imW8ObD3svekOEQvOmA+Y1ucJekg3lR0EYbiCdocsl3zz9/NxeuCq8t/CTOQ2hIA\n" +
            "5U1xVlAcXw9jslCWacXiphMxe25XJpQrWbJ88KUCgYEA1hPzOwyToXFW1qC5+IA/\n" +
            "70DoSNKwzegnICefm+HakF7m0FzoqJrR8G6BofQs4xF3iocRup02OIcOFtIjBM5l\n" +
            "XHTW39F3oFIDM5UDEyC/1BmJcRsyf6oXxCXuQZ1/Cry3a4PGmS2drsqo6HI6mwVt\n" +
            "6eO1KwX7l8PeLozyhMRkS8A=\n" +
            "-----END PRIVATE KEY-----";

    public static final String JWK_RSA_UNKNOWN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDWXhr5ev+xGXts\n" +
            "rcWhFctx0Z3/lWpQoPdTJXN4V7QvMCEvCHAftEN5f4KIvz5zmYLDavAtTLVgTUlL\n" +
            "JBHJAAPkMMGscfnf8vUsv/xC3Hjb5lbi8iyPLw5dUoXDCot2Z63KA6U8Iiwkqyx0\n" +
            "Y+Odegjakn5g8OFbBSRyLx8SmAl47ErQL3I7nrKVJ7OvkaEVVX1dUlWxNggEF1y2\n" +
            "Ntgb+zm4XWAPom5ir4FfMR4JxlvkNNeCoGGvHDOo/0KJDSuG2Ar99Yj/+ckh6Wc7\n" +
            "nTGtb7mPwxZ46dB+/kVL+I1Rak+k+Dct5j7ehaFfnEEV60wGxIN6kg68+MVYOlwb\n" +
            "0K2eya9jAgMBAAECggEALP0O+DDCHK7WYlaN1WK3kamGWS9IYqeFgGOpuiXd4NFf\n" +
            "BXnl3iF8z7Dc+kn4l1YuRUjaIweoQQ+3RBfLy70/E9LPspsw77kTj1KzZGzUyIGs\n" +
            "1nAwvdQIVbmN7QL3hd/4bx8YvGgHj6KE7+BO0qvb09NOxpWHFg0cOwJ8qPBTfJbo\n" +
            "EmXCjy9QysonqPyiryfWDUGHf8+z74ktNs0A3ybyroJFNerbYa0OyxnKAyIbcrKb\n" +
            "Aek/0zdIjrEa9nTmnuA6soofUZ/gMnMok1qXZBETWglBF7yMTTYXKRe0vh98MFea\n" +
            "OaCzPSzZA79IKkP9iF9xDDNPgqyLR4LjHboMLtAcwQKBgQD439R5XjNSjGBdGDGA\n" +
            "MnyZUGW2dvgVYvZbKFZ/6fiWOj7lD5nhlYu7NMwQO5qtfkY+t+rM7NWOh8/Mybcu\n" +
            "ayxW0nkTm+48rcLA7wJA3W6xtDZieWb7bjZO2O8ATAf3SPg+P5Jk0VlvfoZgCV1S\n" +
            "yx49kxCYp6DwL13TnX7zPFKwEwKBgQDcgVoZHzMEH1tgNiMMJ8d76QvzqtOsb6w3\n" +
            "MlWvfICAUxsZIXXugijKcY8iom2r2hMe3lwO/7b1bPIU24SmQ9vFF9phl25sSOgq\n" +
            "g8n62eW3zVF0ztOl8Ufv6bI5CxzW2t5PWIPIOQXP0OyPdYcXMVtsXW314QDDU9qV\n" +
            "9Sih/GMNcQKBgH05ot+u1oQivhoZok6vGKLMIWG+WvXn5a3Yji2uvLZaeGspfnRT\n" +
            "mwuHdWZ1l07UyuOkBzE1uf8NtzGEDff9VUIahXGodCd0BxqtKKUaxigEVZkXHQpG\n" +
            "Wm0yGpA7sYZr4PzxX+f6rtW7BEIudBbEvWKeLSuirQgtzTg9dzNMlw6hAoGABI8c\n" +
            "GMbfXCd10TBtWxP7eOFhoOEYTUllyyJcSSuO0JhnSAa8FHlWdJESf4nSMKpqmMnK\n" +
            "J3k8BCkDfFDpljJy+ck7d6cU3mJHgpGE/I3CxSEWnQ6lq5mofhpb1/gV2Y9e508S\n" +
            "MnKTXy+XpIOujlkz5bKs0lebl25FZ7JQ68B3H9ECgYEAvVeLw1l/P8aXgnK+Qn5s\n" +
            "C7D3TPDJZOZw+LCjVgW2CXg6nwpJM62ZtfsbBOq58NLMLPE5dNT1fqY3dt/o9cn+\n" +
            "T/Q6rVeGNy3GYHCITAeYM1CQFYWjNPqFxPaJkQPcRk6Kl8aiXOoOo9tVM6emqQQ+\n" +
            "tZzOnmHec8d+VWFcIdScxGM=\n" +
            "-----END PRIVATE KEY-----";

    private final Vertx vertx;
    private final String keyId;
    private final String privateKeyPem;
    private final String algorithm;
    private final String issuer;
    private final String subject;
    private final String audience;
    private final JsonObject claims;
    private final int expiresInSeconds;

    public static JWTUtilBuilder builder(Vertx vertx) {
        return new JWTUtilBuilder(vertx);
    }

    private JWTUtil(Vertx vertx, String keyId, String privateKeyPem, String algorithm, String issuer, String subject, String audience, JsonObject claims, int expiresInSeconds) {
        this.vertx = vertx;
        this.keyId = keyId;
        this.privateKeyPem = privateKeyPem;
        this.algorithm = algorithm;
        this.issuer = issuer;
        this.subject = subject;
        this.audience = audience;
        this.claims = claims;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String jwt() {
        JWTAuthOptions options = new JWTAuthOptions();
        options.addPubSecKey(new PubSecKeyOptions()
                .setId(keyId)
                .setAlgorithm(algorithm)
                .setBuffer(privateKeyPem)

        );
        return JWTAuth.create(this.vertx, options)
                .generateToken(new JsonObject()
                                .put("exp", Instant.now().plusSeconds(this.expiresInSeconds).getEpochSecond())
                                .mergeIn(this.claims == null ? new JsonObject() : this.claims),
                        new JWTOptions()
                                .setSubject(this.subject)
                                .setIssuer(this.issuer)
                                .setAudience(audience == null ? List.of() : List.of(this.audience))
                                .setAlgorithm(algorithm)
                );

    }


    public static class JWTUtilBuilder {
        private final Vertx vertx;
        private String keyId;
        private String privateKeyPem;
        private String algorithm;
        private String issuer;
        private String subject;
        private String audience;
        private JsonObject claims;
        private int expiresInSeconds;

        public JWTUtilBuilder(Vertx vertx) {
            this.vertx = vertx;
        }

        public JWTUtilBuilder setKeyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public JWTUtilBuilder setPrivateKeyPem(String privateKeyPem) {
            this.privateKeyPem = privateKeyPem;
            return this;
        }

        public JWTUtilBuilder setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public JWTUtilBuilder setIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public JWTUtilBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public JWTUtilBuilder setAudience(String audience) {
            this.audience = audience;
            return this;
        }

        public JWTUtilBuilder setClaims(JsonObject claims) {
            this.claims = claims;
            return this;
        }

        public JWTUtilBuilder setExpiresInSeconds(int expiresInSeconds) {
            this.expiresInSeconds = expiresInSeconds;
            return this;
        }

        public JWTUtil build() {
            return new JWTUtil(vertx, keyId, privateKeyPem, algorithm, issuer, subject, audience, claims, expiresInSeconds);
        }
    }
}
