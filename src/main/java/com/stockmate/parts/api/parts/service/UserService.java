package com.stockmate.parts.api.parts.service;

import com.stockmate.parts.api.parts.dto.parts.UserBatchApiResponse;
import com.stockmate.parts.api.parts.dto.parts.UserBatchRequestDTO;
import com.stockmate.parts.api.parts.dto.parts.UserBatchResponseDTO;
import com.stockmate.parts.common.exception.InternalServerException;
import com.stockmate.parts.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient webClient;

    @Value("${user.server.url}")
    private String userServerUrl;

    public Map<Long, UserBatchResponseDTO> getUsersByMemberIds(List<Long> memberIds) {
        log.info("사용자 정보 일괄 조회 요청 - Member IDs 수: {}", memberIds.size());

        if (memberIds == null || memberIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            UserBatchRequestDTO requestDTO = UserBatchRequestDTO.builder()
                    .memberIds(memberIds)
                    .build();

            UserBatchApiResponse response = webClient.post()
                    .uri(userServerUrl + "/api/v1/user/batch")
                    .bodyValue(requestDTO)
                    .retrieve()
                    .bodyToMono(UserBatchApiResponse.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        if (ex.getStatusCode().is4xxClientError()) {
                            log.warn("사용자 정보 조회 클라이언트 에러 - Status: {}, Response: {}",
                                    ex.getStatusCode(), ex.getResponseBodyAsString());
                            return Mono.error(ex);
                        }
                        log.error("사용자 정보 조회 서버 에러 - Status: {}, Response: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new InternalServerException(ErrorStatus.NOT_CONNECTTION_USER_DETAIL_EXCEPTION.getMessage()));
                    })
                    .block();

            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.error("사용자 정보 조회 응답 실패");
                throw new InternalServerException(ErrorStatus.RESPONSE_DATA_NOT_MATCH_EXCEPTION.getMessage());
            }

            // List를 Map으로 변환 (memberId를 key로)
            Map<Long, UserBatchResponseDTO> userMap = new HashMap<>();
            for (UserBatchResponseDTO user : response.getData()) {
                userMap.put(user.getMemberId(), user);
            }

            log.info("사용자 정보 일괄 조회 완료 - 조회된 사용자 수: {}", userMap.size());
            return userMap;

        } catch (InternalServerException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("사용자 정보 조회 중 WebClient 예외 - Error: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorStatus.NOT_CONNECTTION_USER_DETAIL_EXCEPTION.getMessage());
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 예상치 못한 오류 - Error: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorStatus.CHECK_USER_DETAIL_EXCEPTION.getMessage());
        }
    }
}

