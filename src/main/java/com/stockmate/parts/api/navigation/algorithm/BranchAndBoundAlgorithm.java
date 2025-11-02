package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Divide and Conquer Algorithm: Branch and Bound (분기 한정법)
 * 시간복잡도: 최악 O(n!), 평균적으로 훨씬 빠름
 * 정확도: 100% (최적해)
 * 
 * 부품 개수가 15개 이하일 때 권장
 */
@Slf4j
@Component
public class BranchAndBoundAlgorithm implements PathOptimizationAlgorithm {
    
    private static final int MAX_LOCATIONS = 15; // 성능을 위한 제한
    
    private int bestDistance;
    private List<Position> bestPath;
    private Position start;
    private Position end;
    private List<Position> locations;
    
    @Override
    public List<Position> findOptimalPath(Position start, Position end, List<Position> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of(start, end);
        }
        
        // 너무 많은 위치는 처리하지 않음 (팩토리얼 복잡도)
        if (locations.size() > MAX_LOCATIONS) {
            log.warn("Branch and Bound: 위치 개수가 {}개로 제한을 초과하여 NN+2-opt로 대체합니다.", locations.size());
            // Fallback to 2-opt
            TwoOptAlgorithm twoOpt = new TwoOptAlgorithm(new NearestNeighborAlgorithm());
            return twoOpt.findOptimalPath(start, end, locations);
        }
        
        this.start = start;
        this.end = end;
        this.locations = new ArrayList<>(locations);
        this.bestDistance = Integer.MAX_VALUE;
        this.bestPath = null;
        
        // 상한선 설정 (NN으로 초기 해 구하기)
        NearestNeighborAlgorithm nn = new NearestNeighborAlgorithm();
        List<Position> initialPath = nn.findOptimalPath(start, end, locations);
        this.bestDistance = calculateTotalDistance(initialPath);
        this.bestPath = initialPath;
        
        log.debug("Branch and Bound 초기 상한선: {}", bestDistance);
        
        // Branch and Bound 시작
        List<Position> currentPath = new ArrayList<>();
        currentPath.add(start);
        Set<Position> visited = new HashSet<>();
        
        branchAndBound(currentPath, visited, 0);
        
        log.debug("Branch and Bound 최적 경로 찾기 완료 - 총 거리: {}", bestDistance);
        return new ArrayList<>(bestPath);
    }
    
    /**
     * Branch and Bound 재귀 함수
     * @param currentPath 현재까지의 경로
     * @param visited 방문한 위치들
     * @param currentDistance 현재까지의 거리
     */
    private void branchAndBound(List<Position> currentPath, Set<Position> visited, int currentDistance) {
        // 가지치기: 현재 거리가 이미 최선해보다 크면 중단
        if (currentDistance >= bestDistance) {
            return;
        }
        
        // 모든 위치를 방문했으면 종료점으로 이동
        if (visited.size() == locations.size()) {
            Position last = currentPath.get(currentPath.size() - 1);
            int totalDistance = currentDistance + last.manhattanDistance(end);
            
            if (totalDistance < bestDistance) {
                bestDistance = totalDistance;
                bestPath = new ArrayList<>(currentPath);
                bestPath.add(end);
                log.debug("Branch and Bound: 더 나은 해 발견 - 거리: {}", bestDistance);
            }
            return;
        }
        
        // 하한선 계산 (현재 거리 + 미방문 위치들의 최소 거리 추정)
        int lowerBound = currentDistance + calculateLowerBound(currentPath.get(currentPath.size() - 1), visited);
        if (lowerBound >= bestDistance) {
            return; // 가지치기
        }
        
        // 다음 위치 선택
        Position current = currentPath.get(currentPath.size() - 1);
        
        // 미방문 위치를 거리 순으로 정렬 (더 나은 가지치기를 위해)
        List<Position> candidates = new ArrayList<>();
        for (Position location : locations) {
            if (!visited.contains(location)) {
                candidates.add(location);
            }
        }
        candidates.sort(Comparator.comparingInt(current::manhattanDistance));
        
        // 각 후보 위치 탐색
        for (Position next : candidates) {
            currentPath.add(next);
            visited.add(next);
            
            int distance = current.manhattanDistance(next);
            branchAndBound(currentPath, visited, currentDistance + distance);
            
            // 백트래킹
            currentPath.remove(currentPath.size() - 1);
            visited.remove(next);
        }
    }
    
    /**
     * 하한선 계산 (Minimum Spanning Tree 근사)
     * 현재 위치에서 미방문 위치들까지의 최소 거리의 합
     */
    private int calculateLowerBound(Position current, Set<Position> visited) {
        int bound = 0;
        
        // 현재 위치에서 가장 가까운 미방문 위치까지의 거리
        int minToUnvisited = Integer.MAX_VALUE;
        for (Position location : locations) {
            if (!visited.contains(location)) {
                int dist = current.manhattanDistance(location);
                minToUnvisited = Math.min(minToUnvisited, dist);
            }
        }
        if (minToUnvisited != Integer.MAX_VALUE) {
            bound += minToUnvisited;
        }
        
        // 가장 가까운 미방문 위치에서 종료점까지의 최소 거리
        int minToEnd = Integer.MAX_VALUE;
        for (Position location : locations) {
            if (!visited.contains(location)) {
                int dist = location.manhattanDistance(end);
                minToEnd = Math.min(minToEnd, dist);
            }
        }
        if (minToEnd != Integer.MAX_VALUE) {
            bound += minToEnd;
        }
        
        return bound;
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
    
    @Override
    public String getAlgorithmName() {
        return "Branch and Bound (Divide and Conquer)";
    }
    
    @Override
    public String getTimeComplexity() {
        return "O(n!) ~ 평균적으로 훨씬 빠름";
    }
    
    @Override
    public String getAccuracy() {
        return "100% (최적해)";
    }
    
    @Override
    public String getDescription() {
        return "분기 한정법을 사용한 TSP 최적해 알고리즘 (부품 개수 ≤ 15개 권장)";
    }
}

