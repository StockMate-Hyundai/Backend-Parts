package com.stockmate.parts.api.navigation.service;

import com.stockmate.parts.api.navigation.algorithm.*;
import com.stockmate.parts.api.navigation.dto.AlgorithmComparisonDTO;
import com.stockmate.parts.api.navigation.dto.NavigationRequestDTO;
import com.stockmate.parts.api.navigation.dto.NavigationResponseDTO;
import com.stockmate.parts.api.navigation.model.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationService {
    
    private final WebClient.Builder webClientBuilder;
    
    private final NearestNeighborAlgorithm nearestNeighborAlgorithm;
    private final TwoOptAlgorithm twoOptAlgorithm;
    private final HeldKarpAlgorithm heldKarpAlgorithm;
    private final DijkstraBasedAlgorithm dijkstraBasedAlgorithm;
    private final BranchAndBoundAlgorithm branchAndBoundAlgorithm;
    
    @Value("${order.server.url}")
    private String orderServerUrl;
    
    // 시간 예측 설정 (application.yml에서 주입)
    @Value("${navigation.time.seconds-per-unit:1.5}")
    private double secondsPerUnitDistance; // 1 Manhattan Distance당 걷기 시간 (초)
    
    @Value("${navigation.time.picking-per-part:8}")
    private int pickingTimePerPart; // 부품 1개당 피킹 시간 (초)
    
    @Value("${navigation.time.buffer:10}")
    private int startEndBufferTime; // 시작/종료 버퍼 시간 (초)
    
    /**
     * 최적 경로 계산 (부품 개수에 따라 자동으로 알고리즘 선택)
     */
    public NavigationResponseDTO calculateOptimalRoute(NavigationRequestDTO requestDTO) {
        log.info("최적 경로 계산 시작 - 주문 번호 수: {}", requestDTO.getOrderNumbers().size());
        
        // 1. Order 서버로부터 부품 위치 정보 가져오기
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("orderNumbers", requestDTO.getOrderNumbers());
        
        Map<String, Object> orderResponse = webClientBuilder.build()
                .post()
                .uri(orderServerUrl + "/api/v1/order/navigation/parts")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        
        if (orderResponse == null || !orderResponse.containsKey("data")) {
            throw new RuntimeException("Order 서버로부터 부품 정보를 가져오지 못했습니다.");
        }
        
        Map<String, Object> data = (Map<String, Object>) orderResponse.get("data");
        List<Map<String, Object>> partLocations = (List<Map<String, Object>>) data.get("partLocations");
        
        if (partLocations == null || partLocations.isEmpty()) {
            throw new RuntimeException("해당 주문에 부품이 없습니다.");
        }
        
        log.info("Order 서버로부터 부품 위치 정보 가져오기 완료 - 부품 수: {}", partLocations.size());
        
        // 2. Position 객체로 변환
        Position start = Position.parse("문");
        Position end = Position.parse("포장대");
        
        List<PartLocationWithInfo> partInfoList = partLocations.stream()
                .map(part -> new PartLocationWithInfo(
                        Position.parse((String) part.get("location")),
                        (String) part.get("partName"),
                        (String) part.get("orderNumber"),
                        ((Number) part.get("partId")).longValue()
                ))
                .collect(Collectors.toList());
        
        List<Position> locations = partInfoList.stream()
                .map(PartLocationWithInfo::getPosition)
                .collect(Collectors.toList());
        
        // 3. 부품 개수에 따라 최적 알고리즘 선택
        PathOptimizationAlgorithm selectedAlgorithm = selectAlgorithm(locations.size());
        log.info("선택된 알고리즘: {} (부품 개수: {})", selectedAlgorithm.getAlgorithmName(), locations.size());
        
        // 4. 최적 경로 계산
        long startTime = System.currentTimeMillis();
        List<Position> optimalPath = selectedAlgorithm.findOptimalPath(start, end, locations);
        long endTime = System.currentTimeMillis();
        
        // 5. 응답 DTO 생성
        int totalDistance = calculateTotalDistance(optimalPath);
        
        // 예상 시간 계산 (application.yml 설정 기반)
        // = (이동 거리 × 걷기시간) + (부품 개수 × 피킹시간) + (버퍼시간)
        int walkingTime = (int) (totalDistance * secondsPerUnitDistance);
        int pickingTime = locations.size() * pickingTimePerPart;
        int estimatedTime = walkingTime + pickingTime + startEndBufferTime;
        
        List<NavigationResponseDTO.RouteStep> routeSteps = new ArrayList<>();
        int cumulativeDistance = 0;
        
        for (int i = 0; i < optimalPath.size(); i++) {
            Position pos = optimalPath.get(i);
            int distanceFromPrevious = 0;
            
            if (i > 0) {
                distanceFromPrevious = optimalPath.get(i - 1).manhattanDistance(pos);
                cumulativeDistance += distanceFromPrevious;
            }
            
            // 부품 정보 찾기
            String description = null;
            String orderNumber = null;
            Long partId = null;
            
            if (!pos.isStart() && !pos.isEnd()) {
                for (PartLocationWithInfo info : partInfoList) {
                    if (info.getPosition().equals(pos)) {
                        description = info.getPartName();
                        orderNumber = info.getOrderNumber();
                        partId = info.getPartId();
                        break;
                    }
                }
            } else if (pos.isStart()) {
                description = "시작점";
            } else if (pos.isEnd()) {
                description = "종료점";
            }
            
            routeSteps.add(NavigationResponseDTO.RouteStep.builder()
                    .location(pos.toString())
                    .description(description)
                    .sequence(i)
                    .cumulativeDistance(cumulativeDistance)
                    .distanceFromPrevious(distanceFromPrevious)
                    .orderNumber(orderNumber)
                    .partId(partId)
                    .build());
        }
        
        log.info("최적 경로 계산 완료 - 알고리즘: {}, 총 거리: {}, 걷기: {}초, 피킹: {}초, 버퍼: {}초, 총 시간: {}초, 실행 시간: {}ms",
                selectedAlgorithm.getAlgorithmName(), totalDistance, walkingTime, pickingTime, startEndBufferTime, estimatedTime, endTime - startTime);
        
        return NavigationResponseDTO.builder()
                .algorithmType(selectedAlgorithm.getAlgorithmName())
                .optimizedRoute(routeSteps)
                .totalDistance(totalDistance)
                .estimatedTimeSeconds(estimatedTime)
                .walkingTimeSeconds(walkingTime)
                .pickingTimeSeconds(pickingTime)
                .bufferTimeSeconds(startEndBufferTime)
                .executionTimeMs(endTime - startTime)
                .build();
    }
    
    /**
     * 모든 알고리즘 비교 (성능 분석용)
     */
    public AlgorithmComparisonDTO compareAllAlgorithms(NavigationRequestDTO requestDTO) {
        log.info("모든 알고리즘 비교 시작 - 주문 번호 수: {}", requestDTO.getOrderNumbers().size());
        
        // 1. Order 서버로부터 부품 위치 정보 가져오기
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("orderNumbers", requestDTO.getOrderNumbers());
        
        Map<String, Object> orderResponse = webClientBuilder.build()
                .post()
                .uri(orderServerUrl + "/api/v1/order/navigation/parts")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        
        if (orderResponse == null || !orderResponse.containsKey("data")) {
            throw new RuntimeException("Order 서버로부터 부품 정보를 가져오지 못했습니다.");
        }
        
        Map<String, Object> data = (Map<String, Object>) orderResponse.get("data");
        List<Map<String, Object>> partLocations = (List<Map<String, Object>>) data.get("partLocations");
        
        if (partLocations == null || partLocations.isEmpty()) {
            throw new RuntimeException("해당 주문에 부품이 없습니다.");
        }
        
        // 2. Position 객체로 변환 (중복 제거)
        Position start = Position.parse("문");
        Position end = Position.parse("포장대");
        
        // 중복된 위치 제거 (같은 위치에 여러 부품이 있을 수 있음)
        Map<String, Position> uniqueLocationsMap = new LinkedHashMap<>();
        for (Map<String, Object> part : partLocations) {
            String locationString = (String) part.get("location");
            if (!uniqueLocationsMap.containsKey(locationString)) {
                uniqueLocationsMap.put(locationString, Position.parse(locationString));
            }
        }
        List<Position> locations = new ArrayList<>(uniqueLocationsMap.values());
        
        log.info("중복 제거 완료 - 전체 부품: {}개, 고유 위치: {}개", partLocations.size(), locations.size());
        
        // 3. 모든 알고리즘 실행 (1차: 거리만 계산)
        List<PathOptimizationAlgorithm> algorithms = Arrays.asList(
                nearestNeighborAlgorithm,
                twoOptAlgorithm,
                heldKarpAlgorithm,
                dijkstraBasedAlgorithm,
                branchAndBoundAlgorithm
        );
        
        Map<String, AlgorithmComparisonDTO.AlgorithmResult> tempResults = new LinkedHashMap<>();
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;
        
        for (PathOptimizationAlgorithm algorithm : algorithms) {
            try {
                long startTime = System.currentTimeMillis();
                List<Position> path = algorithm.findOptimalPath(start, end, locations);
                long endTime = System.currentTimeMillis();
                
                int totalDistance = calculateTotalDistance(path);
                List<String> routeString = path.stream()
                        .map(Position::toString)
                        .collect(Collectors.toList());
                
                // 최소/최대 거리 업데이트
                if (totalDistance > 0) {
                    minDistance = Math.min(minDistance, totalDistance);
                    maxDistance = Math.max(maxDistance, totalDistance);
                }
                
                tempResults.put(algorithm.getAlgorithmName(), AlgorithmComparisonDTO.AlgorithmResult.builder()
                        .algorithmName(algorithm.getAlgorithmName())
                        .totalDistance(totalDistance)
                        .executionTimeMs(endTime - startTime)
                        .route(routeString)
                        .timeComplexity(algorithm.getTimeComplexity())
                        .theoreticalAccuracy(algorithm.getAccuracy())
                        .actualAccuracy(0.0) // 나중에 계산
                        .isOptimal(false) // 나중에 판단
                        .build());
                
                log.info("알고리즘 실행 완료 - {}: 거리={}, 시간={}ms",
                        algorithm.getAlgorithmName(), totalDistance, endTime - startTime);
                
            } catch (Exception e) {
                log.error("알고리즘 실행 실패 - {}: {}", algorithm.getAlgorithmName(), e.getMessage(), e);
                tempResults.put(algorithm.getAlgorithmName(), AlgorithmComparisonDTO.AlgorithmResult.builder()
                        .algorithmName(algorithm.getAlgorithmName())
                        .totalDistance(-1)
                        .executionTimeMs(-1)
                        .route(Collections.emptyList())
                        .timeComplexity(algorithm.getTimeComplexity())
                        .theoreticalAccuracy("실패")
                        .actualAccuracy(0.0)
                        .isOptimal(false)
                        .build());
            }
        }
        
        // 4. 실제 정확도 계산 (2차: 최적 거리 기준으로 정확도 계산)
        Map<String, AlgorithmComparisonDTO.AlgorithmResult> results = new LinkedHashMap<>();
        final int optimalDistance = minDistance;
        
        for (Map.Entry<String, AlgorithmComparisonDTO.AlgorithmResult> entry : tempResults.entrySet()) {
            AlgorithmComparisonDTO.AlgorithmResult result = entry.getValue();
            
            if (result.getTotalDistance() > 0) {
                // 실제 정확도 = (최적 거리 / 현재 거리) * 100
                double actualAccuracy = ((double) optimalDistance / result.getTotalDistance()) * 100.0;
                boolean isOptimal = (result.getTotalDistance() == optimalDistance);
                
                results.put(entry.getKey(), AlgorithmComparisonDTO.AlgorithmResult.builder()
                        .algorithmName(result.getAlgorithmName())
                        .totalDistance(result.getTotalDistance())
                        .executionTimeMs(result.getExecutionTimeMs())
                        .route(result.getRoute())
                        .timeComplexity(result.getTimeComplexity())
                        .theoreticalAccuracy(result.getTheoreticalAccuracy())
                        .actualAccuracy(Math.round(actualAccuracy * 100.0) / 100.0) // 소수점 2자리
                        .isOptimal(isOptimal)
                        .build());
            } else {
                results.put(entry.getKey(), result);
            }
        }
        
        // 5. 추천 알고리즘 결정
        String recommended = selectAlgorithm(locations.size()).getAlgorithmName();
        
        log.info("모든 알고리즘 비교 완료 - 부품 수: {}, 추천: {}, 최적 거리: {}, 최악 거리: {}", 
                locations.size(), recommended, minDistance, maxDistance);
        
        return AlgorithmComparisonDTO.builder()
                .results(results)
                .recommendedAlgorithm(recommended)
                .partCount(locations.size())
                .optimalDistance(minDistance)
                .worstDistance(maxDistance)
                .build();
    }
    
    /**
     * 부품 개수에 따라 최적 알고리즘 선택
     * 
     * 선택 기준:
     * - 1~8개: Held-Karp (DP) - 빠르고 100% 최적
     * - 9~15개: Branch and Bound - 가지치기로 최적해 보장
     * - 16~30개: 2-opt - 실전에서 거의 최적해, 빠름 (수십 ms 이내)
     * - 31개 이상: Nearest Neighbor - 대량 처리, 속도 우선
     */
    private PathOptimizationAlgorithm selectAlgorithm(int partCount) {
        if (partCount <= 8) {
            // 소량: 정확한 해 (Held-Karp DP)
            return heldKarpAlgorithm;
        } else if (partCount <= 15) {
            // 중량: 정확한 해 (Branch and Bound, 가지치기)
            return branchAndBoundAlgorithm;
        } else if (partCount <= 30) {
            // 중대량: 준최적해 (2-opt, 85~100% 정확, 빠름)
            return twoOptAlgorithm;
        } else {
            // 대량: 빠른 해 (Nearest Neighbor, 60~80% 정확, 즉각 응답)
            return nearestNeighborAlgorithm;
        }
    }
    
    /**
     * 경로의 총 거리 계산
     */
    private int calculateTotalDistance(List<Position> path) {
        int distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += path.get(i).manhattanDistance(path.get(i + 1));
        }
        return distance;
    }
    
    /**
     * 부품 위치와 정보를 함께 저장하는 내부 클래스
     */
    private static class PartLocationWithInfo {
        private final Position position;
        private final String partName;
        private final String orderNumber;
        private final Long partId;
        
        public PartLocationWithInfo(Position position, String partName, String orderNumber, Long partId) {
            this.position = position;
            this.partName = partName;
            this.orderNumber = orderNumber;
            this.partId = partId;
        }
        
        public Position getPosition() {
            return position;
        }
        
        public String getPartName() {
            return partName;
        }
        
        public String getOrderNumber() {
            return orderNumber;
        }
        
        public Long getPartId() {
            return partId;
        }
    }
}

