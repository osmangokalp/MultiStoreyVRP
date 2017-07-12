/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildingmodel;

import vrp.problem.EuclideanCoordinate;

/**
 *
 * @author gokalp
 */
public class Node {
   
    private Storey storey;
    private int ID;
    private EuclideanCoordinate euclideanCoordinate = null;
    private int auxilaryID; //will be used for distance matrix calculation
    private boolean depot = false;
    private int demand;
    
    public Node(Storey storey, EuclideanCoordinate euclideanCoordinate, int ID, int demand) {
        this.storey = storey;
        this.euclideanCoordinate = euclideanCoordinate;
        this.ID = ID;
        this.demand = demand;
    }

    public EuclideanCoordinate getEuclideanCoordinate() {
        return euclideanCoordinate;
    }

    public int getID() {
        return ID;
    }

    public Storey getStorey() {
        return storey;
    }

    public void updateNode(Storey newStorey, EuclideanCoordinate newEuclideanCoordinate, int demand) {
        changeStoreyOfNode(newStorey);
        changeEuclideanCoordinate(newEuclideanCoordinate);
        setDemand(demand);
    }
    
    public void changeStoreyOfNode(Storey storey) {
        this.storey.removeNode(this);
        storey.addNode(this);
        this.storey = storey;
    }
    
    public void changeStoreyOfConnectionNode(Storey storey) {
        this.storey.removeConnectionNode(this);
        storey.addConnectionNode(this);
        this.storey = storey;
    }

    public void changeEuclideanCoordinate(EuclideanCoordinate euclideanCoordinate) {
        this.euclideanCoordinate = euclideanCoordinate;
    }

    public int getAuxilaryID() {
        return auxilaryID;
    }

    public void setAuxilaryID(int auxilaryID) {
        this.auxilaryID = auxilaryID;
    }

    public boolean isDepot() {
        return depot;
    }

    public void setDepot(boolean depot) {
        this.depot = depot;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @Override
    public String toString() {
        return "ID: " + ID + ", X: " + euclideanCoordinate.getX() + ", Y: " + euclideanCoordinate.getY() + ", Demand: " + demand + ", Storey: " + storey.getNo();
    }

}
