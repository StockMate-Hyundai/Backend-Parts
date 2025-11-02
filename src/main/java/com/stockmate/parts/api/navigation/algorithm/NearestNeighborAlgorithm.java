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
        
        List<Position> path = new ArrayList<>();
        path.add(start);
        
        // LinkedHashSet으로 순서 보장 (일관된 결과를 위해)
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
        return path;
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

