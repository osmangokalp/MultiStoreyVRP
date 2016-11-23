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
public class Building {
    private int storeyCount;
    private ArrayList<Storey> storeys = new ArrayList<>();

    public Building(int storeyCount) {
        this.storeyCount = storeyCount;
    }
    
}
