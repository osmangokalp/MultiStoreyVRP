/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.beasley;

import java.util.ArrayList;
import problem.NeighborElement;
import problem.Problem;
import problem.Solution;

/**
 *
 * @author osman
 */
public class Beasley {

    //PROBLEM
    private final Problem problem = Problem.getInstance();
    private int n = problem.getNumOfCustomers(); //num of customers
    private int Q = problem.getVehicleCapacity(); //vehicle capacity
    private int L = problem.getMaxRouteTime(); //max route time
    private int dropTime = problem.getDropTime();
    private int[] demands = problem.getDemands();
    private double[][] distanceMatrix = problem.getDistanceMatrix();
    private NeighborElement[][] nNeighborLists;

    /**
     *  
     * @param nNeighborLists nNeighborLists is needed to initialize solution object. Because it is
     * always same for one problem instance and there is no need to calculate it again
     * in every solution object creation. So it is taken from the outside.
     */
    public Beasley(NeighborElement[][] nNeighborLists) {
        this.nNeighborLists = nNeighborLists;
    }
    
    /**
     *
     * @param S must start with 0 (depot node)!
     * @return
     */
    public Solution split(int[] S) {
        int[] P = new int[n + 1];
        double fitness = splittingProcedure(S, P);
        fitness -= (dropTime * n); //service time is not included in fitness!
        ArrayList<ArrayList<Integer>> routes = extractRoutes(S, P);
        Solution solution = createSolution(routes, fitness);
        return solution;
    }

    /**
     * Note that 0 is the depot node.
     *
     * @param S
     * @param P
     * @return
     */
    public double splittingProcedure(int[] S, int[] P) {
        double[] V = new double[n + 1];
        int j, load;
        double cost;
        //initialization
        V[0] = 0;
        P[0] = -1;
        for (int i = 1; i <= n; i++) {
            V[i] = Double.MAX_VALUE;
            P[i] = -1;
        }

        for (int i = 1; i <= n; i++) {
            load = 0;
            cost = 0;
            j = i;
            do {
                load += demands[S[j]];
                if (i == j) {
                    cost = distanceMatrix[0][S[j]] + dropTime + distanceMatrix[S[j]][0];
                } else {
                    cost = cost - distanceMatrix[S[j - 1]][0] + distanceMatrix[S[j - 1]][S[j]] + dropTime + distanceMatrix[S[j]][0];
                }
                if (load <= Q && cost <= L) {
                    if ((V[i - 1] + cost) < V[j]) {
                        V[j] = V[i - 1] + cost;
                        P[j] = i - 1;
                    }
                    j++;
                }

            } while (j <= n && load <= Q && cost <= L);
        }

        return V[n];
    }

    public ArrayList<ArrayList<Integer>> extractRoutes(int[] S, int[] P) {
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>(P.length - 1);
        int i, t = 0;
        int j = n; //n
        do {
            t++;
            i = P[j];
            ArrayList<Integer> route = new ArrayList<>();

            for (int k = i + 1; k <= j; k++) {
                route.add(S[k]);
            }
            routes.add(route);
            j = i;
        } while (i != 0);

        return routes;
    }
    
    /**
     * Constructs circular list of the given routes
     *
     * @param routes
     * @param fitness
     * @return
     */
    public Solution createSolution(ArrayList<ArrayList<Integer>> routes, double fitness) {
        int tripCount = routes.size();
        Solution solution = new Solution(n, tripCount, fitness, nNeighborLists);
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
}
