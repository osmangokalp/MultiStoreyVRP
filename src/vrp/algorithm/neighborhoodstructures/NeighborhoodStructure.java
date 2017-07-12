/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.neighborhoodstructures;

import java.util.Random;
import vrp.problem.Solution;

/**
 *
 * @author user
 */
public interface NeighborhoodStructure {
    public Solution generateRandomNeighbor(Solution solution, int size, Random random);
    
}
