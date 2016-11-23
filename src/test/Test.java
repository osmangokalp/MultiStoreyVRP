/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import algorithm.floyds.FloydsDistanceMatrixConstructor;
import java.util.ArrayList;

/**
 *
 * @author gokalp
 */
public class Test {

    public static void main(String[] args) {
        double[][] matrix = {
            {0, 5, Double.POSITIVE_INFINITY, 7, 3, 1},
            {5, 0, 4, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1},
            {Double.POSITIVE_INFINITY, 4, 0, 2, Double.POSITIVE_INFINITY, 1},
            {7, Double.POSITIVE_INFINITY, 2, 0, 3, Double.POSITIVE_INFINITY},
            {3, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 3, 0, Double.POSITIVE_INFINITY},
            {1, 1, 1, 50, Double.POSITIVE_INFINITY, 0}
        };
        FloydsDistanceMatrixConstructor fdmc = new FloydsDistanceMatrixConstructor();
        double[][] D = fdmc.constructDistanceMatrix(matrix, 0);

        for (int i = 0; i < D.length; i++) {
            for (int j = 0; j < D.length; j++) {
                System.out.print("\t" + D[i][j]);
            }
            System.out.println("");
        }
        
        ArrayList<Integer> path = fdmc.getShortestPath(0, 3);
        System.out.println("\nPath from 0 to 3: ");
        for(int i = 0; i < path.size(); i ++) {
            System.out.print(path.get(i));
            if(i < path.size() - 1) {
                System.out.print(" -> ");
            }
        }
        System.out.println("");
    }
}
