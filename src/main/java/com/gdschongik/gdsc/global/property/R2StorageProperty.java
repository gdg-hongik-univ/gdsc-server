package com.gdschongik.gdsc.global.property;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "storage.r2")
public class R2StorageProperty {

    private final String endpoint;
    private final String bucket;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final Duration uploadUrlTtl;
    private final Duration downloadUrlTtl;
}
