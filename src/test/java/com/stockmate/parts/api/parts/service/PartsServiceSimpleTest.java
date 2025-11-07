package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.parts.*;
import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.repository.PartsRepository;
import com.stockmate.parts.api.parts.repository.StoreRepository;
import com.stockmate.parts.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PartsService 간단 테스트")
class PartsServiceSimpleTest {

    @Mock
    private PartsRepository partsRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PartsService partsService;

    private Parts testPart;

    @BeforeEach
    void setUp() {
        testPart = new Parts();
        testPart.setId(1L);
        testPart.setName("에어필터");
        testPart.setPrice(50000L);
        testPart.setCost(30000L);
        testPart.setCategoryName("엔진부품");
        testPart.setAmount(100);
        testPart.setLocation("A1-1");
        testPart.setModel("소나타");
        testPart.setTrim("터보");
        testPart.setKorName("현대");
        testPart.setEngName("Hyundai");
        testPart.setCategory(1);
        testPart.setCode("PART-001");
        testPart.setWeight(0.5);
    }

    @Test
    @DisplayName("부품 상세 조회 성공 테스트")
    void getPartDetail_Success() {
        // given
        List<Long> partIds = List.of(1L);
        given(partsRepository.findAllById(partIds)).willReturn(List.of(testPart));

        // when
        List<PartsDto> response = partsService.getPartDetail(partIds);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(1L);
        assertThat(response.get(0).getName()).isEqualTo("에어필터");

        verify(partsRepository).findAllById(partIds);
    }

    @Test
    @DisplayName("부품 상세 조회 실패 테스트 - 존재하지 않는 부품")
    void getPartDetail_Fail_NotFound() {
        // given
        List<Long> partIds = List.of(999L);
        given(partsRepository.findAllById(partIds)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> partsService.getPartDetail(partIds))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("존재하지 않는 부품 ID");

        verify(partsRepository).findAllById(partIds);
    }

    @Test
    @DisplayName("발주 가능 여부 확인 성공 테스트 - 재고 충분")
    void checkStock_Success_EnoughStock() {
        // given
        OrderCheckReqDto request = new OrderCheckReqDto(1L, 50);

        given(partsRepository.findById(1L)).willReturn(Optional.of(testPart));

        // when
        OrderCheckResponseDto response = partsService.checkStock(List.of(request));

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderList()).hasSize(1);
        assertThat(response.getOrderList().get(0).getCanOrder()).isTrue();
        assertThat(response.getOrderList().get(0).getAvailableStock()).isEqualTo(100);
        assertThat(response.getTotalPrice()).isEqualTo(2500000); // 50 * 50000

        verify(partsRepository).findById(1L);
    }

    @Test
    @DisplayName("발주 가능 여부 확인 실패 테스트 - 재고 부족")
    void checkStock_Fail_InsufficientStock() {
        // given
        testPart.setAmount(30); // 재고 부족
        OrderCheckReqDto request = new OrderCheckReqDto(1L, 50); // 요청 수량이 재고보다 많음

        given(partsRepository.findById(1L)).willReturn(Optional.of(testPart));

        // when
        OrderCheckResponseDto response = partsService.checkStock(List.of(request));

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderList()).hasSize(1);
        assertThat(response.getOrderList().get(0).getCanOrder()).isFalse();
        assertThat(response.getOrderList().get(0).getAvailableStock()).isEqualTo(30);

        verify(partsRepository).findById(1L);
    }

    @Test
    @DisplayName("발주 가능 여부 확인 실패 테스트 - 잘못된 부품 ID")
    void checkStock_Fail_InvalidPartId() {
        // given
        OrderCheckReqDto request = new OrderCheckReqDto(null, 10); // null 부품 ID

        // when & then
        assertThatThrownBy(() -> partsService.checkStock(List.of(request)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("유효하지 않은 부품 ID");

        verify(partsRepository, never()).findById(any());
    }

    @Test
    @DisplayName("발주 가능 여부 확인 실패 테스트 - 잘못된 수량")
    void checkStock_Fail_InvalidAmount() {
        // given
        OrderCheckReqDto request = new OrderCheckReqDto(1L, 0); // 0 이하 수량

        // when & then
        assertThatThrownBy(() -> partsService.checkStock(List.of(request)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("요청 수량은 0보다 커야 합니다");

        verify(partsRepository, never()).findById(any());
    }

    @Test
    @DisplayName("카테고리별 재고 갯수 조회 테스트")
    void categoryAmount() {
        // given
        List<Object[]> mockData = List.of(
                new Object[]{"엔진부품", 10L},
                new Object[]{"브레이크부품", 5L}
        );

        given(partsRepository.categoryAmount()).willReturn(mockData);

        // when
        var response = partsService.categoryAmount();

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        verify(partsRepository).categoryAmount();
    }

    @Test
    @DisplayName("창고 구역별 부품 조회 테스트")
    void getLocationParts() {
        // given
        Parts part1 = new Parts();
        part1.setId(1L);
        part1.setName("부품1");
        part1.setLocation("A1-1");
        part1.setAmount(10);
        part1.setCategoryName("엔진부품");

        Parts part2 = new Parts();
        part2.setId(2L);
        part2.setName("부품2");
        part2.setLocation("A2-1");
        part2.setAmount(20);
        part2.setCategoryName("엔진부품");

        given(partsRepository.getLocationParts("A", 1)).willReturn(List.of(part1));
        given(partsRepository.getLocationParts("A", 2)).willReturn(List.of(part2));
        given(partsRepository.getLocationParts("A", 3)).willReturn(List.of());
        given(partsRepository.getLocationParts("A", 4)).willReturn(List.of());

        // when
        var response = partsService.getLocationParts("A");

        // then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(4); // 1층~4층

        verify(partsRepository).getLocationParts("A", 1);
        verify(partsRepository).getLocationParts("A", 2);
        verify(partsRepository).getLocationParts("A", 3);
        verify(partsRepository).getLocationParts("A", 4);
    }

    @Test
    @DisplayName("전체 부품 조회 실패 테스트 - 잘못된 페이지")
    void getAllParts_Fail_InvalidPage() {
        // when & then
        assertThatThrownBy(() -> partsService.getAllParts(-1, 10))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("페이지 번호나 사이즈가 유효하지 않습니다");
    }
}

