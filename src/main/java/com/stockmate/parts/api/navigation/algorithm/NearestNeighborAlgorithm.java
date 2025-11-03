package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Greedy Algorithm: Nearest Neighbor (최근접 이웃)
 * 시간복잡도: O(n²)
 * 정확도: 60~80%
 */
@Slf4j
@Component
public class NearestNeighborAlgorithm implements PathOptimizationAlgorithm {
    
    @Override
    public List<Position> findOptimalPath(Position start, Position end, List<Position> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of(start, end);
        }
        
        // 디버그: 첫 3개 위치 로깅
        log.info("NN 시작 - 입력 위치 개수: {}, 처음 3개: {}", 
                locations.size(),
                locations.stream().limit(3).map(Position::toString).toList());
        
        // 1단계: 기본 Nearest Neighbor 경로 생성
        List<Position> path = new ArrayList<>();
        path.add(start);
        
        // LinkedHashSet: 입력 순서 보장 (일관된 결과를 위해)
        Set<Position> unvisited = new LinkedHashSet<>(locations);
        Position current = start;
        
        // 가장 가까운 미방문 위치를 반복적으로 선택
        while (!unvisited.isEmpty()) {
            Position nearest = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (Position candidate : unvisited) {
                int distance = current.manhattanDistance(candidate);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                }
            }
            
            if (nearest != null) {
                path.add(nearest);
                unvisited.remove(nearest);
                current = nearest;
            }
        }
        
        path.add(end);
        
        int initialDistance = calculateTotalDistance(path);
        log.info("NN 기본 경로 완료 - 총 거리: {}", initialDistance);
        
        // 2단계: 경로 상 중간 피킹 최적화 (On-the-way optimization)
        path = optimizeWithOnTheWayPicking(path);
        
        int finalDistance = calculateTotalDistance(path);
        log.info("NN 최적화 완료 - 초기: {}칸, 최종: {}칸, 개선: {}칸", 
                initialDistance, finalDistance, initialDistance - finalDistance);
        
        return path;
    }
    
    /**
     * 경로 상 중간 피킹 최적화
     * A → B로 가는 경로 상에 C가 있으면, A → C → B로 변경
     */
    private List<Position> optimizeWithOnTheWayPicking(List<Position> originalPath) {
        List<Position> optimizedPath = new ArrayList<>(originalPath);
        boolean improved = true;
        int iteration = 0;
        int maxIterations = 10; // 무한 루프 방지
        
        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;
            
            // 각 구간(i → i+1)을 체크
            for (int i = 0; i < optimizedPath.size() - 1; i++) {
                Position from = optimizedPath.get(i);
                Position to = optimizedPath.get(i + 1);
                
                // 나머지 경로에서 "경로 상에 있는" 노드 찾기
                for (int j = i + 2; j < optimizedPath.size() - 1; j++) {
                    Position candidate = optimizedPath.get(j);
                    
                    if (isOnTheWay(from, to, candidate)) {
                        // candidate를 i+1 위치로 이동
                        optimizedPath.remove(j);
                        optimizedPath.add(i + 1, candidate);
                        
                        log.debug("경로 최적화: {} → {} 사이에 {} 삽입", 
                                from.getOriginalLocation(), 
                                to.getOriginalLocation(), 
                                candidate.getOriginalLocation());
                        
                        improved = true;
                        break; // 한 번에 하나씩 최적화
                    }
                }
                
                if (improved) break; // 외부 루프도 중단
            }
        }
        
        return optimizedPath;
    }
    
    /**
     * A → B로 가는 경로 상에 C가 있는지 체크
     * 
     * 판단 기준:
     * - A → C → B의 거리가 A → B의 직행 거리보다 3칸 이내로 길면 "경로 상"으로 판단
     * 
     * @param from 출발지
     * @param to 목적지
     * @param candidate 중간 후보 위치
     * @return 경로 상에 있으면 true
     */
    private boolean isOnTheWay(Position from, Position to, Position candidate) {
        int directDistance = from.manhattanDistance(to);
        int detourDistance = from.manhattanDistance(candidate) + candidate.manhattanDistance(to);
        
        // detour가 직행보다 3칸 이내로 길면 "경로 상"으로 판단
        boolean onTheWay = (detourDistance - directDistance) <= 3;
        
        if (onTheWay) {
            log.debug("경로 상 중간 노드 발견: {} → {} → {} (직행: {}칸, 경유: {}칸, 차이: {}칸)", 
                    from.getOriginalLocation(), 
                    candidate.getOriginalLocation(), 
                    to.getOriginalLocation(),
                    directDistance, 
                    detourDistance, 
                    detourDistance - directDistance);
        }
        
        return onTheWay;
    }
    
    /**
     * 경로의 총 거리 계산 (디버그용)
     */
    private int calculateTotalDistance(List<Position> path) {
        int distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += path.get(i).manhattanDistance(path.get(i + 1));
        }
        return distance;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Nearest Neighbor (Greedy)";
    }
    
    @Override
    public String getTimeComplexity() {
        return "O(n²)";
    }
    
    @Override
    public String getAccuracy() {
        return "60~80%";
    }
    
    @Override
    public String getDescription() {
        return "현재 위치에서 가장 가까운 미방문 위치를 선택하는 그리디 알고리즘";
    }
}

