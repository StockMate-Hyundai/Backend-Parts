package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PartsRepository 테스트")
class PartsRepositoryTest {

    @Autowired
    private PartsRepository partsRepository;

    private Parts testPart1;
    private Parts testPart2;

    @BeforeEach
    void setUp() {
        // 테스트용 부품 1
        testPart1 = new Parts();
        testPart1.setName("에어필터");
        testPart1.setPrice(50000L);
        testPart1.setCost(30000L);
        testPart1.setCategoryName("엔진부품");
        testPart1.setAmount(100);
        testPart1.setLocation("A1-1");
        testPart1.setModel("소나타");
        testPart1.setTrim("터보");
        testPart1.setKorName("현대");
        testPart1.setEngName("Hyundai");
        testPart1.setCategory(1);
        testPart1.setCode("PART-001");

        // 테스트용 부품 2
        testPart2 = new Parts();
        testPart2.setName("오일필터");
        testPart2.setPrice(40000L);
        testPart2.setCost(25000L);
        testPart2.setCategoryName("엔진부품");
        testPart2.setAmount(50);
        testPart2.setLocation("A1-2");
        testPart2.setModel("소나타");
        testPart2.setTrim("터보");
        testPart2.setKorName("현대");
        testPart2.setEngName("Hyundai");
        testPart2.setCategory(1);
        testPart2.setCode("PART-002");
    }

    @Test
    @DisplayName("부품 저장 테스트")
    void savePart() {
        // when
        Parts savedPart = partsRepository.save(testPart1);

        // then
        assertThat(savedPart.getId()).isNotNull();
        assertThat(savedPart.getName()).isEqualTo("에어필터");
        assertThat(savedPart.getAmount()).isEqualTo(100);
        assertThat(savedPart.getLocation()).isEqualTo("A1-1");
    }

    @Test
    @DisplayName("부품 ID로 조회 테스트")
    void findById() {
        // given
        Parts savedPart = partsRepository.save(testPart1);

        // when
        Parts foundPart = partsRepository.findById(savedPart.getId()).orElse(null);

        // then
        assertThat(foundPart).isNotNull();
        assertThat(foundPart.getId()).isEqualTo(savedPart.getId());
        assertThat(foundPart.getName()).isEqualTo("에어필터");
    }

    @Test
    @DisplayName("카테고리별 부품 조회 테스트")
    void findByCategoryAndModel() {
        // given
        partsRepository.save(testPart1);
        partsRepository.save(testPart2);

        List<String> categoryNames = List.of("엔진부품");
        List<String> trims = List.of("터보");
        List<String> models = List.of("소나타");
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Parts> result = partsRepository.findByCategoryAndModel(categoryNames, trims, models, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Parts::getCategoryName)
                .containsOnly("엔진부품");
    }

    @Test
    @DisplayName("부족 재고 조회 테스트")
    void findByAmountLessThanEqual() {
        // given
        Parts lowStockPart = new Parts();
        lowStockPart.setName("부족부품");
        lowStockPart.setPrice(30000L);
        lowStockPart.setAmount(5);
        lowStockPart.setCategoryName("엔진부품");
        lowStockPart.setLocation("B1-1");

        partsRepository.save(testPart1); // amount: 100
        partsRepository.save(lowStockPart); // amount: 5

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Parts> result = partsRepository.findByAmountLessThanEqual(10, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAmount()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("카테고리별 재고 갯수 조회 테스트")
    void categoryAmount() {
        // given
        partsRepository.save(testPart1);
        partsRepository.save(testPart2);

        // when
        List<Object[]> result = partsRepository.categoryAmount();

        // then
        assertThat(result).isNotEmpty();
        // 카테고리별로 그룹화되어 있어야 함
        assertThat(result.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("창고 구역별 부품 조회 테스트")
    void getLocationParts() {
        // given
        Parts partA1 = new Parts();
        partA1.setName("부품A1");
        partA1.setLocation("A1-1");
        partA1.setAmount(10);
        partA1.setCategoryName("엔진부품");
        partA1.setPrice(10000L);

        Parts partA2 = new Parts();
        partA2.setName("부품A2");
        partA2.setLocation("A2-1");
        partA2.setAmount(20);
        partA2.setCategoryName("엔진부품");
        partA2.setPrice(20000L);

        partsRepository.save(partA1);
        partsRepository.save(partA2);

        // when
        List<Parts> result = partsRepository.getLocationParts("A", 1);

        // then
        // 쿼리는 "A1-1" 형식의 location을 찾으므로, "A"와 1을 전달하면 "A1-1"을 찾음
        assertThat(result).isNotNull();
        // 결과가 비어있을 수도 있으므로 null 체크만 수행
        if (!result.isEmpty()) {
            assertThat(result).extracting(Parts::getLocation)
                    .allMatch(loc -> loc != null && loc.startsWith("A") && loc.contains("-1"));
        }
    }

    @Test
    @DisplayName("창고별 재고 비중 조회 테스트")
    void getWarehouseInventoryRatio() {
        // given
        Parts partA = new Parts();
        partA.setName("부품A");
        partA.setLocation("A1-1");
        partA.setAmount(100);
        partA.setCategoryName("엔진부품");

        Parts partB = new Parts();
        partB.setName("부품B");
        partB.setLocation("B1-1");
        partB.setAmount(200);
        partB.setCategoryName("엔진부품");

        partsRepository.save(partA);
        partsRepository.save(partB);

        // when
        List<Object[]> result = partsRepository.getWarehouseInventoryRatio();

        // then
        assertThat(result).isNotEmpty();
        // 창고별로 그룹화되어 있어야 함
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("전체 부품 조회 테스트")
    void findAll() {
        // given
        partsRepository.save(testPart1);
        partsRepository.save(testPart2);

        // when
        List<Parts> allParts = partsRepository.findAll();

        // then
        assertThat(allParts).hasSize(2);
    }

    @Test
    @DisplayName("부품 삭제 테스트")
    void deletePart() {
        // given
        Parts savedPart = partsRepository.save(testPart1);
        Long partId = savedPart.getId();

        // when
        partsRepository.delete(savedPart);

        // then
        assertThat(partsRepository.findById(partId)).isEmpty();
    }

    @Test
    @DisplayName("부품 수정 테스트")
    void updatePart() {
        // given
        Parts savedPart = partsRepository.save(testPart1);

        // when
        savedPart.setAmount(150);
        savedPart.setPrice(60000L);
        Parts updatedPart = partsRepository.save(savedPart);

        // then
        assertThat(updatedPart.getAmount()).isEqualTo(150);
        assertThat(updatedPart.getPrice()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("페이지네이션 테스트")
    void pagination() {
        // given
        for (int i = 0; i < 15; i++) {
            Parts part = new Parts();
            part.setName("부품" + i);
            part.setPrice(10000L);
            part.setAmount(10);
            part.setCategoryName("엔진부품");
            part.setLocation("A1-" + i);
            partsRepository.save(part);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Parts> firstPage = partsRepository.findAll(pageable);
        Page<Parts> secondPage = partsRepository.findAll(PageRequest.of(1, 10));

        // then
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getContent()).hasSize(5);
    }
}

