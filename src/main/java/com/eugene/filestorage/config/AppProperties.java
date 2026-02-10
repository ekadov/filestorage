package com.eugene.filestorage.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt;
    private S3 s3;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Jwt {
        private String issuer;
        private String secret;
        private long accessTokenTtlSeconds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class S3 {
        private String bucket;
        private String region;
        private String endpoint;
        private String accessKey;
        private String secretKey;
    }
}
