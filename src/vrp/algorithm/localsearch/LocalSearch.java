/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.localsearch;

import vrp.problem.NeighborElement;
import vrp.problem.Problem;
import vrp.problem.Solution;

/**
 *
 * @author osman
 */
public abstract class LocalSearch {

    //algorithm parameters
    protected Solution solution; //solution that local search will be applied to
    protected boolean bi; //if true, LS will run with best improvement strategy
    protected int lambda; //string length (will only be used for StringExchange and OrOptMove local searches)
    protected boolean improved = false; //will be true if algorithm improved the solution by local search
    //algorithm variables
    protected double GStar;
    protected double G;
    protected double B1, B2;
    protected int t1;
    protected int t2;
    protected int m;
    protected int k;
    protected boolean capaok;
    protected boolean timeok;
    protected int[] next, prev;
    protected int[] tripNo;
    protected int[] Qa;
    protected int[] Qb;
    protected double[] Ca;
    protected double[] Cb;
    protected NeighborElement[][] mNeighborLists;
    protected NeighborElement[] NL;
    //problem variables
    protected Problem problem;
    protected int n;
    protected double[][] c; //distanceMatrix
    protected int L; //max route time
    protected int Q; //vehicle capacity
    protected int[] demands; //index zero is the depot (no demand)
    protected int dropTime; //service time

    public LocalSearch(Problem problem) {
        super();
        this.problem = problem;
        n = problem.getNumOfCustomers();
        c = problem.getDistanceMatrix();
        L = problem.getMaxRouteTime();
        Q = problem.getVehicleCapacity();
        demands = problem.getDemands();
        dropTime = problem.getDropTime();
    }

    public void setCommonVariables() {
        this.mNeighborLists = solution.getmNeighborLists();
        this.next = solution.getNext();
        this.prev = solution.getPrev();
        this.tripNo = solution.getTripNo();
        this.Qa = solution.getQa();
        this.Qb = solution.getQb();
        this.Ca = solution.getCa();
        this.Cb = solution.getCb();
        this.m = solution.getM();
        this.k = solution.getK();
    }

    public abstract double optimize(Solution solution, boolean bi, int lambda);

    protected abstract void updateSolutionLinks();

}
