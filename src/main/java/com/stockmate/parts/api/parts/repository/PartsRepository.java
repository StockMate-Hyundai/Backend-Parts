package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartsRepository extends JpaRepository<Parts, Long> {
    // categoryName + model로 검색, 없으면 전체 검색
    @Query("""
    select p
    from Parts p
    where
        (:categoryNames is null or p.categoryName in :categoryNames)
        and (:trims is null or p.trim in :trims)
        and (:models is null or p.model in :models)
    """)
    Page<Parts> findByCategoryAndModel(
            @Param("categoryNames") List<String> categoryNames,
            @Param("trims") List<String> trims,
            @Param("models") List<String> models,
            Pageable pageable
    );

    // 부족 재고 조회
    Page<Parts> findByAmountLessThanEqual(Integer amount, Pageable pageable);

    // 카테고리별 재고 갯수
    @Query("""
    select p.categoryName, count(p)
    from Parts p
    group by p.categoryName
    """)
    List<Object[]> categoryAmount();

    // 창고 구역별 부품 조회
    @Query("""
    select p
    from Parts p
    where p.location like concat(:location, '-', :floor)
    """)
    List<Parts> getLocationParts(
            @Param("location") String location,
            @Param("floor") Integer floor
    );
}
