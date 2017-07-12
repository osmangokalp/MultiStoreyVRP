/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import vrp.problem.NeighborElement;
import vrp.problem.Problem;
import vrp.problem.Solution;

/**
 *
 * @author osman
 */
public class Util {

    public static double applyPrecision(double number, int precision) {
        try {
            if (number == Double.POSITIVE_INFINITY || number == Double.NEGATIVE_INFINITY) {
                return number;
            }
            BigDecimal bd = new BigDecimal(number);
            bd = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            return number;
        }
    }

    public static NeighborElement[][] constructNeighborLists(int n, double[][] distanceMatrix) {
        NeighborElement[][] nNeighborLists = new NeighborElement[n + 1][n]; //row zero is the depot
        for (int x = 0; x <= n; x++) { //zero is the depot node
            int index = 0;
            for (int y = 0; y <= n; y++) {
                if (x != y) { //there are n neigbbor for each customer(including depot and without itself)
                    double dist = distanceMatrix[x][y];
                    nNeighborLists[x][index++] = new NeighborElement(y, dist);
                }
            }
        }

        //sort according to the neighbor distances
        for (int x = 0; x <= n; x++) {
            Arrays.sort(nNeighborLists[x], new Comparator<NeighborElement>() {

                @Override
                public int compare(NeighborElement o1, NeighborElement o2) {
                    if (o1.getDistance() < o2.getDistance()) {
                        return -1;
                    } else if (o1.getDistance() > o2.getDistance()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }
        return nNeighborLists;
    }

    public static Solution createSolutionFromIntegerArrayLists(ArrayList<ArrayList<Integer>> routes, double fitness, Problem problem) {
        int tripCount = routes.size();
        int n = problem.getNumOfCustomers();
        Solution solution = new Solution(tripCount, fitness, problem);
        for (int k = 1; k <= tripCount; k++) {
            ArrayList<Integer> route = routes.get(k - 1);

            solution.setNext(n + k, route.get(0)); //each trip starts with depot n + k
            solution.setTripNo(n + k, k); //the depot at the beginning of trip k belongs to trip k

            for (int customerIndex = 0; customerIndex < route.size() - 1; customerIndex++) { //except last node
                int customer = route.get(customerIndex);
                int next = route.get(customerIndex + 1);
                solution.setNext(customer, next);
                solution.setTripNo(customer, k);
            }
            int lastNode = route.get(route.size() - 1);
            solution.setTripNo(lastNode, k);
            if (k == tripCount) { //if it is the last trip
                solution.setNext(lastNode, n + 1); //last trip ends at depot n + 1
            } else {
                solution.setNext(lastNode, n + k + 1); //last node ends at depot n + k + 1
            }
        }
        solution.calculateMVectors();
        return solution;
    }

    public static ArrayList<ArrayList<Integer>> turnSolutiontoArrayLists(Solution solution) {
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();

        int tour[] = solution.getTour();

        for (int i = 1; i < tour.length; i++) {
            ArrayList<Integer> route = new ArrayList<>();
            while (tour[i] != 0) {
                route.add(tour[i]);
                i++;
            }
            if (!route.isEmpty()) {
                routes.add(route);
            }
        }

        return routes;
    }
}
