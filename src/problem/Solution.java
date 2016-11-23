/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package problem;

import java.util.ArrayList;
import utility.Util;

/**
 *
 * @author osman This class represents the VRP solution as a cyclic tour with
 * trip delimeters.
 */
public class Solution {

    private int[] next, prev;
    private NeighborElement[][] mNeighborLists; //depot nodes from n+1 to n+k(m) is included here

    //five m-vectors
    private int[] tripNo, Qb, Qa; //Cumulative times before and after each node
    private double[] Cb, Ca; //Cumulative capacities before and after each node

    private int m, n, k; //k is the depot count, n is the customer count, m = n + k
    private double fitness; //as a convention, fitness value only includes length of the trips (service times are not included)

    private final Problem problem = Problem.getInstance();
    private final int[] demands = problem.getDemands(); //demand[0] is 0
    private final int dropTime = problem.getDropTime(); //service time
    private final double[][] distanceMatrix = problem.getDistanceMatrix();

    /**
     *
     * @param n is the number of customers
     * @param k is the number of trips(routes)
     * @param fitness is the fitness value of the solution (service times are
     * not included)
     * @param nNeighborLists is used for creating mNeighborLists which considers
     * all the m elements (including extra depots in circular lists)
     */
    public Solution(int n, int k, double fitness, NeighborElement[][] nNeighborLists) {
        this.n = n;
        this.k = k;
        this.fitness = fitness;
        this.m = n + k;
        next = new int[m + 1]; //index will start at 1, so the array needs extra one element at the end
        prev = new int[m + 1];
        tripNo = new int[m + 1];
        Qb = new int[m + 1];
        Qa = new int[m + 1];
        Cb = new double[m + 1];
        Ca = new double[m + 1];

        createMNeighborLists(nNeighborLists);
    }

