/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import util.Util;

/**
 *
 * @author osman This class represents the VRP solution as a cyclic tour with
 * trip delimeters.
 */
public class Solution {

    private int[] next, prev;
    private NeighborElement[][] mNeighborLists; //copies of the depot (from n+1 to n+k(m)) are included here

    //five m-vectors
    private int[] tripNo, Qb, Qa; //Cumulative times before and after each node
    private double[] Cb, Ca; //Cumulative capacities before and after each node

    private int m, n, k; //k is the depot count, n is the customer count, m = n + k
    private double fitness; //as a convention, fitness value only includes length of the trips (service times are not included)

    private Problem problem;
    private int[] demands; //demand[0] is 0
    private int dropTime; //service time
    private double[][] distanceMatrix;

    /**
     *
     * @param k is the number of trips(routes)
     * @param fitness is the fitness value of the solution (service times are
     * not included)
     * @param nNeighborLists is used for creating mNeighborLists which considers
     * all the m elements (including extra depots in circular lists)
     * @param problem
     */
    public Solution(int k, double fitness, Problem problem) {
        this.problem = problem;
        this.n = problem.getNumOfCustomers();
        this.demands = problem.getDemands();
        this.dropTime = problem.getDropTime();
        this.distanceMatrix = problem.getDistanceMatrix();
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

        createMNeighborLists(problem.getnNeighborLists());
    }

