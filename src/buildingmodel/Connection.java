/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildingmodel;

/**
 *
 * @author gokalp
 */
public class Connection {

    private Node node1;
    private Node node2;
    private double weight;

    public Connection(Node node1, Node node2, double weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public double getWeight() {
        return weight;
    }

    public void updateConnection(Connection newConnection) {
        //update node1
        node1.changeEuclideanCoordinate(newConnection.getNode1().getEuclideanCoordinate());
        node1.changeStoreyOfConnectionNode(newConnection.getNode1().getStorey());

        //update node2
        node2.changeEuclideanCoordinate(newConnection.getNode2().getEuclideanCoordinate());
        node2.changeStoreyOfConnectionNode(newConnection.getNode2().getStorey());
        
        //update weight
        this.weight = newConnection.getWeight();
    }

    @Override
    public String toString() {
        return "Weight: " + weight + ", Storeys: " + node1.getStorey().getNo() + "-" + node2.getStorey().getNo() + ", Connection Nodes: " + node1.getID() + "-" + node2.getID();
    }

}
