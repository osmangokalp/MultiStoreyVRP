/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package problem;

/**
 *
 * @author osman
 */
public class Problem {

    private int numOfCustomers; //customer count
    private int vehicleCapacity;    //vehicle capacity
    private int maxRouteTime;   //maximum route time
    private int dropTime;   //drop time
    private double[][] distanceMatrix;  //distace matrix
    private int[] demands;  //demands of customers 
    private int depot;
    private double[] bestKnownOrOptimumResults; //best known or optimum results from literature
    private int[] routeCountOfBestResults; //route counts associated with the best results

    private static volatile Problem instance = null;

    private Problem() {
    }
    
    public static synchronized Problem getInstance() {
        if (instance == null) {
            instance = new Problem();
        }

        return instance;
    }

    public int getNumOfCustomers() {
        return numOfCustomers;
    }

    public void setNumOfCustomers(int numOfCustomers) {
        this.numOfCustomers = numOfCustomers;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public int getMaxRouteTime() {
        return maxRouteTime;
    }

    public void setMaxRouteTime(int maxRouteTime) {
        this.maxRouteTime = maxRouteTime;
    }

    public int getDropTime() {
        return dropTime;
    }

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public int[] getDemands() {
        return demands;
    }

    public void setDemands(int[] demands) {
        this.demands = demands;
    }

    public int getDepot() {
        return depot;
    }

    public void setDepot(int depot) {
        this.depot = depot;
    }

    public double[] getBestKnownOrOptimumResults() {
        return bestKnownOrOptimumResults;
    }

    public void setBestKnownOrOptimumResults(double[] bestKnownOrOptimumResults) {
        this.bestKnownOrOptimumResults = bestKnownOrOptimumResults;
    }

    public int[] getRouteCountOfBestResults() {
        return routeCountOfBestResults;
    }

    public void setRouteCountOfBestResults(int[] routeCountOfBestResults) {
        this.routeCountOfBestResults = routeCountOfBestResults;
    }

}
