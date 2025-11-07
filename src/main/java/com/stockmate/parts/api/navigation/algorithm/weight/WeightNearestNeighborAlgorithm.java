package com.stockmate.parts.api.navigation.algorithm;

import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class WeightNearestNeighborAlgorithm implements WeightPathOptimizationAlgorithm {

    @Override
    public List<Position> findOptimalPath(
            Position start,
            Position end,
            List<Position> locations,
            Map<Position, Double> weightMap
    ) {

        if (locations == null || locations.isEmpty()) {
            return List.of(start, end);
        }

        List<Position> path = new ArrayList<>();
        path.add(start);

        Set<Position> unvisited = new LinkedHashSet<>(locations);
        Position current = start;

        double carriedWeight = 0.0;

        while (!unvisited.isEmpty()) {

            Position next = null;
            double bestCost = Double.MAX_VALUE;

            for (Position cand : unvisited) {
                double w = weightMap.getOrDefault(cand, 0.0);
                int dist = current.manhattanDistance(cand);

                double cost = (carriedWeight + w) * dist;

                if (cost < bestCost) {
                    bestCost = cost;
                    next = cand;
                }
            }

            if (next == null) break;

            carriedWeight += weightMap.getOrDefault(next, 0.0);
            path.add(next);
            unvisited.remove(next);
            current = next;
        }

        path.add(end);

        return path;
    }

    @Override
    public String getAlgorithmName() {
        return "Weighted Nearest Neighbor";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n²)";
    }
}