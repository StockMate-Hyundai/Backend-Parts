package com.stockmate.parts.api.parts.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoreInventory Entity 테스트")
class StoreInventoryTest {

    private StoreInventory storeInventory;
    private Parts part;

    @BeforeEach
    void setUp() {
        part = new Parts();
        part.setId(1L);
        part.setName("에어필터");
        part.setPrice(50000L);
        part.setCategoryName("엔진부품");

        storeInventory = StoreInventory.builder()
                .id(1L)
                .amount(50)
                .limitAmount(20)
                .part(part)
                .userId(1L)
                .build();
    }

    @Test
    @DisplayName("StoreInventory 엔티티 생성 테스트")
    void createStoreInventory() {
        // then
        assertThat(storeInventory.getId()).isEqualTo(1L);
        assertThat(storeInventory.getAmount()).isEqualTo(50);
        assertThat(storeInventory.getLimitAmount()).isEqualTo(20);
        assertThat(storeInventory.getPart().getId()).isEqualTo(1L);
        assertThat(storeInventory.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("StoreInventory 재고 수량 업데이트 테스트")
    void updateAmount() {
        // when
        storeInventory.setAmount(100);

        // then
        assertThat(storeInventory.getAmount()).isEqualTo(100);
    }

    @Test
    @DisplayName("StoreInventory 최소 필요 수량 업데이트 테스트")
    void updateLimitAmount() {
        // when
        storeInventory.setLimitAmount(30);

        // then
        assertThat(storeInventory.getLimitAmount()).isEqualTo(30);
    }

    @Test
    @DisplayName("StoreInventory 부족 재고 확인 테스트 - 부족")
    void checkLowStock_True() {
        // given
        storeInventory.setAmount(10);
        storeInventory.setLimitAmount(20);

        // when
        boolean isLowStock = storeInventory.getAmount() < storeInventory.getLimitAmount();

        // then
        assertThat(isLowStock).isTrue();
    }

    @Test
    @DisplayName("StoreInventory 부족 재고 확인 테스트 - 충분")
    void checkLowStock_False() {
        // given
        storeInventory.setAmount(50);
        storeInventory.setLimitAmount(20);

        // when
        boolean isLowStock = storeInventory.getAmount() < storeInventory.getLimitAmount();

        // then
        assertThat(isLowStock).isFalse();
    }

    @Test
    @DisplayName("StoreInventory 재고 추가 테스트")
    void addStock() {
        // given
        int currentAmount = storeInventory.getAmount();
        int addAmount = 30;

        // when
        storeInventory.setAmount(currentAmount + addAmount);

        // then
        assertThat(storeInventory.getAmount()).isEqualTo(80);
    }

    @Test
    @DisplayName("StoreInventory 재고 차감 테스트")
    void deductStock() {
        // given
        int currentAmount = storeInventory.getAmount();
        int deductAmount = 20;

        // when
        storeInventory.setAmount(currentAmount - deductAmount);

        // then
        assertThat(storeInventory.getAmount()).isEqualTo(30);
    }

    @Test
    @DisplayName("StoreInventory 재고 부족 여부 경계값 테스트")
    void checkLowStock_Boundary() {
        // given - 정확히 limitAmount와 같은 경우
        storeInventory.setAmount(20);
        storeInventory.setLimitAmount(20);

        // when
        boolean isLowStock = storeInventory.getAmount() < storeInventory.getLimitAmount();

        // then
        assertThat(isLowStock).isFalse(); // 같으면 부족하지 않음
    }

    @Test
    @DisplayName("StoreInventory 여러 가맹점 재고 테스트")
    void multipleStoreInventories() {
        // given
        StoreInventory store1 = StoreInventory.builder()
                .amount(50)
                .limitAmount(20)
                .part(part)
                .userId(1L)
                .build();

        StoreInventory store2 = StoreInventory.builder()
                .amount(30)
                .limitAmount(20)
                .part(part)
                .userId(2L)
                .build();

        // then
        assertThat(store1.getUserId()).isEqualTo(1L);
        assertThat(store2.getUserId()).isEqualTo(2L);
        assertThat(store1.getPart().getId()).isEqualTo(store2.getPart().getId());
    }

    @Test
    @DisplayName("StoreInventory limitAmount null 처리 테스트")
    void limitAmountNull() {
        // given
        storeInventory.setLimitAmount(null);

        // when
        boolean isLowStock = storeInventory.getLimitAmount() != null 
                && storeInventory.getAmount() < storeInventory.getLimitAmount();

        // then
        assertThat(isLowStock).isFalse();
    }
}

