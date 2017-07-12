/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.constructionheuristic;

import java.util.ArrayList;
import java.util.Collections;
import vrp.problem.NeighborElement;
import vrp.problem.Problem;
import vrp.problem.Solution;
import vrp.util.Util;

/**
 *
 * @author osman
 */
public class CWHParallel implements ConstructionHeuristic {

    private Problem problem;
    private int n, dropTime, vehicleCapacity, maxRouteTime;
    private final double[][] distanceMatrix;
    private int[] demands;

    public CWHParallel(Problem problem) {
        this.problem = problem;
        this.n = problem.getNumOfCustomers();
        this.distanceMatrix = problem.getDistanceMatrix();
        this.demands = problem.getDemands();
        this.dropTime = problem.getDropTime();
        this.vehicleCapacity = problem.getVehicleCapacity();
        this.maxRouteTime = problem.getMaxRouteTime();
    }

    @Override
    public Solution constructSolution() {
        long startTime = System.nanoTime();
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>(n);
        ArrayList<Double> routeLengths = new ArrayList<>(n);
        ArrayList<Integer> routeCapacities = new ArrayList<>(n);
        ArrayList<Saving> savings = new ArrayList<>(n * n);

        //init routes
        for (int i = 1; i <= n; i++) {
            ArrayList<Integer> route = new ArrayList<>();
            route.add(i);
            routeLengths.add(distanceMatrix[0][i] * 2);
            routeCapacities.add(demands[i]);
            routes.add(route);
        }

        //create savings list
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (i != j) {
                    double s = distanceMatrix[i][0] + distanceMatrix[0][j] - distanceMatrix[i][j];
                    savings.add(new Saving(i, j, s));
                }
            }
        }

        //Sort the savings in a decending order
        Collections.sort(savings);

        //scan saving list from the beginning
        for (int savingIndex = 0; savingIndex < savings.size(); savingIndex++) {
            Saving saving = savings.get(savingIndex);
            int i = saving.getI();
            int j = saving.getJ();

            ArrayList<Integer> routeStartingJ = null;
            ArrayList<Integer> routeEndingI = null;
            int routeStartingJIndex, routeEndingIIndex;

            //find route that is starting with j
            for (routeStartingJIndex = 0; routeStartingJIndex < routes.size(); routeStartingJIndex++) {
                ArrayList<Integer> route = routes.get(routeStartingJIndex);
                if (route.get(0) == j) {
                    routeStartingJ = route;
                    break;
                }
            }

            //find route that is ending with i
            for (routeEndingIIndex = 0; routeEndingIIndex < routes.size(); routeEndingIIndex++) {
                ArrayList<Integer> route = routes.get(routeEndingIIndex);
                if (route.get(route.size() - 1) == i) {
                    routeEndingI = route;
                    break;
                }
            }

            if (routeStartingJ == null || routeEndingI == null) {
                continue;
            }

            int mergedRouteSize = routeEndingI.size() + routeStartingJ.size();
            int mergedTotalCapacity = routeCapacities.get(routeEndingIIndex) + routeCapacities.get(routeStartingJIndex);
            double mergedTotalLength = routeLengths.get(routeEndingIIndex) + routeLengths.get(routeStartingJIndex) - saving.getSaving();
            double mergedTotalRouteTime = mergedTotalLength + mergedRouteSize * dropTime;

            //if two routes above were found, merge them.
            if ((routeEndingI != routeStartingJ)
                    && (mergedTotalCapacity <= vehicleCapacity)
                    && (mergedTotalRouteTime <= maxRouteTime)) {
                ArrayList<Integer> mergedRoute = new ArrayList<>();
                mergedRoute.addAll(routeEndingI);
                mergedRoute.addAll(routeStartingJ);

                //remove old routes along with capacity and length info
                if (routeStartingJIndex > routeEndingIIndex) {
                    routes.remove(routeStartingJIndex);
                    routeLengths.remove(routeStartingJIndex);
                    routeCapacities.remove(routeStartingJIndex);
                    routes.remove(routeEndingIIndex);
                    routeLengths.remove(routeEndingIIndex);
                    routeCapacities.remove(routeEndingIIndex);
                } else {
                    routes.remove(routeEndingIIndex);
                    routeLengths.remove(routeEndingIIndex);
                    routeCapacities.remove(routeEndingIIndex);
                    routes.remove(routeStartingJIndex);
                    routeLengths.remove(routeStartingJIndex);
                    routeCapacities.remove(routeStartingJIndex);
                }

                //add merged route at the end of the routes arraylist and add its capacity and length to the parallel arrays
                routes.add(mergedRoute);
                routeLengths.add(mergedTotalLength);
                routeCapacities.add(mergedTotalCapacity);
            }
        }

        double fitness = 0;
        for (double t : routeLengths) {
            fitness += t;
        }
        fitness = Util.applyPrecision(fitness, 4);
//        System.out.println("fitness: " + fitness);

        long estimatedTime = System.nanoTime() - startTime;
//        System.out.println("Elapsed Time: " + estimatedTime/1000 + " micro seconds");
        
        Solution solution = Util.createSolutionFromIntegerArrayLists(routes, fitness, problem);

        return solution;

    }

}
