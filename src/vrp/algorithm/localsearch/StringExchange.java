/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.localsearch;

import vrp.problem.Problem;
import vrp.problem.Solution;
import vrp.util.Util;

/**
 *
 * @author osman
 */
public class StringExchange extends LocalSearch {

    private int t3, t4, t5, t6, t7, t8, t1Star, t3Star, t5Star, t7Star, sigmaStar;
    private int sigma;
    private int[] order;
    private int demandOft2t6String, demandOft8t4String;
    private double timeOft2t6String, timeOft8t4String;

    public StringExchange(Problem problem) {
        super(problem);
    }

    @Override
    public double optimize(Solution solution, boolean bi, int lambda) {
        this.solution = solution;
        this.bi = bi;
        this.lambda = lambda;
        setCommonVariables();
        calculateOrders();

        improved = false;
        GStar = 0;

        for (sigma = -1; sigma < 2; sigma += 2) { //for sigma -1 and +1
            for (t1 = 1; t1 <= m; t1++) {
                if (sigma == 1) {
                    t2 = next[t1];
                } else { //sigma == -1
                    t2 = prev[t1];
                }

                if (t2 > n) {
                    continue; //exchanged strings have to consists of customer nodes only
                }

                B1 = c[t1 > n ? 0 : t1][t2] - (GStar / 4);
                NL = mNeighborLists[t2];

                int index = 0;
                while (NL[index].getDistance() < B1) { //Loop t3
                    t3 = NL[index].getNo();

                    if (t3 == t1) { //t values can not be same
                        index++;
                        continue;
                    }

                    if (sigma == 1) {
                        t4 = prev[t3];
                    } else { //sigma == -1
                        t4 = next[t3];
                    }

                    if (t4 > n) {
                        index++;
                        continue; //exchanged strings have to consists of customer nodes only
                    }

                    if (t4 == t1 || t4 == t2) { //t values can not be same
                        index++;
                        continue;
                    }

                    B2 = c[t1 > n ? 0 : t1][t2] - c[t2][t3 > n ? 0 : t3] + c[t3 > n ? 0 : t3][t4] - c[t4][t1 > n ? 0 : t1] - (GStar / 2);
                    
                    if (B2 > 0) {
                        if (sigma == 1) {
                            t5 = next[t1]; //i1 + sigma
                            for (int i = 0; i < lambda; i++) { //Loop i3
                                t5 = next[t5]; //i1 + sigma + (i + 1)sigma
                                t6 = prev[t5];

                                if (t6 > n) {
                                    break; //exchanged strings have to consists of customer nodes only
                                }

                                if (t5 == t3 || t5 == t4) { //t values can not be same
                                    continue;
                                }

                                if (t6 == t3 || t6 == t4) { //t values can not be same
                                    continue;
                                }

                                t7 = prev[t3]; //i2 - sigma
                                for (int j = 0; j < lambda; j++) { //Loop i4
                                    t7 = prev[t7]; //i2 - sigma - (j + 1)sigma
                                    t8 = next[t7];

                                    if (t8 > n) {
                                        break; //exchanged strings have to consists of customer nodes only
                                    }

                                    if (t7 == t5 || t7 == t6 || t7 == t2 || t7 == t1) {
                                        continue;
                                    }

                                    if (t8 == t5 || t8 == t6 || t8 == t2 || t8 == t1) {
                                        continue;
                                    }

                                    if (!(order[t8] > order[t6] || order[t2] > order[t4])) { //intersection of strings!
                                        continue;
                                    }

                                    if (tripNo[t2] == tripNo[t4]) { //both strings are in the same tour
                                        capaok = true;
                                    } else {
                                        //check the capacity constraint
                                        demandOft2t6String = Qa[t2] - Qa[t6] + demands[t6];
                                        demandOft8t4String = Qa[t8] - Qa[t4] + demands[t4];

                                        capaok = (Qb[t1] + demandOft8t4String + Qa[t5]) <= Q
                                                && (Qa[t3] + demandOft2t6String + Qb[t7]) <= Q;
                                    }
                                    if (!capaok) {
                                        continue;
                                    }

                                    if (tripNo[t2] == tripNo[t4]) { //both strings belongs to the same tour
                                        //because if there is a positive gain, then tour length will become smaller than earlier.                                     
                                        //Otherwise, if the gain will be negative, it will not accepted.
                                        timeok = true;
                                    } else {
                                        //check the max route time constraint
                                        timeOft2t6String = Ca[t2] - Ca[t6] + dropTime;
                                        timeOft8t4String = Ca[t8] - Ca[t4] + dropTime;

                                        timeok = (Cb[t1] + timeOft8t4String + Ca[t5] + c[t1 > n ? 0 : t1][t8] + c[t4][t5 > n ? 0 : t5]) <= L
                                                && (Ca[t3] + timeOft2t6String + Cb[t7] + c[t3 > n ? 0 : t3][t6] + c[t2][t7 > n ? 0 : t7]) <= L;
                                    }

                                    if (!timeok) {
                                        continue;
                                    }

                                    //stringok && capaok && timeok
                                    G = c[t1 > n ? 0 : t1][t2] + c[t5 > n ? 0 : t5][t6] + c[t3 > n ? 0 : t3][t4] + c[t7 > n ? 0 : t7][t8]
                                            - c[t6][t3 > n ? 0 : t3] - c[t8][t1 > n ? 0 : t1] - c[t2][t7 > n ? 0 : t7] - c[t4][t5 > n ? 0 : t5];

                                    G = Util.applyPrecision(G, 4);

                                    if (G > GStar) {
                                        GStar = G;
                                        t1Star = t1;
                                        t3Star = t3;
                                        t5Star = t5;
                                        t7Star = t7;
                                        sigmaStar = sigma;
                                        improved = true;
                                        if (!bi) {
                                            updateSolutionLinks();
                                            return GStar;
                                        }
                                    }

                                }
                            }
                        } else { //sigma == -1
                            t5 = prev[t1]; //i1 + sigma
                            for (int i = 0; i < lambda; i++) { //Loop i3
                                t5 = prev[t5]; //i1 + sigma + (i + 1)sigma
                                t6 = next[t5];

                                if (t6 > n) {
                                    break; //exchanged strings have to consists of customer nodes only
                                }

                                if (t5 == t3 || t5 == t4 || t6 == t3 || t6 == t4) { //t values can not be same
                                    continue;
                                }

                                t7 = next[t3]; //i2 - sigma
                                for (int j = 0; j < lambda; j++) { //Loop i4
                                    t7 = next[t7]; //i2 - sigma - (j + 1)sigma
                                    t8 = prev[t7];

                                    if (t8 > n) {
                                        break; //exchanged strings have to consists of customer nodes only
                                    }

                                    if (t7 == t5 || t7 == t6 || t7 == t2 || t7 == t1) {
                                        continue;
                                    }

                                    if (t8 == t5 || t8 == t6 || t8 == t2 || t8 == t1) {
                                        continue;
                                    }

                                    if (!(order[t4] > order[t2] || order[t6] > order[t8])) { //intersection of strings!
                                        continue;
                                    }

                                    if (tripNo[t2] == tripNo[t4]) { //both strings are in the same tour
                                        capaok = true;
                                    } else {
                                        //check the capacity constraint
                                        demandOft2t6String = Qa[t6] - Qa[t2] + demands[t2];
                                        demandOft8t4String = Qa[t4] - Qa[t8] + demands[t8];

                                        capaok = (Qb[t5] + demandOft8t4String + Qa[t1]) <= Q
                                                && (Qa[t7] + demandOft2t6String + Qb[t3]) <= Q;
                                    }
                                    if (!capaok) {
                                        continue;
                                    }

                                    if (tripNo[t2] == tripNo[t4]) { //both strings belongs to the same tour
                                        //because if there is a positive gain, then tour length will become smaller than earlier.                                     
                                        //Otherwise, if the gain will be negative, it will not accepted.
                                        timeok = true;
                                    } else {

                                        //check the max route time constraint
                                        timeOft2t6String = Ca[t6] - Ca[t2] + dropTime;
                                        timeOft8t4String = Ca[t4] - Ca[t8] + dropTime;

                                        timeok = (Cb[t5] + timeOft8t4String + Ca[t1] + c[t1 > n ? 0 : t1][t8] + c[t4][t5 > n ? 0 : t5]) <= L
                                                && (Ca[t7] + timeOft2t6String + Cb[t3] + c[t3 > n ? 0 : t3][t6] + c[t2][t7 > n ? 0 : t7]) <= L;
                                    }

                                    if (!timeok) {
                                        continue;
                                    }

                                    //stringok && capaok && timeok
                                    G = c[t1 > n ? 0 : t1][t2] + c[t5 > n ? 0 : t5][t6] + c[t3 > n ? 0 : t3][t4] + c[t7 > n ? 0 : t7][t8]
                                            - c[t6][t3 > n ? 0 : t3] - c[t8][t1 > n ? 0 : t1] - c[t2][t7 > n ? 0 : t7] - c[t4][t5 > n ? 0 : t5];

                                    G = Util.applyPrecision(G, 4);

                                    if (G > GStar) {
                                        GStar = G;
                                        t1Star = t1;
                                        t3Star = t3;
                                        t5Star = t5;
                                        t7Star = t7;
                                        sigmaStar = sigma;
                                        improved = true;
                                        if (!bi) {
                                            updateSolutionLinks();
                                            return GStar;
                                        }
                                    }

                                }
                            }
                        }
                    }

                    index++;
                }
            }
        }
        if (improved) { //if there is an improvement then apply it to the solution
            updateSolutionLinks();
        }
        return GStar;
    }

