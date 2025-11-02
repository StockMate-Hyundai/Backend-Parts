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
    
    private static final int WALKING_SPEED = 1; // 1초당 1 Manhattan Distance
    
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
        int estimatedTime = totalDistance * WALKING_SPEED;
        
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
        
        log.info("최적 경로 계산 완료 - 알고리즘: {}, 총 거리: {}, 실행 시간: {}ms",
                selectedAlgorithm.getAlgorithmName(), totalDistance, endTime - startTime);
        
        return NavigationResponseDTO.builder()
                .algorithmType(selectedAlgorithm.getAlgorithmName())
                .optimizedRoute(routeSteps)
                .totalDistance(totalDistance)
                .estimatedTimeSeconds(estimatedTime)
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
        
        // 2. Position 객체로 변환
        Position start = Position.parse("문");
        Position end = Position.parse("포장대");
        
        List<Position> locations = partLocations.stream()
                .map(part -> Position.parse((String) part.get("location")))
                .collect(Collectors.toList());
        
        // 3. 모든 알고리즘 실행
        List<PathOptimizationAlgorithm> algorithms = Arrays.asList(
                nearestNeighborAlgorithm,
                twoOptAlgorithm,
                heldKarpAlgorithm,
                dijkstraBasedAlgorithm,
                branchAndBoundAlgorithm
        );
        
        Map<String, AlgorithmComparisonDTO.AlgorithmResult> results = new LinkedHashMap<>();
        
        for (PathOptimizationAlgorithm algorithm : algorithms) {
            try {
                long startTime = System.currentTimeMillis();
                List<Position> path = algorithm.findOptimalPath(start, end, locations);
                long endTime = System.currentTimeMillis();
                
                int totalDistance = calculateTotalDistance(path);
                List<String> routeString = path.stream()
                        .map(Position::toString)
                        .collect(Collectors.toList());
                
                results.put(algorithm.getAlgorithmName(), AlgorithmComparisonDTO.AlgorithmResult.builder()
                        .algorithmName(algorithm.getAlgorithmName())
                        .totalDistance(totalDistance)
                        .executionTimeMs(endTime - startTime)
                        .route(routeString)
                        .timeComplexity(algorithm.getTimeComplexity())
                        .accuracy(algorithm.getAccuracy())
                        .build());
                
                log.info("알고리즘 실행 완료 - {}: 거리={}, 시간={}ms",
                        algorithm.getAlgorithmName(), totalDistance, endTime - startTime);
                
            } catch (Exception e) {
                log.error("알고리즘 실행 실패 - {}: {}", algorithm.getAlgorithmName(), e.getMessage(), e);
                results.put(algorithm.getAlgorithmName(), AlgorithmComparisonDTO.AlgorithmResult.builder()
                        .algorithmName(algorithm.getAlgorithmName())
                        .totalDistance(-1)
                        .executionTimeMs(-1)
                        .route(Collections.emptyList())
                        .timeComplexity(algorithm.getTimeComplexity())
                        .accuracy("실패")
                        .build());
            }
        }
        
        // 4. 추천 알고리즘 결정
        String recommended = selectAlgorithm(locations.size()).getAlgorithmName();
        
        log.info("모든 알고리즘 비교 완료 - 부품 수: {}, 추천: {}", locations.size(), recommended);
        
        return AlgorithmComparisonDTO.builder()
                .results(results)
                .recommendedAlgorithm(recommended)
                .partCount(locations.size())
                .build();
    }
    
    /**
     * 부품 개수에 따라 최적 알고리즘 선택
     */
    private PathOptimizationAlgorithm selectAlgorithm(int partCount) {
        if (partCount <= 5) {
            // 소량: 정확한 해 (Held-Karp)
            return heldKarpAlgorithm;
        } else if (partCount <= 10) {
            // 중량: 정확한 해 (Branch and Bound)
            return branchAndBoundAlgorithm;
        } else if (partCount <= 15) {
            // 중대량: 준최적해 (2-opt)
            return twoOptAlgorithm;
        } else {
            // 대량: 빠른 해 (Nearest Neighbor)
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

