package com.stockmate.parts.api.navigation.algorithm.weight;

import com.stockmate.parts.api.navigation.algorithm.WeightNearestNeighborAlgorithm;
import com.stockmate.parts.api.navigation.algorithm.WeightPathOptimizationAlgorithm;
import com.stockmate.parts.api.navigation.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class WeightTwoOptAlgorithm implements WeightPathOptimizationAlgorithm {

    private final WeightNearestNeighborAlgorithm weightedNN;

    public WeightTwoOptAlgorithm(WeightNearestNeighborAlgorithm weightedNN) {
        this.weightedNN = weightedNN;
    }

    @Override
    public List<Position> findOptimalPath(
            Position start,
            Position end,
            List<Position> locations,
            Map<Position, Double> weightMap
    ) {
        // 1) 초기해 = Weighted NN
        List<Position> path = new ArrayList<>(
                weightedNN.findOptimalPath(start, end, locations, weightMap)
        );

        if (path.size() <= 3) return path;

        boolean improved = true;
        int maxIterations = 800;
        int iter = 0;

        while (improved && iter < maxIterations) {
            improved = false;
            iter++;

            for (int i = 1; i < path.size() - 2; i++) {
                for (int j = i + 1; j < path.size() - 1; j++) {

                    double oldCost = calcEdgeCost(path, weightMap);
                    List<Position> swapped = twoOptSwap(path, i, j);
                    double newCost = calcEdgeCost(swapped, weightMap);

                    if (newCost < oldCost) {
                        path = swapped;
                        improved = true;
                    }
                }
            }
        }

        return path;
    }

    private List<Position> twoOptSwap(List<Position> path, int i, int j) {
        List<Position> newPath = new ArrayList<>(path.subList(0, i));
        List<Position> rev = new ArrayList<>(path.subList(i, j + 1));
        Collections.reverse(rev);
        newPath.addAll(rev);
        newPath.addAll(path.subList(j + 1, path.size()));
        return newPath;
    }

    /**
     * total cost = Σ( (현재까지 수집 무게) × 거리 )
     */
    private double calcEdgeCost(List<Position> path, Map<Position, Double> weightMap) {

        double cost = 0;
        double currentWeight = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Position from = path.get(i);
            Position to = path.get(i + 1);

            // to 에서 weight 추가 (도착 후 든다고 가정)
            double nextW = weightMap.getOrDefault(to, 0.0);

            cost += currentWeight * from.manhattanDistance(to);
            currentWeight += nextW;
        }

        return cost;
    }

    @Override
    public String getAlgorithmName() {
        return "Weighted NN + 2-opt";
    }

    @Override
    public String getTimeComplexity() {
        return "O(n³)";
    }
}