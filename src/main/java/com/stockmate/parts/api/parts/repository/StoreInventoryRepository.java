package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.dto.AnalysisRowDto;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    // 본사 특정 부품 총 수량
    @Query("""
        select sum(si.amount) from StoreInventory si
        where si.part.id = :partId and si.userId = :userId
        """)
    Long sumAmountByPartAndStore(@Param("partId") Long partId,
                                 @Param("userId") Long userId);

    // 특정 부품이 부족재고인 지점 개수
    @Query("""
        select count(si) from StoreInventory si
        where si.part.id = :partId and si.limitAmount is not null and si.amount < si.limitAmount
        """)
    long countStoresUnderLimit(@Param("partId") Long partId);

    // 전사 재고 분석 (부품별 총 수량 + 부족재고 지점 수 집계)
    @Query(
            value = """
    select new com.stockmate.parts.api.parts.dto.AnalysisRowDto(
        p.id,
        p.name,
        p.price,
        sum(si.amount),
        sum(case when si.limitAmount is not null and si.amount < si.limitAmount then 1 else 0 end)
    )
    from StoreInventory si
    join si.part p
    where (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
    group by p.id, p.name, p.price, p.createdAt
    order by p.createdAt desc
  """,
            countQuery = """
    select count(distinct p.id)
    from StoreInventory si
    join si.part p
    where (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
  """
    )
    Page<AnalysisRowDto> analyzeAll(Pageable pageable,
                                    @Param("q") String q);

    // 재고 조회
    @EntityGraph(attributePaths = {"part", "part.midCate"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("""
    select si
    from StoreInventory si
      join si.part p
      left join p.midCate mc
    where si.userId = :userId
      and (:categoryId is null or mc.id = :categoryId)
    order by si.updatedAt desc
    """)
    Page<StoreInventory> findByUserAndCategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 재고 검색
    @Query("""
    select si
    from StoreInventory si
    join fetch si.part p
    left join fetch p.midCate mc
    where si.userId = :userId
      and lower(p.name) like lower(concat('%', :keyword, '%'))
      and (:categoryId is null or mc.id = :categoryId)
    order by si.updatedAt desc
    """)
    Page<StoreInventory> findByUserIdAndPart_NameContainingIgnoreCaseAndCategory(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // 부족 재고
    @EntityGraph(attributePaths = {"part"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("""
    select si
    from StoreInventory si
    where si.userId = :userId
      and si.limitAmount is not null
      and si.amount < si.limitAmount
    order by si.updatedAt desc
    """)
    Page<StoreInventory> findUnderLimitByUser(@Param("userId") Long userId, Pageable pageable);

}

