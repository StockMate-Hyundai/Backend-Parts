package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartsRepository extends JpaRepository<Parts, Long> {
    // 전체 조회
    Page<Parts> findAll(Pageable pageable);

    // 이름으로 검색
    Page<Parts> findByNameContainingIgnoreCase(String name, Pageable pageable);

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

    // id로 검색
    Optional<Parts> findById(@Param("id") Long id);

    // 부족 재고 조회
    Page<Parts> findByAmountLessThanEqual(Integer amount, Pageable pageable);
}
