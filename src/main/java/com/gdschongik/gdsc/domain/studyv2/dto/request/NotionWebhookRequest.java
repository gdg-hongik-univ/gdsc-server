package com.gdschongik.gdsc.domain.studyv2.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record NotionWebhookRequest(Source source, Data data) {

    public Semester getSemester() {
        String rawSemester = data.properties.semester.select.name; // ex: "2025-1"
        String[] semesterParts = rawSemester.split("-");

        int academicYear = Integer.parseInt(semesterParts[0]);
        String rawSemesterType = semesterParts[1];

        SemesterType semesterType;
        switch (rawSemesterType) {
            case "1" -> semesterType = SemesterType.FIRST;
            case "2" -> semesterType = SemesterType.SECOND;
            default -> throw new CustomException(ErrorCode.METHOD_ARGUMENT_INVALID);
        }

        return Semester.of(academicYear, semesterType);
    }

    public String getTitle() {
        return data.properties.title.title.get(0).plainText;
    }

    public String getStudyName() {
        return data.properties.study.select.name;
    }

    public String getCleanUrl() {
        return data.properties.cleanUrl.richText.get(0).plainText; // ex: "/25-1/beginner-study/notice/1"
    }

    public record Source(
            String type,
            @JsonProperty("automation_id") UUID automationId,
            @JsonProperty("action_id") UUID actionId,
            @JsonProperty("event_id") UUID eventId,
            @JsonProperty("user_id") UUID userId,
            int attempt) {}

    public record Data(
            String object,
            String id,
            @JsonProperty("created_time") OffsetDateTime createdTime,
            @JsonProperty("last_edited_time") OffsetDateTime lastEditedTime,
            @JsonProperty("created_by") User createdBy,
            @JsonProperty("last_edited_by") User lastEditedBy,
            Object cover,
            Icon icon,
            Parent parent,
            boolean archived,
            @JsonProperty("in_trash") boolean inTrash,
            Properties properties,
            String url,
            @JsonProperty("public_url") String publicUrl,
            @JsonProperty("request_id") UUID requestId) {
        public record User(String object, String id) {}

        public record Icon(String type, String emoji) {}

        public record Parent(String type, @JsonProperty("database_id") String databaseId) {}

        public record Properties(
                @JsonProperty("clean-url") RichTextProperty cleanUrl,
                Title title,
                Semester semester,
                Study study,
                Type type) {

            public record RichTextProperty(String id, String type, @JsonProperty("rich_text") List<RichText> richText) {
                public record RichText(
                        String type,
                        Text text,
                        Annotations annotations,
                        @JsonProperty("plain_text") String plainText,
                        String href) {
                    public record Text(String content, String link) {}

                    public record Annotations(
                            boolean bold,
                            boolean italic,
                            boolean strikethrough,
                            boolean underline,
                            boolean code,
                            String color) {}
                }
            }

            public record Semester(String id, String type, Select select) {
                public record Select(String id, String name, String color) {}
            }

            public record Study(String id, String type, Select select) {
                public record Select(String id, String name, String color) {}
            }

            public record Type(String id, String type, Select select) {
                public record Select(String id, String name, String color) {}
            }

            public record Title(String id, String type, List<RichTextProperty.RichText> title) {}
        }
    }
}
