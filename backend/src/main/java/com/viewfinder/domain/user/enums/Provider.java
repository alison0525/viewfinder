package com.viewfinder.domain.user.enums;

public enum Provider {
    LOCAL("이메일"),
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글");

    private final String displayName;

    // 클라이언트 안내 문구에 사용할 제공자 한글 이름 저장
    Provider(String displayName) {
        this.displayName = displayName;
    }

    // 사용자에게 보여 줄 로그인 제공자 이름 반환
    public String getDisplayName() {
        return displayName;
    }
}
