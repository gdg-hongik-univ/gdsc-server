package com.gdschongik.gdsc.domain.discord.dto.response;

public record DiscordGithubHandleResponse(String previousGithubHandle, String currentGithubHandle) {

    private static final int VISIBLE_LENGTH = 4;

    public static DiscordGithubHandleResponse of(String previousGithubHandle, String currentGithubHandle) {
        return new DiscordGithubHandleResponse(maskHandle(previousGithubHandle), currentGithubHandle);
    }

    /**
     * 앞 4자까지 노출하고 나머지를 '*'로 마스킹합니다. 4자 이하인 경우 최소 1자를 마스킹합니다.
     * 예: "johndoe" → "john***", "abc" → "ab*", "a" → "*"
     */
    private static String maskHandle(String handle) {
        int visibleLength = Math.min(handle.length() - 1, VISIBLE_LENGTH);
        int maskLength = handle.length() - visibleLength;
        return handle.substring(0, visibleLength) + "*".repeat(maskLength);
    }
}
