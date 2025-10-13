package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartsRepository extends JpaRepository<Parts, Long> {
    // 1. 전체 조회 (페이징)
    Page<Parts> findAll(Pageable pageable);

    // 2. 이름으로 검색 (대소문자 구분 없이 포함 검색)
    Page<Parts> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 3. categoryName으로 검색
    Page<Parts> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

    // 4. 이름 + categoryName 동시에 검색
    @Query("""
        select p
        from Parts p
        where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
          and (:categoryName is null or lower(p.categoryName) like lower(concat('%', :categoryName, '%')))
        order by p.name
    """)
    Page<Parts> findByNameAndCategoryName(
            @Param("name") String name,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );

    // 5. id로 검색

}
