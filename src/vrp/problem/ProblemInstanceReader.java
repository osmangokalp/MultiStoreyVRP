/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.problem;

import vrp.problem.EuclideanCoordinate;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author osman
 */
public class ProblemInstanceReader {

    private Problem problem;
    private EuclideanCoordinate[] coordinates;  //depot + customer coordinates
    private double[][] distanceMatrix;  //distace matrix (first element is depot)
    private int[] demands;  //demands of customers
    private int numOfCustomers;
    private int vehicleCapacity;
    private int maxRouteTime;
    private int dropTime;
    private int depot;
    private double[] bestKnownOrOptimalResults = { //taken from http://www.vrp-rep.org/solutions.html, 22.10.2015
        524.61, //CMT01, opt
        835.26, //CMT02, bk
        826.14, //CMT03, bk
        1028.42, //CMT04, bk
        1291.29, //CMT05, bk
        555.43, //CMT06, bk
        909.68, //CMT07, bk
        865.94, //CMT08, bk
        1162.55, //CMT09, bk
        1395.85, //CMT10, bk
        1042.11, //CMT11, bk
        819.56, //CMT12, opt
        1541.14, //CMT13, bk
        866.37 //CMT14, bk
    };
    
    //Route (vehicle) counts of best known results. Taken from "Active-guided evolution strategies for large-scale capacitated vehicle routing problems" paper.
    private int[] routeCountOfBestResults = {5, 10, 8, 12, 16, 6, 11, 9, 14, 18, 7, 10, 11, 11}; 
    
    //File operations
    private String fileName;
    private BufferedReader br;
    private String line;
    private String[] tokens;

    public ProblemInstanceReader(String fileName) {
        this.fileName = fileName;
        depot = 0; //In CMT Instances depot is always the first node
        openFile();
        readHeader();
        readDepot();
        readCustomers();
        constructDistanceMatrix();
        roundDistanceMatrix(); //rounded up or down after four decimals
        constructProblem();
    }

    private void readCustomers() {

        demands = new int[numOfCustomers + 1];
        demands[depot] = 0;

        try {
            for (int i = 1; i <= numOfCustomers; i++) {

                line = br.readLine();
                line = line.trim();
                line = line.trim().replaceAll("\\s+", " ");
                tokens = line.split(" ");

                coordinates[i] = new EuclideanCoordinate(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
                demands[i] = Integer.parseInt(tokens[2]);

//                System.out.println("customer " + i + ":, location: " + coordinates[i].toString() + ", demand: " + demands[i]);
            }

        } catch (IOException ex) {
            System.out.println("IO Exception occurred in file " + fileName);
        }
    }

    private void readDepot() {
        coordinates = new EuclideanCoordinate[numOfCustomers + 1];
        try {
            line = br.readLine();
            line = line.trim();
            line = line.trim().replaceAll("\\s+", " ");
            tokens = line.split(" ");

            coordinates[0] = new EuclideanCoordinate(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));

//            System.out.println("depot location: " + coordinates[0].toString());

        } catch (IOException ex) {
            System.out.println("IO Exception occurred in file " + fileName);
        }
    }

    private void readHeader() {
        try {
            line = br.readLine();
            line = line.trim();
            line = line.trim().replaceAll("\\s+", " ");
            tokens = line.split(" ");

            numOfCustomers = Integer.parseInt(tokens[0]);
            vehicleCapacity = Integer.parseInt(tokens[1]);
            maxRouteTime = Integer.parseInt(tokens[2]);
            dropTime = Integer.parseInt(tokens[3]);

//            System.out.println("# of customers: " + numOfCustomers);
//            System.out.println("vehicle capacity: " + vehicleCapacity);
//            System.out.println("max route time: " + maxRouteTime);
//            System.out.println("drop time: " + dropTime);

        } catch (IOException ex) {
            System.out.println("IO Exception occurred in file " + fileName);
        }
    }

    private void openFile() {
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProblemInstanceReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void constructDistanceMatrix() {
        distanceMatrix = new double[numOfCustomers + 1][numOfCustomers + 1]; //# of customers + depot

        for (int i = 0; i <= numOfCustomers; i++) {
            for (int j = i; j <= numOfCustomers; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
                } else {
                    distanceMatrix[i][j] = coordinates[i].distanceWithAnotherCoordinate(coordinates[j]);
                    distanceMatrix[j][i] = distanceMatrix[i][j];
                }
            }
        }
    }

    public double[] getBestKnownOrOptimalResults() {
        return bestKnownOrOptimalResults;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public int[] getDemands() {
        return demands;
    }

    public int getNumOfCustomers() {
        return numOfCustomers;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public int getMaxRouteTime() {
        return maxRouteTime;
    }

    public int getDropTime() {
        return dropTime;
    }

    public int getDepot() {
        return depot;
    }

    public int[] getRouteCountOfBestResults() {
        return routeCountOfBestResults;
    }

    public void setRouteCountOfBestResults(int[] routeCountOfBestResults) {
        this.routeCountOfBestResults = routeCountOfBestResults;
    }

    public EuclideanCoordinate[] getCoordinates() {
        return coordinates;
    }

    private void roundDistanceMatrix() {
        for(int i = 0; i <= numOfCustomers; i ++) {
            for(int j = 0; j <= numOfCustomers; j ++) {
                if( i!= j) {
                    BigDecimal bd = new BigDecimal(distanceMatrix[i][j]);
                    bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
                    distanceMatrix[i][j] = bd.doubleValue();
                }
            }
        }
    }

    private void constructProblem() {
        problem = new Problem();
        
        problem.setDemands(getDemands());
        problem.setDistanceMatrix(getDistanceMatrix());
        problem.setDropTime(getDropTime());
        problem.setMaxRouteTime(getMaxRouteTime());
        problem.setNumOfCustomers(getNumOfCustomers());
        problem.setVehicleCapacity(getVehicleCapacity());
        problem.setDepot(getDepot());
        problem.setBestKnownOrOptimumResults(getBestKnownOrOptimalResults());
        problem.setRouteCountOfBestResults(getRouteCountOfBestResults());
        problem.setnNeighborLists(vrp.util.Util.constructNeighborLists(numOfCustomers, distanceMatrix));
    }
    
    public Problem getProblem() {
        return problem;
    }

}
