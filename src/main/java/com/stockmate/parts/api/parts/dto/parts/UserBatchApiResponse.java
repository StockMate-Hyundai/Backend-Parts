package com.stockmate.parts.api.parts.dto.parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBatchApiResponse {
    private int status;
    private boolean success;
    private String message;
    private List<UserBatchResponseDTO> data;
}

