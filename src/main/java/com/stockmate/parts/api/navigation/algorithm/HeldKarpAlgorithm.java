package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dynamic Programming Algorithm: Held-Karp (TSP 최적해)
 * 시간복잡도: O(n² × 2^n)
 * 정확도: 100% (최적해)
 * 
 * 부품 개수가 15개 이하일 때 권장
 */
@Slf4j
@Component
public class HeldKarpAlgorithm implements PathOptimizationAlgorithm {
    
    private static final int MAX_LOCATIONS = 20; // 성능을 위한 제한
    
    @Override
    public List<Position> findOptimalPath(Position start, Position end, List<Position> locations) {
        if (locations == null || locations.isEmpty()) {
            return List.of(start, end);
        }
        
        // 너무 많은 위치는 처리하지 않음 (지수 복잡도)
        if (locations.size() > MAX_LOCATIONS) {
            log.warn("Held-Karp: 위치 개수가 {}개로 제한을 초과하여 NN+2-opt로 대체합니다.", locations.size());
            // Fallback to 2-opt
            TwoOptAlgorithm twoOpt = new TwoOptAlgorithm(new NearestNeighborAlgorithm());
            return twoOpt.findOptimalPath(start, end, locations);
        }
        
        int n = locations.size();
        
        // DP 테이블: dp[집합][마지막 노드] = 최소 거리
        // 집합은 비트마스크로 표현 (2^n 가지)
        int[][] dp = new int[1 << n][n];
        int[][] parent = new int[1 << n][n]; // 경로 추적용
        
        // 초기화
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE / 2);
        }
        for (int[] row : parent) {
            Arrays.fill(row, -1);
        }
        
        // 시작: 문에서 각 위치로 이동
        for (int i = 0; i < n; i++) {
            dp[1 << i][i] = start.manhattanDistance(locations.get(i));
        }
        
        // DP 채우기
        for (int mask = 0; mask < (1 << n); mask++) {
            for (int last = 0; last < n; last++) {
                // 현재 집합에 last가 포함되어 있지 않으면 스킵
                if ((mask & (1 << last)) == 0) continue;
                
                // 현재 상태에서 도달 불가능하면 스킵
                if (dp[mask][last] == Integer.MAX_VALUE / 2) continue;
                
                // 다음 위치 선택
                for (int next = 0; next < n; next++) {
                    // 이미 방문한 위치는 스킵
                    if ((mask & (1 << next)) != 0) continue;
                    
                    int newMask = mask | (1 << next);
                    int newDistance = dp[mask][last] + locations.get(last).manhattanDistance(locations.get(next));
                    
                    if (newDistance < dp[newMask][next]) {
                        dp[newMask][next] = newDistance;
                        parent[newMask][next] = last;
                    }
                }
            }
        }
        
        // 모든 위치를 방문한 후 포장대로 가는 최소 거리 찾기
        int fullMask = (1 << n) - 1;
        int minDistance = Integer.MAX_VALUE;
        int lastNode = -1;
        
        for (int i = 0; i < n; i++) {
            int totalDistance = dp[fullMask][i] + locations.get(i).manhattanDistance(end);
            if (totalDistance < minDistance) {
                minDistance = totalDistance;
                lastNode = i;
            }
        }
        
        // 경로 재구성
        List<Position> path = new ArrayList<>();
        path.add(start);
        
        // 방문 순서 추적
        List<Integer> visitOrder = new ArrayList<>();
        int mask = fullMask;
        int current = lastNode;
        
        while (current != -1) {
            visitOrder.add(current);
            int prev = parent[mask][current];
            mask ^= (1 << current); // 현재 비트 제거
            current = prev;
        }
        
        // 역순으로 추가
        Collections.reverse(visitOrder);
        for (int idx : visitOrder) {
            path.add(locations.get(idx));
        }
        
        path.add(end);
        
        log.debug("Held-Karp 최적 경로 찾기 완료 - 총 거리: {}", minDistance);
        return path;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Held-Karp (Dynamic Programming)";
    }
    
    @Override
    public String getTimeComplexity() {
        return "O(n² × 2^n)";
    }
    
    @Override
    public String getAccuracy() {
        return "100% (최적해)";
    }
    
    @Override
    public String getDescription() {
        return "동적 계획법을 사용한 TSP 최적해 알고리즘 (부품 개수 ≤ 20개 권장)";
    }
}

