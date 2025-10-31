package com.stockmate.parts.api.parts.dto.parts;

import com.stockmate.parts.api.parts.entity.Parts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class LocationResponseDto {
    private Integer floor;
    private List<PartsDto> parts;
}
