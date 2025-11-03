package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metaheuristic Algorithm: 2-opt (Local Search)
 * 시간복잡도: O(n³)
 * 정확도: 85~95%
 * 
 * Nearest Neighbor 결과를 2-opt로 개선
 */
@Slf4j
@Component
public class TwoOptAlgorithm implements PathOptimizationAlgorithm {
    
    private final NearestNeighborAlgorithm nearestNeighborAlgorithm;
    
    public TwoOptAlgorithm(NearestNeighborAlgorithm nearestNeighborAlgorithm) {
        this.nearestNeighborAlgorithm = nearestNeighborAlgorithm;
    }
    
    @Override
    public List<Position> findOptimalPath(Position start, Position end, List<Position> locations) {
        // 1. Nearest Neighbor로 초기 경로 생성
        List<Position> path = new ArrayList<>(nearestNeighborAlgorithm.findOptimalPath(start, end, locations));
        
        if (path.size() <= 3) {
            // 경로가 너무 짧으면 개선할 것이 없음
            return path;
        }
        
        int initialDistance = calculateTotalDistance(path);
        log.info("2-opt 시작 - 초기 거리: {}", initialDistance);
        
        // 2. 2-opt 개선 (시작점과 종료점은 고정)
        boolean improved = true;
        int maxIterations = 1000; // 무한 루프 방지
        int iteration = 0;
        
        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;
            
            // 시작점(0)과 종료점(n-1)은 고정, 중간 부분만 최적화
            for (int i = 1; i < path.size() - 2; i++) {
                for (int j = i + 1; j < path.size() - 1; j++) {
                    // 2-opt: [i, j] 구간을 뒤집었을 때 거리 변화 계산
                    // 원본: ... → path[i-1] → path[i] ... path[j] → path[j+1] → ...
                    // Swap: ... → path[i-1] → path[j] ... path[i] → path[j+1] → ...
                    
                    // 제거되는 간선
                    int removedDistance = path.get(i - 1).manhattanDistance(path.get(i))
                                        + path.get(j).manhattanDistance(path.get(j + 1));
                    
                    // 추가되는 간선
                    int addedDistance = path.get(i - 1).manhattanDistance(path.get(j))
                                      + path.get(i).manhattanDistance(path.get(j + 1));
                    
                    // 개선되었으면 swap 적용
                    if (addedDistance < removedDistance) {
                        path = twoOptSwap(path, i, j);
                        improved = true;
                        log.debug("2-opt 개선: 구간[{}, {}] swap, 거리 감소: {} → {}", 
                                i, j, removedDistance, addedDistance);
                    }
                }
            }
        }
        
        int twoOptDistance = calculateTotalDistance(path);
        log.info("2-opt 완료 - 초기: {}칸, 2-opt 후: {}칸, 개선: {}칸, 반복: {}회", 
                initialDistance, twoOptDistance, initialDistance - twoOptDistance, iteration);
        
        // 3. 2-opt 후 다시 On-the-way Picking 최적화
        path = optimizeWithOnTheWayPicking(path);
        
        int finalDistance = calculateTotalDistance(path);
        log.info("최종 최적화 완료 - 2-opt: {}칸, 최종: {}칸, 추가 개선: {}칸", 
                twoOptDistance, finalDistance, twoOptDistance - finalDistance);
        
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
     * 2-opt swap: 경로의 [i, j] 구간을 역순으로 뒤집음
     */
    private List<Position> twoOptSwap(List<Position> path, int i, int j) {
        List<Position> newPath = new ArrayList<>(path.subList(0, i));
        List<Position> reversed = new ArrayList<>(path.subList(i, j + 1));
        Collections.reverse(reversed);
        newPath.addAll(reversed);
        newPath.addAll(path.subList(j + 1, path.size()));
        return newPath;
    }
    
    /**
     * 전체 경로의 총 거리 계산 (디버깅용)
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
        return "Nearest Neighbor + 2-opt (Metaheuristic)";
    }
    
    @Override
    public String getTimeComplexity() {
        return "O(n³)";
    }
    
    @Override
    public String getAccuracy() {
        return "85~95%";
    }
    
    @Override
    public String getDescription() {
        return "Nearest Neighbor 결과를 2-opt Local Search로 개선하는 메타휴리스틱 알고리즘";
    }
}

