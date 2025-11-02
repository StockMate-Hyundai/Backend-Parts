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
                    // 현재 거리
                    int currentDistance = calculateSegmentDistance(path, i, j);
                    
                    // 2-opt swap 후 거리
                    List<Position> swappedPath = twoOptSwap(path, i, j);
                    int swappedDistance = calculateSegmentDistance(swappedPath, i, j);
                    
                    // 개선되었으면 적용
                    if (swappedDistance < currentDistance) {
                        path = swappedPath;
                        improved = true;
                    }
                }
            }
        }
        
        log.debug("2-opt 개선 완료 - 반복 횟수: {}", iteration);
        return path;
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
     * 특정 구간의 거리 계산
     */
    private int calculateSegmentDistance(List<Position> path, int start, int end) {
        int distance = 0;
        for (int i = start; i <= end; i++) {
            if (i < path.size() - 1) {
                distance += path.get(i).manhattanDistance(path.get(i + 1));
            }
        }
        return distance;
    }
    
    /**
     * 전체 경로의 총 거리 계산
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

