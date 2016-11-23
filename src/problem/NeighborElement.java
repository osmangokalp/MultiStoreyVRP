/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package problem;

/**
 *
 * @author osman This class represents the neighbor of each customer or depot
 * node. It holds the neighbor no along with the distance from it.
 */
public class NeighborElement {

    private int no;
    private double distance;

    public NeighborElement(int no, double distance) {
        this.no = no;
        this.distance = distance;
    }

    public int getNo() {
        return no;
    }

    public double getDistance() {
        return distance;
    }

}
