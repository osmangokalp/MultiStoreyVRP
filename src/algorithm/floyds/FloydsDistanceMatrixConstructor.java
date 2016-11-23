/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm.floyds;

import java.util.ArrayList;

/**
 *
 * @author gokalp
 */
public class FloydsDistanceMatrixConstructor {

    private int[][] P;
    ArrayList<Integer> path;

    /**
     * 
     * @param D is expected that its diagonal elements are zero and unconnected
     * nodes has inifinite weight.
     * @param nodeCount is the number of nodes in the matrix.
     * @return distance matrix that holds all pairs shortest path distances.
     */
    public double[][] constructDistanceMatrix(double[][] D, int nodeCount) {
        int size = D.length;
        this.P = new int[size][size];

        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == k || j == k) continue;
                    if ( D[i][j] > D[i][k] + D[k][j]) {
                        D[i][j] = D[i][k] + D[k][j];
                        P[i][j] = k;
                    }
                }
            }
        }

        //exclude connection nodes and left only real nodes
        double[][] distancematrix = new double[nodeCount][nodeCount];
        for(int i = 0; i < nodeCount; i ++) {
            System.arraycopy(D[i], 0, distancematrix[i], 0, nodeCount);
        }
        
        return distancematrix;
    }

    /**
     * 
     * @param i is the source node.
     * @param j is the end node.
     * @return intermediate node/nodes between @param i and @param j or null if
     * there is not any intermediate node/nodes.
     */
    public ArrayList<Integer> getShortestPath(int i, int j) {
        path = new ArrayList<>();
        path.add(i);
        findPath(i, j);
        path.add(j);
        
        return path;
    }

    private void findPath(int q, int r) {
         if(P[q][r] != 0) {
             findPath(q, P[q][r]);
             path.add(P[q][r]);
             findPath(P[q][r], r);
         } 
    }
}
