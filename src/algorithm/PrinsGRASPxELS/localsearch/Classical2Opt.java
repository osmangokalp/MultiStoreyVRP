/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.PrinsGRASPxELS.localsearch;

import problem.Problem;
import problem.Solution;
import util.Util;

/**
 *
 * @author osman
 */
public class Classical2Opt extends LocalSearch {

    private int sigma, t3, t4, t1Star, t3Star, sigmaStar;

    public Classical2Opt(Problem problem) {
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
                if (sigma == -1) {
                    t2 = prev[t1];
                } else {
                    t2 = next[t1];
                }

                B1 = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2] - GStar / 2; //In distance matrix depot is represented by zero, but in circular list depots are n + k
                NL = mNeighborLists[t2];
                int index = 0;
                while (NL[index].getDistance() < B1) {
                    t3 = NL[index].getNo();

                    if (sigma == -1) {
                        t4 = next[t3];
                    } else {
                        t4 = prev[t3];
                    }

                    //if one of the segments does not contain a depot (i.e. both removed edges belong to the same tour)
                    capaok = ((tripNo[t1] == tripNo[t3]) && t1 <= n && t3 <= n) || ((tripNo[t2] == tripNo[t4]) && t2 <= n && t4 <= n);

                    if (t4 == t1 || t4 == t2) {
                        index++;
                        continue;
                    }

                    if (!capaok) { //both segments contains depot
                        if (sigma == 1) {
                            capaok = ((Qb[t1] + Qb[t4] <= Q) && (Qa[t3] + Qa[t2] <= Q));
                        } else { //sigma == -1
                            capaok = ((Qb[t2] + Qb[t3] <= Q) && (Qa[t1] + Qa[t4] <= Q));
                        }
                    }
                    if (!capaok) {
                        index++;
                        continue;
                    }

                    if (sigma == 1) {
                        timeok = (Cb[t1] + Cb[t4] + c[t1 > n ? 0 : t1][t4 > n ? 0 : t4]) <= L
                                && (Ca[t3] + Ca[t2] + c[t3 > n ? 0 : t3][t2 > n ? 0 : t2]) <= L;
                    } else { //sigma == -1
                        timeok = (Cb[t2] + Cb[t3] + c[t2 > n ? 0 : t2][t3 > n ? 0 : t3]) <= L
                                && (Ca[t4] + Ca[t1] + c[t4 > n ? 0 : t4][t1 > n ? 0 : t1]) <= L;
                    }

                    if (!timeok) {
                        index++;
                        continue;
                    }

                    // capaok && timeok
                    G = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2]
                            - c[t2 > n ? 0 : t2][t3 > n ? 0 : t3]
                            + c[t3 > n ? 0 : t3][t4 > n ? 0 : t4]
                            - c[t4 > n ? 0 : t4][t1 > n ? 0 : t1];

                    G = Util.applyPrecision(G, 4);
                    if (G > GStar) {
                        GStar = G;
                        t1Star = t1;
                        t3Star = t3;
                        sigmaStar = sigma;
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

        int t2Star, t4Star;
        if (sigmaStar == 1) {
            t2Star = next[t1Star];
            t4Star = prev[t3Star];
        } else { //sigmastar == -1
            t2Star = prev[t1Star];
            t4Star = next[t3Star];
        }
        if (sigmaStar == 1) {
            solution.invertSegment(t3Star, t1Star, t2Star, t4Star);
        } else { //sigmastar == -1
            solution.invertSegment(t4Star, t2Star, t1Star, t3Star);
        }

        solution.updateAfterLocalSearchModification(GStar);
    }

}
