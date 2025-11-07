package com.stockmate.parts.api.parts.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Parts Entity 테스트")
class PartsTest {

    private Parts parts;

    @BeforeEach
    void setUp() {
        parts = new Parts();
        parts.setId(1L);
        parts.setName("에어필터");
        parts.setPrice(50000L);
        parts.setCost(30000L);
        parts.setImage("image_url");
        parts.setTrim("터보");
        parts.setModel("소나타");
        parts.setCategory(1);
        parts.setKorName("현대");
        parts.setEngName("Hyundai");
        parts.setCategoryName("엔진부품");
        parts.setAmount(100);
        parts.setCode("PART-001");
        parts.setLocation("A1-1");
        parts.setWeight(0.5);
        
        List<String> codes = new ArrayList<>();
        codes.add("CODE-001");
        codes.add("CODE-002");
        parts.setCode_(codes);
    }

    @Test
    @DisplayName("Parts 엔티티 생성 테스트")
    void createParts() {
        // then
        assertThat(parts.getId()).isEqualTo(1L);
        assertThat(parts.getName()).isEqualTo("에어필터");
        assertThat(parts.getPrice()).isEqualTo(50000L);
        assertThat(parts.getCost()).isEqualTo(30000L);
        assertThat(parts.getAmount()).isEqualTo(100);
        assertThat(parts.getLocation()).isEqualTo("A1-1");
        assertThat(parts.getCategoryName()).isEqualTo("엔진부품");
    }

    @Test
    @DisplayName("Parts 재고 수량 업데이트 테스트")
    void updateAmount() {
        // when
        parts.setAmount(150);

        // then
        assertThat(parts.getAmount()).isEqualTo(150);
    }

    @Test
    @DisplayName("Parts 위치 변경 테스트")
    void updateLocation() {
        // when
        parts.setLocation("B2-3");

        // then
        assertThat(parts.getLocation()).isEqualTo("B2-3");
    }

    @Test
    @DisplayName("Parts 가격 업데이트 테스트")
    void updatePrice() {
        // when
        parts.setPrice(60000L);

        // then
        assertThat(parts.getPrice()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("Parts 원가 업데이트 테스트")
    void updateCost() {
        // when
        parts.setCost(35000L);

        // then
        assertThat(parts.getCost()).isEqualTo(35000L);
    }

    @Test
    @DisplayName("Parts 코드 리스트 테스트")
    void codeList() {
        // given
        List<String> newCodes = new ArrayList<>();
        newCodes.add("CODE-003");
        newCodes.add("CODE-004");

        // when
        parts.setCode_(newCodes);

        // then
        assertThat(parts.getCode_()).hasSize(2);
        assertThat(parts.getCode_()).contains("CODE-003", "CODE-004");
    }

    @Test
    @DisplayName("Parts 카테고리 정보 테스트")
    void categoryInfo() {
        // then
        assertThat(parts.getCategory()).isEqualTo(1);
        assertThat(parts.getCategoryName()).isEqualTo("엔진부품");
    }

    @Test
    @DisplayName("Parts 차량 정보 테스트")
    void vehicleInfo() {
        // then
        assertThat(parts.getKorName()).isEqualTo("현대");
        assertThat(parts.getEngName()).isEqualTo("Hyundai");
        assertThat(parts.getModel()).isEqualTo("소나타");
        assertThat(parts.getTrim()).isEqualTo("터보");
    }

    @Test
    @DisplayName("Parts 재고 부족 확인 테스트")
    void checkLowStock() {
        // given
        parts.setAmount(5);
        int threshold = 10;

        // when
        boolean isLowStock = parts.getAmount() < threshold;

        // then
        assertThat(isLowStock).isTrue();
    }

    @Test
    @DisplayName("Parts 재고 충분 확인 테스트")
    void checkSufficientStock() {
        // given
        parts.setAmount(100);
        int threshold = 10;

        // when
        boolean isSufficient = parts.getAmount() >= threshold;

        // then
        assertThat(isSufficient).isTrue();
    }

    @Test
    @DisplayName("Parts 창고 위치 파싱 테스트")
    void parseLocation() {
        // given
        parts.setLocation("A1-1");

        // when
        String location = parts.getLocation();
        String warehouse = location.substring(0, 1);
        String[] parts = location.split("-");
        String section = parts.length > 0 ? parts[0].substring(1) : "";
        String floor = parts.length > 1 ? parts[1] : "";

        // then
        assertThat(warehouse).isEqualTo("A");
        assertThat(section).isEqualTo("1");
        assertThat(floor).isEqualTo("1");
    }

    @Test
    @DisplayName("Parts 전체 필드 업데이트 테스트")
    void updateAllFields() {
        // when
        parts.setName("오일필터");
        parts.setPrice(40000L);
        parts.setCost(25000L);
        parts.setAmount(200);
        parts.setLocation("B2-2");
        parts.setCategoryName("엔진부품");

        // then
        assertThat(parts.getName()).isEqualTo("오일필터");
        assertThat(parts.getPrice()).isEqualTo(40000L);
        assertThat(parts.getCost()).isEqualTo(25000L);
        assertThat(parts.getAmount()).isEqualTo(200);
        assertThat(parts.getLocation()).isEqualTo("B2-2");
    }
}

