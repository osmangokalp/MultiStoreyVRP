/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.PrinsGRASPxELS;

import java.util.Random;

/**
 *
 * @author osman
 */
public class ParameterList {

    private Random random;

    private int pMin, pMax;
    private int np, ni, nc;
    private double beta;

    //local search
    private int lambda;
    private boolean bi;

    public int getLambda() {
        return lambda;
    }

    public boolean isBi() {
        return bi;
    }

    public int getpMin() {
        return pMin;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public void setBi(boolean bi) {
        this.bi = bi;
    }

    public void setpMin(int pMin) {
        this.pMin = pMin;
    }

    public int getpMax() {
        return pMax;
    }

    public void setpMax(int pMax) {
        this.pMax = pMax;
    }

    public int getNp() {
        return np;
    }

    public void setNp(int np) {
        this.np = np;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public int getNc() {
        return nc;
    }

    public void setNc(int nc) {
        this.nc = nc;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

}
