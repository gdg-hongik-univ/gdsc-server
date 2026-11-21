package com.gdschongik.gdsc.infra.storage.dto;

import static com.gdschongik.gdsc.global.common.constant.TestStorageConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gdschongik.gdsc.global.config.ObjectMapperConfig;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PresignedUrlDtoTest {

    private static final String OBJECT_KEY = UPLOAD_PATH + "/file";
    private static final String OBJECT_URL = R2_ENDPOINT + "/" + R2_BUCKET + "/" + OBJECT_KEY;

    @Test
    void 만료_시각을_ISO_8601_문자열로_직렬화한다() {
        // given
        String expectedExpiresAt = "2026-01-01T00:00:00Z";
        PresignedUrlDto download =
                new PresignedUrlDto(OBJECT_KEY, OBJECT_URL, "GET", Instant.parse(expectedExpiresAt), Map.of());

        // when
        JsonNode json = new ObjectMapperConfig().objectMapper().valueToTree(download);

        // then
        assertThat(json.get("expiresAt")).isEqualTo(TextNode.valueOf(expectedExpiresAt));
    }

    @Test
    void 필수_헤더와_각_헤더의_값은_수정할_수_없다() {
        // given
        String headerName = "content-type";
        Map<String, List<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put(headerName, new ArrayList<>(List.of(CONTENT_TYPE)));

        // when
        PresignedUrlDto upload = new PresignedUrlDto(OBJECT_KEY, OBJECT_URL, "PUT", Instant.EPOCH, requiredHeaders);

        // then
        assertThatThrownBy(() -> upload.requiredHeaders().put("x-custom", List.of("value")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> upload.requiredHeaders().get(headerName).add("image/jpeg"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
