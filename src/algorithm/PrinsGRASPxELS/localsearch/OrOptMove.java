/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.PrinsGRASPxELS.localsearch;

import problem.Solution;
import utility.Util;

/**
 *
 * @author osman
 */
public class OrOptMove extends LocalSearch {

    private int t3, t4, t5, t6, t1Star, t3Star, t5Star;
    private int[] order;
    private int demandOft2t5String;
    double timeOft2t5String;

    public OrOptMove() {
        super();
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
        for (t1 = 1; t1 <= m; t1++) { //Loop i1
            t2 = next[t1];
            if (t2 > n) {
                continue;
            }
            B1 = c[t1 > n ? 0 : t1][t2] - (GStar / 3);
            NL = mNeighborLists[t2];
            int index = 0;
            while (NL[index].getDistance() < B1) { //Loop t3
                t3 = NL[index].getNo();
                if (t3 == t1) {
                    index++;
                    continue;
                }

                t4 = next[t3];

                if (t4 == t1) {
                    index++;
                    break;
                }

                for (int i = 0; i < lambda; i++) { //Loop i3
                    if (i == 0) {
                        t5 = next[t1]; //i1 + 1 (which is also t2)
                    } else {
                        t5 = next[t5]; //i1 + 2 ... i1 + k
                    }

                    //string has depot
                    if (t5 > n) {
                        break;
                    }

                    if (t5 == t3 || t5 == t4) {
                        break;//only changing node here is t5 and t6 (t1 and t3 is static in this for loop), so once it lost its order feasibility, it can not get it back
                    }

                    t6 = next[t5];
                    if (t6 == t3 || t6 == t4) {
                        break;//only changing node here is t5 and t6 (t1 and t3 is static in this for loop), so once it lost its order feasibility, it can not get it back
                    }

                    //check order feasibility
                    if (!((order[t1] < order[t5] && order[t5] < order[t3])
                            || (order[t5] < order[t3] && order[t3] < order[t1])
                            || (order[t3] < order[t1] && order[t1] < order[t5]))) {
                        break;//only changing node here is t5 (t1 and t3 is static in this for loop), so once it lost its order feasibility, it can not get it back
                    }

                    //re-check that string has no depot
                    if (tripNo[t2] != tripNo[t5]) {
                        break;
                    }

                    if (tripNo[t2] == tripNo[t3]) { //if relocation is in the same route, then capacity will not change.
                        capaok = true;
                    } else {
                        demandOft2t5String = Qa[t2] - Qa[t5] + demands[t5];
                        capaok = (Qb[t3] + demandOft2t5String + Qa[t4] <= Q) && (Qb[t1] + Qa[t6] <= Q);
                    }

                    if (!capaok) {
                        continue;
                    }

                    if (tripNo[t2] == tripNo[t3]) {
                        //if relocation is in the same route, then allow it to pass the time condition.
                        //Because if there is a positive gain, time constraint will not be violated.
                        //If there is negative gain, it will not be considered anyway.
                        timeok = true;
                    } else {
                        timeOft2t5String = Ca[t2] - Ca[t5] + dropTime;
                        timeok = (Cb[t3] + timeOft2t5String + Ca[t4] + c[t2][t3 > n ? 0 : t3] + c[t4 > n ? 0 : t4][t5] <= L)
                                && (Cb[t1] + Ca[t6] + c[t6 > n ? 0 : t6][t1 > n ? 0 : t1] <= L);
                    }

                    if (!timeok) {
                        continue;
                    }

                    G = c[t1 > n ? 0 : t1][t2] - c[t2][t3 > n ? 0 : t3]
                            + c[t3 > n ? 0 : t3][t4 > n ? 0 : t4] - c[t4 > n ? 0 : t4][t5]
                            + c[t5][t6 > n ? 0 : t6] - c[t6 > n ? 0 : t6][t1 > n ? 0 : t1];

                    G = Util.applyPrecision(G, 2);

                    if (G > GStar) {
                        GStar = G;
                        t1Star = t1;
                        t3Star = t3;
                        t5Star = t5;
                        improved = true;
                        if (!bi) {
                            updateSolutionLinks();
                            return GStar;
                        }
                    }
                }

                index++;
            }
        }

        for (t3 = 1; t3 <= m; t3++) { //Loop i2
            t4 = next[t3];
            B1 = c[t3 > n ? 0 : t3][t4 > n ? 0 : t4] - (GStar / 3);
            NL = mNeighborLists[t4];
            int index = 0;
            while (NL[index].getDistance() < B1) { //Loop t5
                t5 = NL[index].getNo();

                if (t5 > n || t5 == t3) {
                    index++;
                    continue;
                }

                t6 = next[t5];

                if (t6 == t3) {
                    index++;
                    continue;
                }

                for (int i = 0; i < lambda; i++) { //Loop i1
                    if (i == 0) {
                        t1 = prev[t5]; //i3 - 1
                    } else {
                        t1 = prev[t1]; //i3 - 2 ... i3 - k
                    }

                    if (t1 == t4 || t1 == t3) {
                        continue;
                    }

                    t2 = next[t1];

                    //string has depot
                    if (t2 > n) {
                        break;
                    }

                    if (t2 == t3 || t2 == t4) {
                        continue;
                    }

                    //check order feasibility
                    if (!((order[t1] < order[t5] && order[t5] < order[t3])
                            || (order[t5] < order[t3] && order[t3] < order[t1])
                            || (order[t3] < order[t1] && order[t1] < order[t5]))) {
                        continue;
                    }

                    //re-check that string has no depot
                    if (tripNo[t2] != tripNo[t5]) {
                        break;
                    }

                    if (tripNo[t2] == tripNo[t3]) { //if relocation is in the same route, then capacity will not change.
                        capaok = true;
                    } else {
                        demandOft2t5String = Qa[t2] - Qa[t5] + demands[t5];
                        capaok = (Qb[t3] + demandOft2t5String + Qa[t4] <= Q) && (Qb[t1] + Qa[t6] <= Q);
                    }

                    if (!capaok) {
                        continue;
                    }

                    if (tripNo[t2] == tripNo[t3]) {
                        //if relocation is in the same route, then allow it to pass the time condition.
                        //Because if there is a positive gain, time constraint will not be violated.
                        //If there is negative gain, it will not be considered anyway.
                        timeok = true;
                    } else {
                        timeOft2t5String = Ca[t2] - Ca[t5] + dropTime;
                        timeok = (Cb[t3] + timeOft2t5String + Ca[t4] + c[t2][t3 > n ? 0 : t3] + c[t4 > n ? 0 : t4][t5] <= L)
                                && (Cb[t1] + Ca[t6] + c[t6 > n ? 0 : t6][t1 > n ? 0 : t1] <= L);
                    }

                    if (!timeok) {
                        continue;
                    }

                    G = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2] - c[t2 > n ? 0 : t2][t3 > n ? 0 : t3]
                            + c[t3 > n ? 0 : t3][t4 > n ? 0 : t4] - c[t4 > n ? 0 : t4][t5 > n ? 0 : t5]
                            + c[t5 > n ? 0 : t5][t6 > n ? 0 : t6] - c[t6 > n ? 0 : t6][t1 > n ? 0 : t1];

                    G = Util.applyPrecision(G, 2);

                    if (G > GStar) {
                        GStar = G;
                        t1Star = t1;
                        t3Star = t3;
                        t5Star = t5;
                        improved = true;
                        if (!bi) {
                            updateSolutionLinks();
                            return GStar;
                        }
                    }
                }

                index++;
            }
        }