    /**
     * It converts the VRP solution by concatenating the sequences of customers
     * of its different trips, which is equivalent to erasing the copies of the
     * depot node used as trip delimeters. First element of the giant tour is
     * zero. (because split procedure expects this)
     *
     * @return giant tour that is encoded as permutation of the n customers.
     */
    public int[] concat() {

        int[] giantTour = new int[n + 1];
        giantTour[0] = 0;
        int index = 1, node;
        boolean[] startNodeConsidered = new boolean[k];

        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
                startNodeConsidered[depotNode - n - 1] = true;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                    } else {
                        giantTour[index++] = node;
                    }
                    node = next[node];
                }

            }
        }
        return giantTour;
    }

    /**
     * Also sets previous by setting node as previous of nextNode
     *
     * @param node
     * @param nextNode
     */
    public void setNext(int node, int nextNode) {
        next[node] = nextNode;
        prev[nextNode] = node;
    }

    public void setTripNo(int node, int tripNo) {
        this.tripNo[node] = tripNo;
    }

    public int getM() {
        return m;
    }

    public int[] getNext() {
        return next;
    }

    public int[] getPrev() {
        return prev;
    }

    public int[] getTripNo() {
        return tripNo;
    }

    public int[] getQb() {
        return Qb;
    }

    public int[] getQa() {
        return Qa;
    }

    public double[] getCb() {
        return Cb;
    }

    public double[] getCa() {
        return Ca;
    }

    public int getK() {
        return k;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public NeighborElement[][] getmNeighborLists() {
        return mNeighborLists;
    }

    /**
     * Inverts the segment that is between start and end nodes (both are
     * included) ande connects them with the specified new neighbor nodes. In
     * the original cyclic tour, the direction must be from start from end. This
     * means that end node will be reached from start node by next operations.
     *
     * @param start
     * @param end
     * @param startNeighbor is to be connected start node after inversion.
     * @param endNeighbor is to be connected end node after inversion.
     */
    public void invertSegment(int start, int end, int startNeighbor, int endNeighbor) {
        int temp, temp2;

        temp = start;
        while (temp != end) {
            temp2 = next[temp];
            prev[temp] = temp2;
            temp = temp2;
        }

        temp = start;
        while (temp != end) {
            temp2 = prev[temp];
            next[temp2] = temp;
            temp = temp2;
        }

        prev[startNeighbor] = start;
        next[start] = startNeighbor;
        next[endNeighbor] = end;
        prev[end] = endNeighbor;
    }

    /**
     * Calculates the mVectors which will be used in constraint checking
     * operations later.
     */
    public void calculateMVectors() {

        //calculate Qa, Qb, Ca, Cb
        for (int i = 1; i <= n; i++) {
            int node = i;
            Qa[i] = demands[node]; //i is included
            Ca[i] = dropTime; //i is included
            while (next[node] <= n) { //only for nodes at trip(i)
                Ca[i] += distanceMatrix[node][next[node]] + dropTime;
                node = next[node];
                Qa[i] += demands[node];
            }
            Ca[i] += distanceMatrix[node][0]; //cost of between last node and the depot

            node = i;
            Qb[i] = demands[node]; //i is included
            Cb[i] = dropTime;
            while (prev[node] <= n) { //only for nodes at trip(i)
                Cb[i] += distanceMatrix[node][prev[node]] + dropTime;
                node = prev[node];
                Qb[i] += demands[node];
            }
            Cb[i] += distanceMatrix[node][0]; //cost of between first node and the depot
        }
        for (int i = n + 1; i <= m; i++) { //for depot nodes
            Qb[i] = 0;
            Qa[i] = 0;
            Ca[i] = 0;
            Cb[i] = 0;
        }

        //Find trip numbers
        int tNo = -1;
        int node;
        boolean[] startNodeConsidered = new boolean[k];
        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
                startNodeConsidered[depotNode - n - 1] = true;
                this.tripNo[depotNode] = ++tNo;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                        this.tripNo[node] = ++tNo;
                    } else {
                        this.tripNo[node] = tNo;
                    }
                    node = next[node];
                }
            }
        }
    }

    public void updateAfterLocalSearchModification(double gain) {
        this.fitness -= gain;
        calculateMVectors();
    }

    /**
     * This will create the new neighbor lists of each node so that depot nodes
     * from n + 1 to n + k are also included.
     *
     * @param nNeighborLists is the list does not contain depot nodes from n + 1
     * to n + k.
     */
    private void createMNeighborLists(NeighborElement[][] nNeighborLists) {
        mNeighborLists = new NeighborElement[m + 1][m - 1]; //0. row will not used. There are m-1 columns because node itself is not considered as neighbor

        for (int i = 1; i <= n; i++) {
            int index = 0;
            for (int j = 0; j < n; j++) {
                NeighborElement ne = nNeighborLists[i][j];
                if (ne.getNo() == 0) { //if it is depot node
                    for (int x = 1; x <= k; x++) {
                        mNeighborLists[i][index++] = new NeighborElement(n + k, ne.getDistance());
                    }
                } else {
                    mNeighborLists[i][index++] = ne;
                }
            }
        }

        for (int i = n + 1; i <= m; i++) {
            int index = 0;
            for (int x = 1; x <= k; x++) {
                if (i != n + x) {
                    mNeighborLists[i][index++] = new NeighborElement(n + x, 0); //distance between depots are determined as 0 !!!
                }
            }
            for (int j = 0; j < n; j++) {
                mNeighborLists[i][index++] = nNeighborLists[0][j]; //copy from 0. row because it represents depot node in the nNeighborLists
            }
        }
    }

    public ArrayList<Double> getTimeOfEachRoute() {
        ArrayList<Double> routeTimes = new ArrayList<>();
        double time = 0;
        int startNode = n + 1;
        int currentNode = startNode;
        int nextNode = next[currentNode];
        int routeNo = 0;
        while (nextNode != startNode) {
            if (nextNode > n) {
                routeNo++;
                time += distanceMatrix[currentNode][0];
                time = Util.applyPrecision(time, 2);
                routeTimes.add(time);
                time = 0;
            } else {
                time += distanceMatrix[currentNode > n ? 0 : currentNode][nextNode] + dropTime;
            }
            currentNode = next[currentNode];
            nextNode = next[currentNode];
        }
        time += distanceMatrix[currentNode][0];
        routeTimes.add(Util.applyPrecision(time, 2));
        
        return routeTimes;
    }
    
    public ArrayList<Integer> getDemandOfEachRoute() {
        ArrayList<Integer> routeDemands = new ArrayList<>();
        
        int demand = 0;
        int startNode = n + 1;
        int currentNode = startNode;
        int nextNode = next[currentNode];
        int routeNo = 0;
        while (nextNode != startNode) {
            if (nextNode > n) {
                routeNo++;
                routeDemands.add(demand);
                demand = 0;
            } else {
                demand += demands[nextNode];
            }
            currentNode = next[currentNode];
            nextNode = next[currentNode];
        }
        
        routeDemands.add(demand);
        return routeDemands;
    }
    
    public boolean checkSolution(int algNo) {
        boolean result = true;
        double total = 0, demand = 0, time = 0, totalTime = 0;
        int startNode = n + 1;
        int currentNode = startNode;
        int nextNode = next[currentNode];
        int routeNo = 0;
        while (nextNode != startNode) {
            total += distanceMatrix[currentNode > n ? 0 : currentNode][nextNode > n ? 0 : nextNode];
            if (nextNode > n) {
                routeNo++;
//                System.out.println("Demand: " + demand);
                time += distanceMatrix[currentNode][0];
//                System.out.println("Time: " + time);
//                System.out.println("");
                totalTime += time;
                time = Util.applyPrecision(time, 2);
                if (demand > problem.getVehicleCapacity()) {
                    System.out.println("DEMAND FAZLA!!! " + demand + ", Alg no: " + algNo + ", Route No:" + routeNo);
                    result = false;
                }
                if (time > problem.getMaxRouteTime()) {
                    System.out.println("ROUTE TIME FAZLA!!! " + time + ", Alg no: " + algNo + ", Route No:" + routeNo);
                    result = false;
                }
                demand = 0;
                time = 0;
            } else {
                demand += demands[nextNode];
                time += distanceMatrix[currentNode > n ? 0 : currentNode][nextNode] + dropTime;
            }
            currentNode = next[currentNode];
            nextNode = next[currentNode];
        }
        time += distanceMatrix[currentNode][0];
        if (demand > problem.getVehicleCapacity()) {
            System.out.println("DEMAND FAZLA!!! " + demand + ", Alg no: " + algNo + ", Route No:" + routeNo);
            result = false;
        }
        if (time > problem.getMaxRouteTime()) {
            System.out.println("ROUTE TIME FAZLA!!! " + demand + ", Alg no: " + algNo + ", Route No:" + routeNo);
            result = false;
        }
//        System.out.println("Demand: " + demand);
//        System.out.println("Time: " + time);
//        System.out.println("");
//        System.out.println("Total Time: " + totalTime);
//        System.out.println("Fitness: " + total);
        return result;
    }

    /**
     * This method can be used when the fitness value is not specified from the
     * outside. It can calculate and update the fitness value.
     */
    public void calculateFitness() {
        double f = 0;

        int node;
        boolean[] startNodeConsidered = new boolean[k];

        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
                startNodeConsidered[depotNode - n - 1] = true;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                        if (prev[node] > n) { //two depot nodes one after another
                            node = next[node];
                            continue;
                        }
                    }
                    f += distanceMatrix[node > n ? 0 : node][prev[node] > n ? 0 : prev[node]];
                    node = next[node];
                }
                f += distanceMatrix[0][prev[depotNode]];
            }
        }
        this.fitness = f;
    }

    public void printTours() {
        System.out.println("\tPRINT TOURS Fitness: " + fitness);
        int index = 1, node;
        boolean[] startNodeConsidered = new boolean[k];

        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
                System.out.print("\t\t" + depotNode + ", ");
                startNodeConsidered[depotNode - n - 1] = true;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

                    System.out.print(node + ", ");
                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                    }
                    node = next[node];
                }
                System.out.print(node + ", ");

            }
        }
        System.out.println("");
    }

    public void printNextArray() {
        int index = 0;
        System.out.print("Next Array: ");
        for (int i : next) {
            System.out.print("N[" + (index++) + "]" + "=" + i + ", ");
        }
        System.out.println("");
    }

    public void printPrevArray() {
        int index = 0;
        System.out.print("Prev Array: ");
        for (int i : prev) {
            System.out.print("P[" + (index++) + "]" + "=" + i + ", ");
        }
        System.out.println("");
    }

    public void printTripArray() {
        int index = 0;
        System.out.print("Trip Array: ");
        for (int i : tripNo) {
            System.out.print("T[" + (index++) + "]" + "=" + i + ", ");
        }
        System.out.println("");
    }

    public ArrayList<ArrayList<Integer>> getRoutesAsArraylists() {
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        ArrayList<Integer> route = new ArrayList<>();
        int node;
        boolean[] startNodeConsidered = new boolean[k];

        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
//                System.out.print("\t\t" + depotNode + ", ");
                startNodeConsidered[depotNode - n - 1] = true;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

//                    System.out.print(node + ", ");
                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                        routes.add(route);
                        route = new ArrayList<>();
                    } else {
                        route.add(node);

                    }
                    node = next[node];
                }
//                System.out.print(node + ", ");
                routes.add(route);
            }
        }
        return routes;
    }

}
