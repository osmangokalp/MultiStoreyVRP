/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildingmodel;

import java.util.ArrayList;

/**
 *
 * @author gokalp
 */
public class Storey {

    private int no = -1;
    private int width;
    private int height;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> connectionNodes = new ArrayList<>();

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addConnectionNode(Node connectionNode) {
        connectionNodes.add(connectionNode);
    }

    public void removeConnectionNode(Node connectionNode) {
        connectionNodes.remove(connectionNode);
    }

    public ArrayList<Node> getConnectionNodes() {
        return connectionNodes;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getNo() {
        return no;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getConnectionNodeCount() {
        return connectionNodes.size();
    }

    public Node findConnectionNode(int ID) {
        Node connectionNode = null;
        for (Node node : connectionNodes) {
            if (node.getID() == ID) {
                return node;
            }
        }
        return connectionNode;
    }

    public Node findNodeWithAuxilaryID(int auxID) {

        for (Node node : nodes) {
            if (node.getAuxilaryID() == auxID) {
                return node;
            }
        }
        
        for (Node node : connectionNodes) {
            if (node.getAuxilaryID() == auxID) {
                return node;
            }
        }

        return null;
    }
}
