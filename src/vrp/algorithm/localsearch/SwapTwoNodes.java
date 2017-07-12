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
public class SwapTwoNodes extends LocalSearch {

    private int v1, v2, w1, w2, sigma, t1Star, t2Star;
    private boolean legitimacy;

    public SwapTwoNodes(Problem problem) {
        super(problem);
    }

    @Override
    public double optimize(Solution solution, boolean bi, int lambda) {
        this.solution = solution;
        this.bi = bi;
        this.lambda = lambda;
        setCommonVariables();

        improved = false;
        GStar = 0;
        for (sigma = -1; sigma < 2; sigma += 2) { //for sigma -1 and +1
            for (t1 = 1; t1 <= m; t1++) {
                v1 = prev[t1];
                w1 = next[t1];
                B1 = ((c[v1 > n ? 0 : v1][t1 > n ? 0 : t1] + c[t1 > n ? 0 : t1][w1 > n ? 0 : w1]) / 2) - (GStar / 2);
                NL = mNeighborLists[t1];
                int index = 0;
                while (NL[index].getDistance() < B1) {
                    if (sigma == 1) {
                        t2 = next[NL[index].getNo()];
                    } else {
                        t2 = prev[NL[index].getNo()];
                    }
                    
                    if (t1 > n || t2 > n) {//one of the nodes is depot
                        index++;
                        continue;
                    }
                    
                    //GEREKSİZ: Yukarısı doğruysa buraya gelmez, doğru değilse zaten depo yoktur
                    if (t1 > n && t2 > n) { //both nodes are depot, there is no need to exchange them!
                        index++;
                        continue;
                    }

                    v2 = prev[t2];
                    w2 = next[t2];
                 
                    legitimacy = (t1 != w2) && (t1 != v2) && (t2 != v1) && (t2 != w1);

                    if (!legitimacy) {
                        index++;
                        continue;
                    }

                    capaok = (tripNo[t1] == tripNo[t2]) && t1 <= n && t2 <= n;
                    if (!capaok) {
                        if (t1 > n) {
                            capaok = Qb[v1] + demands[t2] + Qa[w1] <= Q;
                        } else if (t2 > n) {
                            capaok = Qa[w2] + demands[t1] + Qb[v2] <= Q;
                        } else {
                            capaok = (Qb[v1] + Qa[w1] + demands[t2]) <= Q && (Qa[w2] + Qb[v2] + demands[t1]) <= Q;
                        }
                    }

                    if (!capaok) {
                        index++;
                        continue;
                    }

                    if (t1 > n) {
                        capaok = Cb[v1] + c[v1 > n ? 0 : v1][t2] + c[t2][w1 > n ? 0 : w1] + dropTime + Ca[w1] <= L;
                    } else if (t2 > n) {
                        capaok = Ca[w2] + c[t1][w2 > n ? 0 : w2] + c[t1][v2 > n ? 0 : v2] + dropTime + Cb[v2] <= L;
                    } else {
                        timeok = (Cb[v1] + Ca[w1] + c[v1 > n ? 0 : v1][t2] + c[t2][w1 > n ? 0 : w1] + dropTime <= L)
                                && (Ca[w2] + Cb[v2] + c[t1][w2 > n ? 0 : w2] + c[t1][v2 > n ? 0 : v2] + dropTime <= L);
                    }

                    if (!timeok) {
                        index++;
                        break;
                    }

                    // legitimacy && capaok && timeok
                    G = c[v1 > n ? 0 : v1][t1 > n ? 0 : t1] + c[t1 > n ? 0 : t1][w1 > n ? 0 : w1]
                            + c[v2 > n ? 0 : v2][t2 > n ? 0 : t2] + c[t2 > n ? 0 : t2][w2 > n ? 0 : w2]
                            - c[v1 > n ? 0 : v1][t2 > n ? 0 : t2] - c[t2 > n ? 0 : t2][w1 > n ? 0 : w1]
                            - c[v2 > n ? 0 : v2][t1 > n ? 0 : t1] - c[t1 > n ? 0 : t1][w2 > n ? 0 : w2];

                    G = Util.applyPrecision(G, 4);

                    if (G > GStar) {
                        GStar = G;
                        t1Star = t1;
                        t2Star = t2;
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
        if (improved) { //if there is an improvement then apply it to the solution
            updateSolutionLinks();
        }
        return GStar;
    }

    @Override
    protected void updateSolutionLinks() {
        int v1Star = prev[t1Star];
        int w1Star = next[t1Star];
        int w2Star = next[t2Star];
        int v2Star = prev[t2Star];

        solution.setNext(v1Star, t2Star);
        solution.setNext(t2Star, w1Star);

        solution.setNext(v2Star, t1Star);
        solution.setNext(t1Star, w2Star);

        solution.updateAfterLocalSearchModification(GStar);
    }
}
