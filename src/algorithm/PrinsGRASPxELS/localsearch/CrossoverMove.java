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
public class CrossoverMove extends LocalSearch {

    private int t3, t4, t1Star, t3Star;
    private boolean t2t3ok, t1t4ok;

    public CrossoverMove() {
        super();
    }

    @Override
    public double optimize(Solution solution, boolean bi, int lambda) {
        this.solution = solution;
        this.bi = bi;
        this.lambda = lambda;
        setCommonVariables();

        improved = false;
        GStar = 0;
        for (t1 = 1; t1 <= m; t1++) {
            t2 = next[t1];
            B1 = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2] - GStar / 2; //In distance matrix depot is represented by zero, but in circular list depots are > n
            NL = mNeighborLists[t2];
            int index = 0;
            while (NL[index].getDistance() < B1) {

                t3 = NL[index].getNo();
                t4 = next[t3];

                t2t3ok = (t2 > n) || (t3 > n) || (tripNo[t2] != tripNo[t3]);
                if (!t2t3ok) {
                    index++;
                    continue;
                }

                t1t4ok = (t1 > n) || (t4 > n) || (tripNo[t1] != tripNo[t4]);
                if (!t1t4ok) {
                    index++;
                    continue;
                }

                capaok = (Qb[t1] + Qa[t4] <= Q) && (Qb[t3] + Qa[t2] <= Q);
                if (!capaok) {
                    index++;
                    continue;
                }

                timeok = ((Cb[t1] + c[t1 > n ? 0 : t1][t4 > n ? 0 : t4] + Ca[t4]) <= L) && ((Cb[t3] + c[t3 > n ? 0 : t3][t2 > n ? 0 : t2] + Ca[t2]) <= L);
                if (!timeok) {
                    index++;
                    continue;
                }

                // t1t4ok && t2t3ok && capaok && timeok
                G = c[t1 > n ? 0 : t1][t2 > n ? 0 : t2]
                        - c[t2 > n ? 0 : t2][t3 > n ? 0 : t3]
                        + c[t3 > n ? 0 : t3][t4 > n ? 0 : t4]
                        - c[t4 > n ? 0 : t4][t1 > n ? 0 : t1];

                G = Util.applyPrecision(G, 2);

                if (G > GStar) {
                    GStar = G;
                    t1Star = t1;
                    t3Star = t3;
                    improved = true;
                    if (!bi) {
                        updateSolutionLinks();
                        return GStar;
                    }
                }

                index++;
            }
        }
        if (improved) { //if there is an improvement then apply it to the solution
            updateSolutionLinks();
        }
        return GStar;
    }

    @Override
    protected void updateSolutionLinks() {

        int t2Star = next[t1Star];
        int t4Star = next[t3Star];

        solution.setNext(t1Star, t4Star);
        solution.setNext(t3Star, t2Star);

        makeItOneCircularListAgain();
        solution.updateAfterLocalSearchModification(GStar);

    }

    /**
     * Because crossover move split tour into two pieces, it must be 
     * converted to the one circular list before proceed to the other local searches.
     */
    private void makeItOneCircularListAgain() {
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
        
        s1 = next[d1];
        s2 = next[d2];
        
        solution.setNext(d2, s1);
        solution.setNext(d1, s2);
    }

}