    /**
     * will be used for clone operation
     */
    private Solution() {

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

    public int getN() {
        return n;
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

    /**
     * Should be called if the tour possibly be divided into unconnected sub-tours.
     * After this method, updateAfterLocalSearchModification() method should also be called
     * to re-calculate help vectors.
     */
    public void makeItOneCircularListAgain() {
        int node, d1, d2 = -1, s1, s2;
        boolean[] startNodeConsidered = new boolean[k];

        d1 = n + 1;
        startNodeConsidered[0] = true;
        //find other depot nodes that are accessible from d1
        node = d1;
        node = next[node];
        while (node != d1) {
            if (node > n) {
                startNodeConsidered[node - n - 1] = true;
            }
            node = next[node];
        }

        //find one of the unvisited depot as d2
        for (int i = 0; i < k; i++) {
            if (!startNodeConsidered[i]) {
                d2 = n + i + 1;
                break;
            }
        }
        
        if (d1 == -1 || d2 == -1) {
            return; //It is already a circular list
        }
        
        s1 = next[d1];
        s2 = next[d2];
        
        setNext(d2, s1);
        setNext(d1, s2);
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
                time = Util.applyPrecision(time, 4);
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
        time = Util.applyPrecision(time, 4);
        if (demand > problem.getVehicleCapacity()) {
            System.out.println("DEMAND FAZLA!!! " + demand + ", Alg no: " + algNo + ", Route No:" + routeNo);
            result = false;
        }
        if (time > problem.getMaxRouteTime()) {
            System.out.println("ROUTE TIME FAZLA!!! " + time + ", Alg no: " + algNo + ", Route No:" + routeNo);
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

    /**
     * Used by Tez3GUI to draw tours(tours are seperated by 0, first and last
     * elements are 0)
     *
     * @return
     */
    public int[] getTour() {
        int[] tour = new int[n + k + 1];

        int index = 0, node;
        boolean[] startNodeConsidered = new boolean[k];

        for (int depotNode = n + 1; depotNode <= m; depotNode++) {
            if (!startNodeConsidered[depotNode - n - 1]) {
                tour[index++] = depotNode > n ? 0 : depotNode;
                startNodeConsidered[depotNode - n - 1] = true;
                node = depotNode;
                node = next[node];
                while (node != depotNode) {

                    tour[index++] = node > n ? 0 : node;
                    if (node > n) {
                        startNodeConsidered[node - n - 1] = true;
                    }
                    node = next[node];
                }
                tour[index++] = node > n ? 0 : node;

            }
        }

        return tour;
    }

    public void setNext(int[] next) {
        this.next = next;
    }

    public void setPrev(int[] prev) {
        this.prev = prev;
    }

    public void setmNeighborLists(NeighborElement[][] mNeighborLists) {
        this.mNeighborLists = mNeighborLists;
    }

    public void setTripNo(int[] tripNo) {
        this.tripNo = tripNo;
    }

    public void setQb(int[] Qb) {
        this.Qb = Qb;
    }

    public void setQa(int[] Qa) {
        this.Qa = Qa;
    }

    public void setCb(double[] Cb) {
        this.Cb = Cb;
    }

    public void setCa(double[] Ca) {
        this.Ca = Ca;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public Problem getProblem() {
        return problem;
    }

    public Solution cloneSolution() {
        Solution clone = new Solution();

        int[] nextCopy = new int[next.length];
        System.arraycopy(next, 0, nextCopy, 0, next.length);
        clone.setNext(nextCopy);

        int[] prevCopy = new int[prev.length];
        System.arraycopy(prev, 0, prevCopy, 0, prev.length);
        clone.setPrev(prevCopy);

        clone.setmNeighborLists(mNeighborLists);

        int[] tripNoCopy = new int[tripNo.length];
        System.arraycopy(tripNo, 0, tripNoCopy, 0, tripNo.length);
        clone.setTripNo(tripNoCopy);

        int[] QbCopy = new int[Qb.length];
        System.arraycopy(Qb, 0, QbCopy, 0, Qb.length);
        clone.setQb(QbCopy);

        int[] QaCopy = new int[Qa.length];
        System.arraycopy(Qa, 0, QaCopy, 0, Qa.length);
        clone.setQa(QaCopy);

        double[] CaCopy = new double[Ca.length];
        System.arraycopy(Ca, 0, CaCopy, 0, Ca.length);
        clone.setCa(CaCopy);

        double[] CbCopy = new double[Cb.length];
        System.arraycopy(Cb, 0, CbCopy, 0, Cb.length);
        clone.setCb(CbCopy);

        clone.setM(m);
        clone.setN(n);
        clone.setK(k);
        clone.setFitness(fitness);

        clone.setProblem(problem);
        clone.setDemands(demands);
        clone.setDropTime(dropTime);
        clone.setDistanceMatrix(distanceMatrix);

        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Solution)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Solution sol = (Solution) obj;

        if (sol.getFitness() != getFitness()) {
            return false; //BELKİ KAPATILABİLİR (AYNI ÇÖZÜM KÜSÜRATLARLA FARKLI FİTNESS VEREBİLİR Mİ?) !!!!!
        }

        //check routes
        ArrayList<ArrayList<Integer>> otherSolution = Util.turnSolutiontoArrayLists(sol);
        ArrayList<ArrayList<Integer>> thisSolution = Util.turnSolutiontoArrayLists(this);

        int sizeOther = otherSolution.size();
        int sizeThis = thisSolution.size();

        if (sizeOther != sizeThis) {
            return false;
        }

        for (int i = 0; i < sizeThis; i++) {
            boolean match = false;
            // find match of i. route
            ArrayList<Integer> thisRoute = thisSolution.get(i);
            for (int j = 0; j < sizeOther; j++) {
                ArrayList<Integer> otherRoute = otherSolution.get(j);
                int thisRouteLength = thisRoute.size();
                int otherRouteLength = otherRoute.size();
                int index = 0;
                match = false;

                if (thisRouteLength != otherRouteLength) {
                    continue;
                }

                //straight
                while (Objects.equals(thisRoute.get(index), otherRoute.get(index))) {
                    index++;

                    if (index == thisRouteLength) { //two routes are equal
                        match = true;
                        break;
                    }
                }

                if (match) {
                    break;
                }

                //reverse
                index = 0;

                while (Objects.equals(thisRoute.get(index), otherRoute.get(thisRouteLength - index - 1))) {
                    index++;

                    if (index == thisRouteLength) { //two routes are equal
                        match = true;
                        break;
                    }
                }

                if (match) {
                    break;
                }
            }

            if (!match) { //one of the routes does not matched with any oother route
                return false;
            }
        }

        return true;

    }

    @Override
    public int hashCode() {
        int hash = 7;
        
        ArrayList<ArrayList<Integer>> routes = Util.turnSolutiontoArrayLists(this);
        ArrayList<String> routeCodes = new ArrayList<>(routes.size());
        
        for (ArrayList<Integer> route : routes) {
            String str = "";
            int leftIndex = 0;
            int rightIndex = route.size()-1;
            while (leftIndex < rightIndex) {
                int product = route.get(leftIndex++) * route.get(rightIndex--);
                str += product;
            }
            if (leftIndex == rightIndex) {
                str += route.get(leftIndex);
            }
            routeCodes.add(str);
        }
        
        Collections.sort(routeCodes);
        hash = 53 * hash + Objects.hashCode(routeCodes);
        
        return hash;
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
        
}
