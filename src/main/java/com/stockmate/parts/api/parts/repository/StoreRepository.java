package com.stockmate.parts.api.parts.repository;

import com.stockmate.parts.api.parts.dto.store.StorePartsDto;
import com.stockmate.parts.api.parts.entity.StoreInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreInventory, Long> {

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

    // 본사 특정 부품 총 수량
//    @Query("""
//        select sum(si.amount)
//        from StoreInventory si
//        where si.part.id = :partId and si.userId = :userId
//        """)
//    Long sumAmountByPartAndStore(@Param("partId") Long partId,
//                                 @Param("userId") Long userId);
//
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
//
//
//    // 발주 가능 여부
//    @Query("select p.amount from Parts p where p.id = :id")
//    Integer findAmountByPartId(@Param("id") Long id);

//    @Query("""
//      select new com.stockmate.parts.api.parts.dto.CategorySumProjection(
//        bc.id,
//        bc.name,
//        sum(si.amount)
//      )
//      from StoreInventory si
//        join si.part p
//        left join p.midCate mc
//        left join mc.bigCate bc
//      where (:userId is null or si.userId = :userId)
//      group by bc.id, bc.name
//      order by sum(si.amount) desc
//    """)
//    List<CategorySumProjection> sumByBigCategory(@Param("userId") Long userId);
}

