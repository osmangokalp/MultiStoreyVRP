package vrp.algorithm.metaheuristics;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import vrp.algorithm.constructionheuristic.CWHParallel;
import vrp.algorithm.constructionheuristic.ConstructionHeuristic;
import vrp.algorithm.localsearch.Classical2Opt;
import vrp.algorithm.localsearch.CrossoverMove;
import vrp.algorithm.localsearch.LocalSearch;
import vrp.algorithm.localsearch.OrOptMove;
import vrp.algorithm.localsearch.StringExchange;
import vrp.algorithm.localsearch.StringExchangeWithInversion;
import vrp.algorithm.localsearch.SwapTwoNodes;
import vrp.algorithm.neighborhoodstructures.CROSSExchange;
import vrp.algorithm.neighborhoodstructures.NeighborhoodStructure;
import vrp.problem.Problem;
import vrp.problem.Solution;
import vrp.util.TerminationCondition;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

import java.util.ArrayList;
import java.util.Random;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;


/**
 *
 * @author user
 */
public class ILS {

    private Solution s; //current solution
    private int maxIter; //maximum number of algorithm iterations
    private int maxElapsedTime;
    private int maxNumOfLSCall;
    private int maxNumOfIndividualLSCall;
    private final Random random; //random number generator
    private ArrayList<LocalSearch> localSearches;
    private ConstructionHeuristic ch;
    private NeighborhoodStructure ns;

    //ILS parameter
    private int pSize = -1; //perturbation size

    //local search parameters
    private boolean bi = false;
    private int lambda = 3;

    //for controlling progress bar
    JProgressBar progressBar;

    //For observations
    //private SolutionHashTable sht;
    private int lastImprovedIteration;
    private long lastImprovedTime;
    private int lastImprovedLSNumber;
    private int iterCounter = 0;
    private int numOfLSCall = 0;
    private int numOfIndividualLSCall = 0;
    private long elapsedTime = 0; //milliseconds
    private long startTime;

    public ILS(Problem problem, TerminationCondition termCon, int pSize, int lambda, boolean bi, Random random, JProgressBar pBar) {
        this.maxIter = termCon.getMaxIter();
        this.maxElapsedTime = termCon.getMaxTime(); //seconds
        this.maxNumOfLSCall = termCon.getMaxNumberOfLSCall();
        this.maxNumOfIndividualLSCall = termCon.getMaxNumberOfIndividualfLSCall();
        this.random = random;
        this.progressBar = pBar;
        this.pSize = pSize;
        this.lambda = lambda;
        this.bi = bi;
        ch = new CWHParallel(problem);
        ns = new CROSSExchange();

        localSearches = new ArrayList<>(5);
        localSearches.add(new Classical2Opt(problem));
        localSearches.add(new CrossoverMove(problem));
        localSearches.add(new SwapTwoNodes(problem));
        localSearches.add(new OrOptMove(problem));
        localSearches.add(new StringExchange(problem));
        localSearches.add(new StringExchangeWithInversion(problem));

    }

    public Solution solve() {
        startTime = System.nanoTime();

        s = ch.constructSolution(); //create initial solution
        s = localSearch(s, bi, lambda); //apply local search

        while (iterCounter < maxIter && numOfLSCall < maxNumOfLSCall && elapsedTime < maxElapsedTime * 1000 && numOfIndividualLSCall < maxNumOfIndividualLSCall) {
            iterCounter++;

            Solution s1 = shakingProcedure(s, pSize);

            Solution s2 = localSearch(s1, bi, lambda);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            });

            elapsedTime = (System.nanoTime() - startTime) / 1000000;
            if (s2.getFitness() < s.getFitness()) {
                s = s2;
                lastImprovedIteration = iterCounter;
                lastImprovedTime = elapsedTime;
                lastImprovedLSNumber = numOfLSCall;

            }

            if (elapsedTime >= maxElapsedTime * 1000 || numOfLSCall >= maxNumOfLSCall) {
                break; //exit from while loop
            }
        }

        return s;
    }

    private Solution localSearch(Solution s1, boolean bi, int lambda) {
        numOfLSCall++;
        Solution s2 = s1.cloneSolution();

        boolean improved;
        int n = localSearches.size();

        do {
            improved = false;

            //monitoring phase
            for (int i = 0; i < n; i++) {
                LocalSearch ls = localSearches.get(i);

                double GStar = ls.optimize(s2, bi, lambda);
                numOfIndividualLSCall++;
                if (GStar > 0) {
                    improved = true;
                }
            }
            if (!improved) {
                break;
            }

        } while (true);
        return s2;
    }

    private Solution shakingProcedure(Solution s, int n) {
        return ns.generateRandomNeighbor(s, n, random);
    }

    public int getLastImprovedIteration() {
        return lastImprovedIteration;
    }

    public long getLastImprovedTime() {
        return lastImprovedTime;
    }

    public int getLastImprovedLSNumber() {
        return lastImprovedLSNumber;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public int getNumOfLSCall() {
        return numOfLSCall;
    }

    public int getIterCounter() {
        return iterCounter;
    }

    public int getNumOfIndividualLSCall() {
        return numOfIndividualLSCall;
    }

}
