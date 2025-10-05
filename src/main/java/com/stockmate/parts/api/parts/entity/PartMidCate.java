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
@Table(name = "part_mid_cate")
public class PartMidCate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 예: 브레이크 시스템, 엔진 부품 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "big_cate_id")
    private PartBigCate bigCate; // 대분류 (선택)
}

