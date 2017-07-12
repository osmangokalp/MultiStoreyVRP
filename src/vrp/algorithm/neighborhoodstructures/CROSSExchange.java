/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.neighborhoodstructures;

import java.util.ArrayList;
import java.util.Random;
import vrp.problem.Solution;
import vrp.util.Util;

/**
 *
 * @author user Based on "A Tabu Search Heuristic for the Vehicle Routing
 * Problem with Soft Time Windows" (Taillard and Badeau, 1997)"
 */
public class CROSSExchange implements NeighborhoodStructure {

    /**
     *
     * @param solution that the neighbor is to be generated from
     * @param size is the length limit of the segments to be exchanged (max length)
     * @param random is used for random index generation for the segments
     * @return generated random neighbor
     */
    @Override
    public Solution generateRandomNeighbor(Solution solution, int size, Random random) {
        Solution neighborSolution = solution.cloneSolution();
        boolean found = false;
        int X1, Y1, X2, Y2, X1Prime, Y1Prime, X2Prime, Y2Prime;
        int k = neighborSolution.getK(); //get the #of routes
        int n = neighborSolution.getN(); //number of nodes
        int[] tripNo = neighborSolution.getTripNo(); //get the trips array
        int[] next = neighborSolution.getNext(); //get the next array
        int[] Qa = neighborSolution.getQa();
        int[] Qb = neighborSolution.getQb();
        double[] Ca = neighborSolution.getCa();
        double[] Cb = neighborSolution.getCb();
        int[] demands = neighborSolution.getProblem().getDemands();
        int droptime = neighborSolution.getProblem().getDropTime();
        int Q = neighborSolution.getProblem().getVehicleCapacity();
        int L = neighborSolution.getProblem().getMaxRouteTime();
        double[][] c = neighborSolution.getProblem().getDistanceMatrix();
        int counter, current; //helper variables
        double gain = Double.NEGATIVE_INFINITY;

        boolean successiveDepots = false;
        while (!found) {

            // first select X1 and X2
            X1 = random.nextInt(n + k) + 1;
            X2 = random.nextInt(n + k) + 1;
            while (tripNo[X1] == tripNo[X2]) //ensure that X1 and X2 belong to different tours
            {
                X2 = random.nextInt(n) + 1;
            }

            X1Prime = next[X1];
            X2Prime = next[X2];

            //select Y1 from X1 to the maxLength or depot
            //firstly find the candidates
            ArrayList<Integer> candidates = new ArrayList<>();
            counter = 0;
            current = X1;
            if (current <= n) { //Y cannot be depot
                candidates.add(current);
            }

            while ((current = next[current]) <= n && counter++ < size) {
                candidates.add(current);
            }
            
            if (candidates.isEmpty())
                continue;

            Y1 = candidates.get(random.nextInt(candidates.size()));

            //select Y2 from X2 to the maxLength or depot
            //firstly find the candidates
            candidates.clear();
            counter = 0;
            current = X2;

            if (X1 != Y1 && current <= n) //if X1 == Y1, Y2 must not be X2 and also Y cannot be depot
            {
                candidates.add(current);
            }

            while ((current = next[current]) <= n && counter++ < size) {
                candidates.add(current);
            }
            if (candidates.isEmpty()) {
                continue; //there is no alternative, start again
            }
            Y2 = candidates.get(random.nextInt(candidates.size()));

            Y1Prime = next[Y1];
            Y2Prime = next[Y2];

            boolean capaok;
            boolean timeok;

            int d1 = Qa[X1Prime] - Qa[Y1] + demands[Y1];
            int d2 = Qa[X2Prime] - Qa[Y2] + demands[Y2];
            double t1 = Ca[X1Prime] - Ca[Y1] + droptime;
            double t2 = Ca[X2Prime] - Ca[Y2] + droptime;

            if (X1 != Y1 && X2 != Y2) { //two strings exchange
                capaok = Qb[X1 > n ? 0 : X1] + d2 + Qa[Y1Prime] <= Q && Qb[X2 > n ? 0 : X2] + d1 + Qa[Y2Prime] <= Q;
                timeok = Cb[X1 > n ? 0 : X1] + t2 + Ca[Y1Prime] + c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] + c[Y2][Y1Prime > n ? 0 : Y1Prime] <= L
                        && Cb[X2 > n ? 0 : X2] + t1 + Ca[Y2Prime] + c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] + c[Y1][Y2Prime > n ? 0 : Y2Prime] <= L;

                if (capaok && timeok) {
                    neighborSolution.setNext(X1, X2Prime);
                    neighborSolution.setNext(Y2, Y1Prime);
                    neighborSolution.setNext(X2, X1Prime);
                    neighborSolution.setNext(Y1, Y2Prime);
                    gain = c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime] + c[Y1][Y1Prime > n ? 0 : Y1Prime]
                            + c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime] + c[Y2][Y2Prime > n ? 0 : Y2Prime]
                            - c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] - c[Y2][Y1Prime > n ? 0 : Y1Prime]
                            - c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] - c[Y1][Y2Prime > n ? 0 : Y2Prime];
                } else {
                    continue;
                }
            } else {
                if (X1 == Y1) {
                    capaok = Qb[X1 > n ? 0 : X1] + d2 + Qa[Y1Prime] <= Q;
                    timeok = Cb[X1 > n ? 0 : X1] + t2 + Ca[Y1Prime] + c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] + c[Y2][Y1Prime > n ? 0 : Y1Prime] <= L;

                    if (capaok && timeok) {
                        neighborSolution.setNext(X1, X2Prime);
                        neighborSolution.setNext(Y2, Y1Prime);
                        neighborSolution.setNext(X2, Y2Prime);
                        if (X2 > n && Y2Prime > n){
                            successiveDepots = true;
//                            System.out.println("CROSSEXCHANGE: arka arkaya 2 depo");
                        }
                        gain = c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime] + c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime]
                                + c[Y2][Y2Prime > n ? 0 : Y2Prime] - c[X2 > n ? 0 : X2][Y2Prime > n ? 0 : Y2Prime]
                                - c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] - c[Y2][X1Prime > n ? 0 : X1Prime];

                    } else {
                        continue;
                    }

                } else { //X2 == Y2
                    capaok = Qb[X2 > n ? 0 : X2] + d1 + Qa[Y2Prime] <= Q;
                    timeok = Cb[X2 > n ? 0 : X2] + t1 + Ca[Y2Prime] + c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] + c[Y1][Y2Prime > n ? 0 : Y2Prime] <= L;

                    if (capaok && timeok) {
                        neighborSolution.setNext(X2, X1Prime);
                        neighborSolution.setNext(Y1, Y2Prime);
                        neighborSolution.setNext(X1, Y1Prime);
                        if (X1 > n && Y1Prime > n) {
                            successiveDepots = true;
//                            System.out.println("CROSSEXCHANGE: arka arkaya 2 depo");
                        }
                        gain = c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime] + c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime]
                                + c[Y1][Y1Prime > n ? 0 : Y1Prime] - c[X1 > n ? 0 : X1][Y1Prime > n ? 0 : Y1Prime]
                                - c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] - c[Y1][X2Prime > n ? 0 : X2Prime];
                    } else {
                        continue;
                    }

                }
            }

            //found
            found = true;

        }
        
        if (successiveDepots) {
            ArrayList<ArrayList<Integer>> routes = Util.turnSolutiontoArrayLists(neighborSolution);
            neighborSolution = Util.createSolutionFromIntegerArrayLists(routes, -1, neighborSolution.getProblem());
            neighborSolution.calculateFitness();
//            neighborSolution.printTours();
        } else {
            neighborSolution.makeItOneCircularListAgain();
            neighborSolution.updateAfterLocalSearchModification(gain);
        }
        return neighborSolution;
    }

    /**
     *
     * @param solution that the neighbor is to be generated from
     * @param size is the length limit of the segments to be exchanged (max length)
     * @param bi
     * @return generated random neighbor
     */
    public Solution findImprovingNeighbor(Solution solution, int size, boolean bi) {
        Solution neighborSolution;
        boolean found = false;
        int X1, Y1, X2, Y2, X1Prime, Y1Prime, X2Prime, Y2Prime;
        int k = solution.getK(); //get the #of routes
        int n = solution.getN(); //number of nodes
        int[] tripNo = solution.getTripNo(); //get the trips array
        int[] next = solution.getNext(); //get the next array
        int[] Qa = solution.getQa();
        int[] Qb = solution.getQb();
        double[] Ca = solution.getCa();
        double[] Cb = solution.getCb();
        int[] demands = solution.getProblem().getDemands();
        int droptime = solution.getProblem().getDropTime();
        int Q = solution.getProblem().getVehicleCapacity();
        int L = solution.getProblem().getMaxRouteTime();
        double[][] c = solution.getProblem().getDistanceMatrix();
        int counter; //helper variables
        double gain = Double.NEGATIVE_INFINITY;

        Solution bestSolution = solution;

        for (X1 = 1; X1 <= n + k; X1++) {
            X1Prime = next[X1];

            for (X2 = 1; X2 <= n + k; X2++) {
                if (tripNo[X1] == tripNo[X2]) {
                    continue;
                }

                X2Prime = next[X2];

                counter = 0;
                for (Y1 = X1; Y1 <= n; Y1 = next[Y1]) {
                    if (Y1 > n) { //cannot be depot
                        continue;
                    }
                    if (counter++ > size) {
                        break;
                    }
                    Y1Prime = next[Y1];

                    counter = 0;
                    for (Y2 = X2; Y2 <= n; Y2 = next[Y2]) {
                        if (Y2 > n || (X1 == Y1 && X2 == Y2)) { //bot strings cannot be empty, and Y2 cannot be depot
                            continue;
                        }

                        if (counter++ > size) {
                            break;
                        }
                        
                        
//                        System.out.println("X1: " + X1 + ", X2: " + X2 + ", Y1: " + Y1 + ", Y2: " + Y2);
                        Y2Prime = next[Y2];

                        boolean capaok;
                        boolean timeok;

                        int d1 = Qa[X1Prime] - Qa[Y1] + demands[Y1];
                        int d2 = Qa[X2Prime] - Qa[Y2] + demands[Y2];
                        double t1 = Ca[X1Prime] - Ca[Y1] + droptime;
                        double t2 = Ca[X2Prime] - Ca[Y2] + droptime;

                        if (X1 != Y1 && X2 != Y2) { //two strings exchange
                            capaok = Qb[X1 > n ? 0 : X1] + d2 + Qa[Y1Prime] <= Q && Qb[X2 > n ? 0 : X2] + d1 + Qa[Y2Prime] <= Q;
                            timeok = Cb[X1 > n ? 0 : X1] + t2 + Ca[Y1Prime] + c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] + c[Y2][Y1Prime > n ? 0 : Y1Prime] <= L
                                    && Cb[X2 > n ? 0 : X2] + t1 + Ca[Y2Prime] + c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] + c[Y1][Y2Prime > n ? 0 : Y2Prime] <= L;

                            if (capaok && timeok) {
                                neighborSolution = solution.cloneSolution();
                                neighborSolution.setNext(X1, X2Prime);
                                neighborSolution.setNext(Y2, Y1Prime);
                                neighborSolution.setNext(X2, X1Prime);
                                neighborSolution.setNext(Y1, Y2Prime);
                                gain = c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime] + c[Y1][Y1Prime > n ? 0 : Y1Prime]
                                        + c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime] + c[Y2][Y2Prime > n ? 0 : Y2Prime]
                                        - c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] - c[Y2][Y1Prime > n ? 0 : Y1Prime]
                                        - c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] - c[Y1][Y2Prime > n ? 0 : Y2Prime];
                            } else {
                                continue;
                            }
                        } else {
                            if (X1 == Y1) {
                                capaok = Qb[X1 > n ? 0 : X1] + d2 + Qa[Y1Prime] <= Q;
                                timeok = Cb[X1 > n ? 0 : X1] + t2 + Ca[Y1Prime] + c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] + c[Y2][Y1Prime > n ? 0 : Y1Prime] <= L;

                                if (capaok && timeok) {
                                    neighborSolution = solution.cloneSolution();
                                    neighborSolution.setNext(X1, X2Prime);
                                    neighborSolution.setNext(Y2, Y1Prime);
                                    neighborSolution.setNext(X2, Y2Prime);
                                    gain = c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime] + c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime]
                                            + c[Y2][Y2Prime > n ? 0 : Y2Prime] - c[X2 > n ? 0 : X2][Y2Prime > n ? 0 : Y2Prime]
                                            - c[X1 > n ? 0 : X1][X2Prime > n ? 0 : X2Prime] - c[Y2][X1Prime > n ? 0 : X1Prime];

                                } else {
                                    continue;
                                }

                            } else { //X2 == Y2
                                capaok = Qb[X2 > n ? 0 : X2] + d1 + Qa[Y2Prime] <= Q;
                                timeok = Cb[X2 > n ? 0 : X2] + t1 + Ca[Y2Prime] + c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] + c[Y1][Y2Prime > n ? 0 : Y2Prime] <= L;

                                if (capaok && timeok) {
                                    neighborSolution = solution.cloneSolution();
                                    neighborSolution.setNext(X2, X1Prime);
                                    neighborSolution.setNext(Y1, Y2Prime);
                                    neighborSolution.setNext(X1, Y1Prime);
                                    gain = c[X2 > n ? 0 : X2][X2Prime > n ? 0 : X2Prime] + c[X1 > n ? 0 : X1][X1Prime > n ? 0 : X1Prime]
                                            + c[Y1][Y1Prime > n ? 0 : Y1Prime] - c[X1 > n ? 0 : X1][Y1Prime > n ? 0 : Y1Prime]
                                            - c[X2 > n ? 0 : X2][X1Prime > n ? 0 : X1Prime] - c[Y1][X2Prime > n ? 0 : X2Prime];
                                } else {
                                    continue;
                                }

                            }
                        }

                        //at this point neighbor is found
                        if (gain > 0) { //there is an improvement
                            neighborSolution.makeItOneCircularListAgain();
                            neighborSolution.updateAfterLocalSearchModification(gain);

                            if (neighborSolution.getFitness() < bestSolution.getFitness()) {
                                bestSolution = neighborSolution;
                                if (!bi) {
                                    return bestSolution;
                                }
                            }
                        }

                    }
                }
            }
        }

        return bestSolution;

    }
}