        for (t5 = 1; t5 <= n; t5++) { //Loop i3
            t6 = next[t5];

            for (int i = 0; i < lambda; i++) { //Loop i1
                if (i == 0) {
                    t1 = prev[t5]; //i3 - 1
                } else {
                    t1 = prev[t1]; //i3 - 2 ... i3 - k
                }

                if (c[t5][t6 > n ? 0 : t6] - c[t6 > n ? 0 : t6][t1 > n ? 0 : t1] > (GStar / 3)) {
                    t2 = next[t1];

                    //string has depot
                    if (t2 > n) {
                        break;
                    }

                    B2 = c[t5][t6 > n ? 0 : t6] - c[t6 > n ? 0 : t6][t1 > n ? 0 : t1] + c[t1 > n ? 0 : t1][t2] - ((2 * GStar) / 3);
                    NL = mNeighborLists[t2];
                    int index = 0;
                    while (index < m - 1 && NL[index].getDistance() < B1) { //Loop t3
                        t3 = NL[index].getNo();

                        if (t3 == t1 || t3 == t5 || t3 == t6) {
                            index++;
                            continue;
                        }

                        t4 = next[t3];

                        if (t4 == t1 || t4 == t2 || t4 == t5 || t4 == t6) {
                            index++;
                            continue;
                        }

                        //check order feasibility
                        if (!((order[t1] < order[t5] && order[t5] < order[t3])
                                || (order[t5] < order[t3] && order[t3] < order[t1])
                                || (order[t3] < order[t1] && order[t1] < order[t5]))) {
                            index++;
                            continue;
                        }

                        //re-check that string has no depot
                        if (tripNo[t2] != tripNo[t5]) {
                            index++;
                            break;
                        }

                        if (tripNo[t2] == tripNo[t3]) { //if relocation is in the same route, then capacity will not change.
                            capaok = true;
                        } else {
                            demandOft2t5String = Qa[t2] - Qa[t5] + demands[t5];
                            capaok = (Qb[t3] + demandOft2t5String + Qa[t4] <= Q) && (Qb[t1] + Qa[t6] <= Q);
                        }

                        if (!capaok) {
                            index++;
                            continue;
                        }

                        if (tripNo[t2] == tripNo[t3]) {
                            //if relocation is in the same route, then allow it to pass the time condition.
                            //Because if there is a positive gain, time constraint will not be violated.
                            //If there is negative gain, it will not be considered anyway.
                            timeok = true;
                        } else {
                            timeOft2t5String = Ca[t2] - Ca[t5] + dropTime;
                            timeok = (Cb[t3] + timeOft2t5String + Ca[t4] + c[t2][t3 > n ? 0 : t3] + c[t4 > n ? 0 : t4][t5] <= L)
                                    && (Cb[t1] + Ca[t6] + c[t6 > n ? 0 : t6][t1 > n ? 0 : t1] <= L);
                        }

                        if (!timeok) {
                            index++;
                            continue;
                        }

                        G = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2] - c[t2 > n ? 0 : t2][t3 > n ? 0 : t3]
                                + c[t3 > n ? 0 : t3][t4 > n ? 0 : t4] - c[t4 > n ? 0 : t4][t5 > n ? 0 : t5]
                                + c[t5 > n ? 0 : t5][t6 > n ? 0 : t6] - c[t6 > n ? 0 : t6][t1 > n ? 0 : t1];

                        G = Util.applyPrecision(G, 2);

                        if (G > GStar) {
                            GStar = G;
                            t1Star = t1;
                            t3Star = t3;
                            t5Star = t5;
                            improved = true;
                            if (!bi) {
                                updateSolutionLinks();
                                return GStar;
                            }
                        }

                        index++;
                    }
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
//        solution.printTours();
        int t2Star, t4Star, t6Star;

        t2Star = next[t1Star];
        t4Star = next[t3Star];
        t6Star = next[t5Star];

        solution.setNext(t1Star, t6Star);
        solution.setNext(t3Star, t2Star);
        solution.setNext(t5Star, t4Star);

        solution.updateAfterLocalSearchModification(GStar);

//        if (!solution.checkSolution(-1)) {
//            System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
//            System.out.println("t1Star: " + t1Star);
//            System.out.println("t2Star: " + t2Star);
//            System.out.println("t3Star: " + t3Star);
//            System.out.println("t4Star: " + t4Star);
//            System.out.println("t5Star: " + t5Star);
//            System.out.println("t6Star: " + t6Star);
//            solution.printTours();
//            System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
//        }
    }

    private void calculateOrders() {
        order = new int[solution.getM() + 1];
        int start = 1;
        order[start] = 0;
        int temp = next[start];
        int index = 1;
        while (temp != start) {
            order[temp] = index++;
            temp = next[temp];
        }
    }
}
