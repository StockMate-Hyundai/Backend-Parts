package com.stockmate.parts.api.navigation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "네비게이션 요청 DTO")
public class NavigationRequestDTO {
    
    @Schema(description = "주문 번호 리스트", example = "[\"SMO-1\", \"SMO-2\"]")
    private List<String> orderNumbers;
}

