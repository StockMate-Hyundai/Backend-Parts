package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        where (:categoryName is null or :categoryName = '' or lower(p.categoryName) like lower(concat('%', :categoryName, '%')))
          and (:trim is null or :trim = '' or :trim = p.trim)
          and (:model is null or :model = '' or lower(p.model) like lower(concat('%', :model, '%')))
    """)
    Page<Parts> findByCategoryAndModel(
            @Param("categoryName") String categoryName,
            @Param("trim") String trim,
            @Param("model") String model,
            Pageable pageable
    );

    // id로 검색
    Optional<Parts> findById(@Param("id") Long id);

    // 부족 재고 조회
    Page<Parts> findByAmountLessThanEqual(Integer amount, Pageable pageable);
}
