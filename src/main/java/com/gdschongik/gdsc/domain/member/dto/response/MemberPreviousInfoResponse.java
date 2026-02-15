package com.gdschongik.gdsc.domain.member.dto.response;

public record MemberPreviousInfoResponse(Long previousMemberId, String previousGithubHandle, String previousEmail) {

    private static final int VISIBLE_LENGTH = 4;

    public static MemberPreviousInfoResponse of(
            Long previousMemberId, String previousGithubHandle, String previousEmail) {
        return new MemberPreviousInfoResponse(
                previousMemberId, maskWithAsterisks(previousGithubHandle), maskEmail(previousEmail));
    }

    /**
     * 앞 4자까지 노출하고 나머지를 '*'로 마스킹합니다. 4자 이하인 경우 최소 1자를 마스킹합니다.
     * 예: "johndoe" → "john***", "abc" → "ab*", "a" → "*"
     */
    private static String maskWithAsterisks(String handle) {
        int visibleLength = Math.min(handle.length() - 1, VISIBLE_LENGTH);
        int maskLength = handle.length() - visibleLength;
        return handle.substring(0, visibleLength) + "*".repeat(maskLength);
    }

    /**
     * 이메일의 '@' 앞 부분에 동일한 마스킹 규칙을 적용합니다.
     * 예: "johndoe@gmail.com" → "john***@gmail.com", "abc@gmail.com" → "ab*@gmail.com"
     */
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        return maskWithAsterisks(localPart) + domainPart;
    }
}
