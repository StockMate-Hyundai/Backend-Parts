package com.stockmate.parts.api.parts.entity;

import com.stockmate.parts.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "parts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parts extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long price;
    private String image;
    private String trim;
    private String model;
    private Integer category;
    private String korName;
    private String engName;
    private String categoryName;
    private Integer amount;
    private String code;
    @ElementCollection
    @CollectionTable(
            name = "part_codes",
            joinColumns = @JoinColumn(name = "part_id")
    )
    @Column(name = "code")
    private List<String> code_;
}
