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

    // 지점 관련
    STORE_ANALYSIS_SUCCESS(HttpStatus.OK, "재고 분석 조회 성공"),
    STORE_EXPORT_SUCCESS(HttpStatus.OK, "재고 분석 내보내기 성공"),
    STORE_DASHBOARD_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리별 재고 비중 조회 성공"),
    STORE_SEARCH_SUCCESS(HttpStatus.OK, "지점 재고 조회 성공"),
    STORE_UNDER_LIMIT_SUCCESS(HttpStatus.OK, "지점 부족 재고 조회 성공"),
    STORE_CATEGORY_LACK_COUNT_SUCCESS(HttpStatus.OK, "카테고리별 부족 재고 조회 성공"),
    STORE_FIND_NAME_SUCCESS(HttpStatus.OK, "부품 검색 성공"),

    STORE_LIMIT_UPDATE_SUCCESS(HttpStatus.OK, "최소 필요 수량 변경 성공"),

    // 본사 재고 관련
    PART_DISTRIBUTION_SUCCESS(HttpStatus.OK, "부품 분포 조회 성공"),
    PARTS_STOCK_CHECK_SUCCESS(HttpStatus.OK, "주문 가능 여부 조회 성공"),
    PARTS_DETAIL_SUCCESS(HttpStatus.OK, "부품 상세 조회 성공"),
    PARTS_LIST_SUCCESS(HttpStatus.OK, "부품 목록 조회 성공"),
    PARTS_MODEL_CATEGORY_SUCCESS(HttpStatus.OK, "부품 검색 성공"),
    PARTS_LACK_STOCK(HttpStatus.OK, "부족 재고 조회 성공"),
    PARTS_CATEGORY_AMOUNT(HttpStatus.OK, "카테고리별 재고 수 조회 성공"),
    PARTS_STOCK_DEDUCTION_SUCCESS(HttpStatus.OK, "재고 차감 성공"),

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
    UPDATE_STORE_INVENTORY_SUCCESS(HttpStatus.OK,"가맹점 재고 업데이트 성공"),
    RELEASE_STOCK_SUCCESS(HttpStatus.OK,"가맹점 부품 출고 처리 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}