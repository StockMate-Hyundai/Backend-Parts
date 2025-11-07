package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;

import java.util.List;
import java.util.Map;

public interface WeightPathOptimizationAlgorithm {

    List<Position> findOptimalPath(
            Position start,
            Position end,
            List<Position> locations,
            Map<Position, Double> weightMap
    );

    String getAlgorithmName();
    String getTimeComplexity();
}