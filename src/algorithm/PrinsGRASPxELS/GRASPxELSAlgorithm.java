/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.PrinsGRASPxELS;

import algorithm.PrinsGRASPxELS.localsearch.*;
import algorithm.beasley.Beasley;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import problem.Problem;
import problem.NeighborElement;
import problem.Solution;
import util.Util;

/**
 *
 * @author osman
 */
public class GRASPxELSAlgorithm {

    //for controlling progress bar
    JProgressBar progressBar;

    //ALGORITHM
    //parameters
    private int pMin, pMax;
    private int np, ni, nc;
    private boolean bi; //best improvement (for LS)
    private int lambda; //string length (for LS)
    private int SEED;
    //variables
    private Random random;
    private NeighborElement[][] nNeighborLists; //first row is for depot (size is n, not m!)
    //LS objects
    private ArrayList<LocalSearch> localSearches;

    //Beasley
    Beasley beasley;

    //GRASP 
    //parameters
    private double cMin, cMax, beta; //used for RCL
    //variables
    private int[] RCL;
    private int i, j;

    //PROBLEM
    private final Problem problem;
    private int n; //num of customers
    private int[] demands;
    private double[][] distanceMatrix;

    //for measuring algoritm
    double bestFitness;
    Solution bestSolution;
    int bestIter, lsIter;
    double[] totalImprovementsOfEachLS;

    public GRASPxELSAlgorithm(Problem problem, ParameterList params, JProgressBar pBar) {
        this.progressBar = pBar;
        this.problem = problem;
        setProblem();
        setParameters(params);
        initVariables();
        constructNeighborList();
        problem.setnNeighborLists(nNeighborLists);
        beasley = new Beasley(problem);
    }

    private void constructNeighborList() {
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
    }

    private void initVariables() {
        RCL = new int[n];
        nNeighborLists = new NeighborElement[n + 1][n]; //row zero is the depot

        localSearches = new ArrayList<>(5);
        localSearches.add(new Classical2Opt(problem));
        localSearches.add(new CrossoverMove(problem));
        localSearches.add(new SwapTwoNodes(problem));
        localSearches.add(new OrOptMove(problem));
        localSearches.add(new StringExchange(problem));
        localSearches.add(new StringExchangeWithInversion(problem));

    }

    private void setParameters(ParameterList params) {
        random = params.getRandom();
        beta = params.getBeta();
        np = params.getNp();
        ni = params.getNi();
        nc = params.getNc();
        pMax = params.getpMax();
        pMin = params.getpMin();
        bi = params.isBi();
        lambda = params.getLambda();
    }

    private void setProblem() {
        n = problem.getNumOfCustomers();
        distanceMatrix = problem.getDistanceMatrix();
        demands = problem.getDemands();
    }

    public Solution solve() {
        bestFitness = Double.MAX_VALUE;
        bestIter = 0;
        lsIter = 0;
        totalImprovementsOfEachLS = new double[localSearches.size()];
        for (int i = 0; i < localSearches.size(); i++) {
            totalImprovementsOfEachLS[i] = 0;
        }

        double fStar = Double.POSITIVE_INFINITY;
        int p = pMin;
        int[] T, TBar;
        Solution SStar = null, SBar;

        for (int i = 1; i <= np; i++) {
//            System.out.println("np = " + i);
            if (np == 1) {
                T = CWH();
                SBar = beasley.split(T);
            } else {
                T = RNNH();
                SBar = beasley.split(T);
            }

            SBar = applyLocalSearch(SBar, bi, lambda);
            TBar = SBar.concat();

            for (int j = 1; j <= ni; j++) {
//                System.out.print(j + ",");
                double fHat = SBar.getFitness();
                Solution SHat = null;

                for (int k = 1; k <= nc; k++) {
//                   System.out.println("\t\tnc = " + k);
                    T = TBar;
                    mutate(T, p);
                    Solution S = beasley.split(T);
                    S = applyLocalSearch(S, bi, lambda);
                    if (S.getFitness() < fHat) {
                        fHat = S.getFitness();
                        SHat = S;
                    }
                    incrementProgressBar();
                }

                if (fHat < SBar.getFitness()) {
                    SBar = SHat;
                    TBar = SBar.concat();
                    p = pMin;
                } else {
                    p = Math.min(p + 1, pMax);
                }
            }
            if (SBar.getFitness() < fStar) {
                fStar = SBar.getFitness();
                SStar = SBar;
            }
//            System.out.println("\tBest So far:");
//            SStar.printTours();
//            System.out.println("");
        }
//        printResults();
        if (SStar != null) {
            SStar = beasley.split(SStar.concat());
        }
        return SStar;
    }

