package com.stockmate.parts.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * 200
     */
    SEND_REGISTER_SUCCESS(HttpStatus.OK,"회원가입 성공"),
    SEND_LOGIN_SUCCESS(HttpStatus.OK,"로그인 성공"),
    SEND_REISSUE_TOKEN_SUCCESS(HttpStatus.OK,"토큰 재발급 성공"),
    SEND_HEALTH_CHECK_SUCCESS(HttpStatus.OK,"서버 상태 체크 성공"),

    /**
     * 201
     */
    CREATE_NOTICE_SUCCESS(HttpStatus.CREATED, "공지사항 등록 성공"),
    SAVE_TERM_SUCCESS(HttpStatus.CREATED,"약관 등록 성공"),
    CREATE_FAQ_SUCCESS(HttpStatus.CREATED, "FAQ 등록 성공"),
    SAVE_COMPANYINFO_SUCCESS(HttpStatus.CREATED,"회사 정보 등록 성공"),
    SAVE_SERVICEINFO_SUCCESS(HttpStatus.CREATED,"서비스 정보 등록 성공"),
    SAVE_HOMEINFO_SUCCESS(HttpStatus.CREATED,"홈 화면 정보 등록 성공"),
    CREATE_NEWS_SUCCESS(HttpStatus.CREATED,"뉴스 등록 성공"),
    CREATE_ALLIANCE_SUCCESS(HttpStatus.CREATED,"제휴 정보 등록 성공"),
    SEND_SETTLEMENT_UPLOAD_SUCCESS(HttpStatus.CREATED,"정산 내역 등록 성공"),
    CREATE_FRANCHISE_SUCCESS(HttpStatus.CREATED,"가맹점 등록 성공"),
    SAVE_CONTACT_SUCCESS(HttpStatus.CREATED,"문의처 정보 등록 성공"),
    CREATE_CATEGORY_SUCCESS(HttpStatus.CREATED,"카테고리 생성 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}