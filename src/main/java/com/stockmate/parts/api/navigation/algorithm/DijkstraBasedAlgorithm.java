package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Graph Algorithm: Dijkstra를 활용한 전처리 + Nearest Neighbor
 * 시간복잡도: O(n³) (Floyd-Warshall) + O(n²) (NN)
 * 정확도: 60~80%
 * 
 * 모든 쌍 간 최단 거리를 사전 계산 후 Nearest Neighbor 적용
 * 창고 내 장애물이 있는 경우 유용하지만, 현재는 Manhattan Distance를 사용하므로
 * 실질적으로는 NN과 동일한 결과를 보임 (학습용)
 */
@Slf4j
@Component
public class DijkstraBasedAlgorithm implements PathOptimizationAlgorithm {
    
    @Override
    public List<Position> findOptimalPath(Position start, Position end, List<Position> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of(start, end);
        }
        
        // 모든 위치 (시작, 중간, 종료)
        List<Position> allPositions = new ArrayList<>();
        allPositions.add(start);
        allPositions.addAll(locations);
        allPositions.add(end);
        
        int n = allPositions.size();
        
        // Floyd-Warshall로 모든 쌍 간 최단 거리 계산
        int[][] dist = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = allPositions.get(i).manhattanDistance(allPositions.get(j));
                }
            }
        }
        
        // Floyd-Warshall
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE && dist[k][j] != Integer.MAX_VALUE) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }
        
        // Nearest Neighbor with precomputed distances
        List<Position> path = new ArrayList<>();
        path.add(start);
        
        Set<Integer> unvisited = new HashSet<>();
        for (int i = 1; i < n - 1; i++) {
            unvisited.add(i); // 중간 위치들
        }
        
        int current = 0; // 시작 인덱스
        
        while (!unvisited.isEmpty()) {
            int nearest = -1;
            int minDistance = Integer.MAX_VALUE;
            
            for (int candidate : unvisited) {
                if (dist[current][candidate] < minDistance) {
                    minDistance = dist[current][candidate];
                    nearest = candidate;
                }
            }
            
            if (nearest != -1) {
                path.add(allPositions.get(nearest));
                unvisited.remove(nearest);
                current = nearest;
            }
        }
        
        path.add(end);
        
        log.debug("Dijkstra-based (Floyd-Warshall + NN) 경로 찾기 완료");
        return path;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Floyd-Warshall + Nearest Neighbor (Graph)";
    }
    
    @Override
    public String getTimeComplexity() {
        return "O(n³)";
    }
    
    @Override
    public String getAccuracy() {
        return "60~80%";
    }
    
    @Override
    public String getDescription() {
        return "Floyd-Warshall로 모든 쌍 간 최단 거리를 사전 계산 후 Nearest Neighbor 적용 (장애물 처리 가능)";
    }
}