    public Solution applyLocalSearch(Solution solution, boolean bi, int lambda) {
        int numOfImprovements;
        lsIter++;
//        System.out.println(lsIter);
        do {
//            System.out.println("\tLOCAL SEARCH PROCEDURE BEGINS....");
            numOfImprovements = 0;
            for (int i = 0; i < localSearches.size(); i++) {
                LocalSearch ls = localSearches.get(i);

                double fitnessBefore = solution.getFitness();
//                System.out.println(ls.getClass().getName() + " begins!");
                double GStar = ls.optimize(solution, bi, lambda);
                if (GStar > 0) {

                    numOfImprovements++;
                    /*int[] giantTour = solution.concat();
                    solution = beasley.split(giantTour);*/
                    double fitnessAfter = solution.getFitness();

                    if (fitnessAfter < bestFitness) {
                        bestFitness = solution.getFitness();
                        bestSolution = solution;
                        bestIter = lsIter;
                    }
                    totalImprovementsOfEachLS[i] += fitnessBefore - fitnessAfter;
                }
            }
        } while (numOfImprovements > 0);
//        System.out.println("\tLOCAL SEARCH PROCEDURE ENDS....");
        return solution;
    }

    /**
     * Finds a giant tour using Randomized Nearest Neighbor Heuristic.
     *
     * @return giant tour as an int array.
     */
    public int[] RNNH() {
        int[] giantTour = new int[n + 1];
        double[] cijArray = new double[n - 1];
        int index;
        ArrayList<Integer> unRoutedCustomers = new ArrayList(n);
        for (int k = 1; k <= n; k++) {
            unRoutedCustomers.add(k);
        }

        giantTour[0] = 0;
        i = unRoutedCustomers.remove(random.nextInt(unRoutedCustomers.size()));
        giantTour[1] = i;

        for (int k = 2; k <= n; k++) {
            // BUILDING RCL
            //find distances from i along with cMax and cMin
            cMax = Double.NEGATIVE_INFINITY;
            cMin = Double.POSITIVE_INFINITY;
            index = 0;
            for (int uc : unRoutedCustomers) {
                double dist = distanceMatrix[i][uc];
                if (dist > cMax) {
                    cMax = dist;
                }
                if (dist < cMin) {
                    cMin = dist;
                }
                cijArray[index++] = dist;
            }
            //find nodes to be added into RCL
            int RCLSize = 0;
            index = 0;
            double upperLimit = cMin + beta * (cMax - cMin);
            //System.out.println("cMin: " + cMin + ", cMax: " + cMax + ", upperLimit: " + upperLimit);
            for (int uc : unRoutedCustomers) {
                double cij = cijArray[index++];
                //System.out.println("c" + i + "," + uc + ": " + cij);
                if (cij >= cMin && cij <= upperLimit) {
                    //System.out.println("RCL[" + RCLSize + "]:" + uc);
                    RCL[RCLSize++] = uc;
                }
            }
            //System.out.println("RCL size: " + RCLSize);
            // select from RCL an element at random
            j = RCL[random.nextInt(RCLSize)];
            unRoutedCustomers.remove(new Integer(j)); //remove from unRouted
            // add element to solution
            giantTour[k] = j;

            i = j; // set j as new i
        }

        return giantTour;
    }

