/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.util;

/**
 *
 * @author user
 */
public class TerminationCondition {
    private int maxTime; //in seconds
    private int maxIter;
    private int maxNumberOfLSCall;
    private int maxNumberOIndividualfLSCall;

    public TerminationCondition(int maxTime, int maxIter, int maxNumberOfLSCall, int maxNumberOIndividualfLSCall) {
        this.maxTime = maxTime;
        this.maxIter = maxIter;
        this.maxNumberOfLSCall = maxNumberOfLSCall;
        this.maxNumberOIndividualfLSCall = maxNumberOIndividualfLSCall;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public int getMaxNumberOfLSCall() {
        return maxNumberOfLSCall;
    }

    public int getMaxNumberOfIndividualfLSCall() {
        return maxNumberOIndividualfLSCall;
    }

}
