package com.stockmate.parts.api.parts.entity;

import com.stockmate.parts.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreInventory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer amount;       // 현재 수량

    @Column(name = "limit_amount")
    private Integer limitAmount;  // 최소 필요 수량(부족 판단 기준)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "part_id", nullable = false)
    private Parts part;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