    private void mutate(int[] T, int p) {
        for (int k = 0; k < p; k++) {
            int index1 = random.nextInt(n) + 1; //1 to n
            while (T[index1] > n) {
                index1 = random.nextInt(n) + 1;
            }

            int index2 = random.nextInt(n) + 1;
            while (index2 == index1 || T[index2] > n) {
                index2 = random.nextInt(n) + 1;
            }

            //swap two customers
            int temp = T[index1];
            T[index1] = T[index2];
            T[index2] = temp;
        }
    }

    public NeighborElement[][] getnNeighborLists() {
        return nNeighborLists;
    }

    /**
     *
     */
    public int[] CWH() {
        int[] giantTour = new int[n + 1];
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
                    s = Util.applyPrecision(s, 2);
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
            double mergedTotalRouteTime = mergedTotalLength + mergedRouteSize * problem.getDropTime();

            //if two routes above were found, merge them.
            if ((routeEndingI != routeStartingJ)
                    && (mergedTotalCapacity <= problem.getVehicleCapacity())
                    && (mergedTotalRouteTime <= problem.getMaxRouteTime())) {
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

                //firstly, add depot node at the beginning of the merged root
                mergedRoute.add(0, 0);
                double threeOptGain = 0;

                //apply 3-opt to the merged route
                boolean improved;
                do {
                    int bestMoveNo = -1, aStar = -1, bStar = -1, cStar = -1;
                    double bestGain = 0;
                    improved = false;
                    if (mergedRoute.size() < 4) {
                        break;
                    }
                    for (int a = 0; a < mergedRoute.size() - 2; a++) {
                        for (int b = a + 1; b < mergedRoute.size() - 1; b++) {
                            for (int c = b + 1; c < mergedRoute.size(); c++) {
                                int i1 = mergedRoute.get(a);
                                int i1_s = mergedRoute.get(a + 1);
                                int i2 = mergedRoute.get(b);
                                int i2_s = mergedRoute.get(b + 1);
                                int i3 = mergedRoute.get(c);
                                int i3_s;
                                if (c == mergedRoute.size() - 1) {
                                    i3_s = mergedRoute.get(0);
                                } else {
                                    i3_s = mergedRoute.get(c + 1);
                                }
                                //calculate the gain that is obtained by removing 3 edges
                                double gainOfDeletingEdges = distanceMatrix[i1][i1_s] + distanceMatrix[i2][i2_s] + distanceMatrix[i3][i3_s];
                                double gain;

                                //move type 1
                                gain = gainOfDeletingEdges - distanceMatrix[i1][i1_s] - distanceMatrix[i2][i3] - distanceMatrix[i2_s][i3_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 1;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 2
                                gain = gainOfDeletingEdges - distanceMatrix[i2][i2_s] - distanceMatrix[i1][i3] - distanceMatrix[i1_s][i3_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 2;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 3
                                gain = gainOfDeletingEdges - distanceMatrix[i1][i2_s] - distanceMatrix[i2][i3] - distanceMatrix[i1_s][i3_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 3;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 4
                                gain = gainOfDeletingEdges - distanceMatrix[i1][i2_s] - distanceMatrix[i2][i3_s] - distanceMatrix[i3][i1_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 4;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 5
                                gain = gainOfDeletingEdges - distanceMatrix[i1][i2] - distanceMatrix[i2_s][i3_s] - distanceMatrix[i3][i1_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 5;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 6
                                gain = gainOfDeletingEdges - distanceMatrix[i1][i3] - distanceMatrix[i1_s][i2_s] - distanceMatrix[i2][i3_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 6;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }

                                //move type 7
                                gain = gainOfDeletingEdges - distanceMatrix[i3][i3_s] - distanceMatrix[i1][i2] - distanceMatrix[i1_s][i2_s];
                                if (gain > bestGain) {
                                    bestGain = gain;
                                    bestMoveNo = 7;
                                    aStar = a;
                                    bStar = b;
                                    cStar = c;
                                }
                            }
                        }
                    }
                    bestGain = Util.applyPrecision(bestGain, 10);
                    if (bestGain > 0) {
                        threeOptGain += bestGain;

                        ArrayList<Integer> a = new ArrayList<>();
                        ArrayList<Integer> b = new ArrayList<>();
                        ArrayList<Integer> c = new ArrayList<>();

                        //fill a,b and c arraylists
                        for (int index = aStar + 1; index <= bStar; index++) {
                            a.add(mergedRoute.get(index));
                        }

                        for (int index = bStar + 1; index <= cStar; index++) {
                            b.add(mergedRoute.get(index));
                        }

                        for (int index = cStar + 1; index < mergedRoute.size(); index++) {
                            c.add(mergedRoute.get(index));
                        }

                        for (int index = 0; index <= aStar; index++) {
                            c.add(mergedRoute.get(index));
                        }

                        improved = true;
                        switch (bestMoveNo) {
                            case 1: //aBc
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                for (int index = b.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(b.get(index));
                                }
                                mergedRoute.addAll(c);
                                break;
                            case 2: //abC
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                mergedRoute.addAll(b);
                                for (int index = c.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(c.get(index));
                                }
                                break;
                            case 3: //aBC
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                for (int index = b.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(b.get(index));
                                }
                                for (int index = c.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(c.get(index));
                                }
                                break;
                            case 4: //acb
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                mergedRoute.addAll(c);
                                mergedRoute.addAll(b);
                                break;
                            case 5: //aCb
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                for (int index = c.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(c.get(index));
                                }
                                mergedRoute.addAll(b);
                                break;
                            case 6: //acB
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                mergedRoute.addAll(c);
                                for (int index = b.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(b.get(index));
                                }
                                break;
                            case 7: //aCB
                                mergedRoute = new ArrayList<>();
                                mergedRoute.addAll(a);
                                for (int index = c.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(c.get(index));
                                }
                                for (int index = b.size() - 1; index >= 0; index--) {
                                    mergedRoute.add(b.get(index));
                                }
                                break;
                            default:
                                break;
                        }

                        //move depot to the beginning in a circular list
                        int indexOfDepot = mergedRoute.indexOf(new Integer(0));
                        ArrayList<Integer> beforeDepot = new ArrayList<>();
                        for (int index = indexOfDepot - 1; index >= 0; index--) {
                            beforeDepot.add(mergedRoute.remove(0));
                        }
                        mergedRoute.addAll(beforeDepot);

                    }
                } while (improved);

                //find and remove depot from the merged root
                mergedRoute.remove(new Integer(0));

                //add merged route at the end of the routes arraylist and add its capacity and length to the parallel arrays
                routes.add(mergedRoute);

                if (threeOptGain > 0) {
//                    System.out.println("Three opt gain: " + threeOptGain);
                    routeLengths.add(mergedTotalLength - threeOptGain);
                } else {
                    routeLengths.add(mergedTotalLength);
                }

                routeCapacities.add(mergedTotalCapacity);
            }
        }

        //convert to giant tour
        giantTour[0] = 0;
        int index = 1;
        for (ArrayList<Integer> route : routes) {
            for (Integer customer : route) {
                giantTour[index++] = customer;
            }
        }

        return giantTour;
    }

    private void incrementProgressBar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(progressBar.getValue() + 1);
            }
        });
    }

    private class Saving implements Comparable<Saving> {

        private final int i;
        private final int j;
        private final double saving;

        public Saving(int c1, int c2, double saving) {
            this.i = c1;
            this.j = c2;
            this.saving = saving;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        public double getSaving() {
            return saving;
        }

        @Override
        public int compareTo(Saving o) {
            if (o.getSaving() - saving > 0) {
                return 1;
            } else if (o.getSaving() - saving < 0) {
                return -1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return i + ", " + j + ": " + saving;
        }

    }

    public void printResults() {
        System.out.println("Best Fitness: " + bestFitness);
        System.out.println("Found in LS iter: " + bestIter);
        System.out.println("Total # of LS iter: " + lsIter);
        System.out.println("Total improvements of each LS:");
        for (double d : totalImprovementsOfEachLS) {
            System.out.println(d);
        }
    }

}
