package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;

import java.util.List;

/**
 * 경로 최적화 알고리즘 인터페이스
 */
public interface PathOptimizationAlgorithm {
    
    /**
     * 최적 경로 계산
     * @param start 시작 위치 (문)
     * @param end 종료 위치 (포장대)
     * @param locations 방문할 위치들
     * @return 최적 경로 (시작 -> 중간 위치들 -> 종료)
     */
    List<Position> findOptimalPath(Position start, Position end, List<Position> locations);
    
    /**
     * 알고리즘 이름
     */
    String getAlgorithmName();
    
    /**
     * 알고리즘 시간 복잡도
     */
    String getTimeComplexity();
    
    /**
     * 알고리즘 정확도 (%)
     */
    String getAccuracy();
    
    /**
     * 알고리즘 설명
     */
    String getDescription();
}

