package com.stockmate.parts.api.health.controller;

import com.stockmate.parts.common.response.ApiResponse;
import com.stockmate.parts.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HealthCheck", description = "HealthCheck 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/parts")
@RequiredArgsConstructor
public class HealthCheckController {

    @Operation(
            summary = "Health Check API"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "서버 상태 체크 성공"),
    })
    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse<Void>> healthCheck() {

        return ApiResponse.success_only(SuccessStatus.SEND_HEALTH_CHECK_SUCCESS);
    }
}