    @Override
    protected void updateSolutionLinks() {
        int t2Star, t4Star, t6Star, t8Star;

//        System.out.println("sigma: " + sigmaStar + ", t1: " + t1Star + ", t3: " + t3Star + ", t5: " + t5Star + ", t7: " + t7Star);
//        System.out.println("Before update");
//        solution.printTours();
//        System.out.println("check: " + solution.checkSolution(0));
        
        if (sigmaStar == 1) {
            t2Star = next[t1Star];
            t4Star = prev[t3Star];
            t6Star = prev[t5Star];
            t8Star = next[t7Star];
            
//            System.out.println("order t2: " + order[t2Star] + ", order t4: " + order[t4Star] + ", order t6: " + order[t6Star] + ", order t8: " + order[t8Star]);
//            System.out.println("t2: " + t2Star + ", t4: " + t4Star + ", t6: " + t6Star + ", t8: " + t8Star);

            solution.setNext(t1Star, t8Star);
            solution.setNext(t4Star, t5Star);
            solution.setNext(t7Star, t2Star);
            solution.setNext(t6Star, t3Star);

        } else { //sigmastar == -1
            t2Star = prev[t1Star];
            t4Star = next[t3Star];
            t6Star = next[t5Star];
            t8Star = prev[t7Star];
//            System.out.println("order t2: " + order[t2Star] + ", order t4: " + order[t4Star] + ", order t6: " + order[t6Star] + ", order t8: " + order[t8Star]);
//            System.out.println("t2: " + t2Star + ", t4: " + t4Star + ", t6: " + t6Star + ", t8: " + t8Star);

            solution.setNext(t3Star, t6Star);
            solution.setNext(t2Star, t7Star);
            solution.setNext(t5Star, t4Star);
            solution.setNext(t8Star, t1Star);

        }
        
        solution.updateAfterLocalSearchModification(GStar);
        
//        System.out.println("After update");
//        solution.printTours();
//        System.out.println("check: " + solution.checkSolution(0));
    }

    private void calculateOrders() {
        order = new int[solution.getM() + 1];
        int start = n + 1;
        order[start] = 0;
        int temp = next[start];
        int index = 1;
        while (temp != start) {
            order[temp] = index++;
            temp = next[temp];
        }
    }
}
