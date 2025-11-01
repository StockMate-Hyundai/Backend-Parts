package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.entity.Parts;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreInventory, Long> {
    // 본사 -> 지점 부품 검색
    @Query("""
        select p, si
        from StoreInventory si
        join si.part p
        where si.userId = :storeId
    """)
    Page<Object[]> findByUserId(Long storeId, Pageable pageable);

    // 지점 부품 검색
    @Query("""
        select p, si, CASE WHEN si.amount < si.limitAmount Then true ELSE false END
        from StoreInventory si
        join si.part p
        where si.userId = :userId
            and (:categoryNames is null or p.categoryName in :categoryNames)
            and (:trims is null or p.trim in :trims)
            and (:models is null or p.model in :models)
    """)
    Page<Object[]> searchParts(
            Long userId,
            List<String> categoryNames,
            List<String> trims,
            List<String> models,
            Pageable pageable
    );

    // 카테고리별 부족 재고 조회
    @Query("""
        select p, si, CASE WHEN si.amount < si.limitAmount Then true ELSE false END
        from StoreInventory si
        join si.part p
        where si.userId = :userId
            and si.amount < si.limitAmount
            and (:categoryName is null or :categoryName = '' or p.categoryName = :categoryName)
    """)
    Page<Object[]> findUnderLimitByCategory(
            Long userId,
            String categoryName,
            Pageable pageable
    );

    // 카테고리별 부족 부품 갯수
    @Query("""
        select p.categoryName, count(si)
        from StoreInventory si
        join si.part p
        where si.userId = :userId
            and(si.amount < si.limitAmount)
        group by p.categoryName
    """)
    List<Object[]> countLackPartsByCategory(Long userId);

    // 부품명으로 검색
    @Query("""
        select p, si, CASE WHEN si.amount < si.limitAmount Then true ELSE false END
        from StoreInventory si
        join si.part p
        where si.userId = :userId
            and (:name is null or :name = '' or p.korName like concat('%', :name, '%'))
    """)
    Page<Object[]> findByName(
            Long userId,
            String name,
            Pageable pageable
    );

    // 최소 필요 수량 변경
    Optional<StoreInventory> findStoreInventoryByUserIdAndPartId(Long userId, Long partId);

    // 가맹점 ID와 부품 코드로 재고 조회
    @Query("""
        SELECT si
        FROM StoreInventory si
        JOIN si.part p
        WHERE si.userId = :userId AND p.code = :partCode
    """)
    Optional<StoreInventory> findByUserIdAndPartCode(Long userId, String partCode);

    // 부품 ID로 가맹점별 재고 조회 (페이지네이션)
    @Query("""
        SELECT si
        FROM StoreInventory si
        WHERE si.part.id = :partId
        ORDER BY si.userId ASC
    """)
    Page<StoreInventory> findByPartId(@org.springframework.data.repository.query.Param("partId") Long partId, Pageable pageable);

//    // 특정 부품이 부족재고인 지점 개수
//    @Query("""
//        select count(si) from StoreInventory si
//        where si.part.id = :partId and si.limitAmount is not null and si.amount < si.limitAmount
//        """)
//    long countStoresUnderLimit(@Param("partId") Long partId);
//
//    // 전사 재고 분석 (부품별 총 수량 + 부족재고 지점 수 집계)
//    @Query(
//            value = """
//    select new com.stockmate.parts.api.parts.dto.AnalysisRowDto(
//        p.id,
//        p.name,
//        p.price,
//        sum(si.amount),
//        sum(case when si.limitAmount is not null and si.amount < si.limitAmount then 1 else 0 end)
//    )
//    from StoreInventory si
//    join si.part p
//    where (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
//    group by p.id, p.name, p.price, p.createdAt
//    order by p.createdAt desc
//  """,
//            countQuery = """
//    select count(distinct p.id)
//    from StoreInventory si
//    join si.part p
//    where (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
//  """
//    )
//    Page<AnalysisRowDto> analyzeAll(Pageable pageable,
//                                    @Param("q") String q);
}

