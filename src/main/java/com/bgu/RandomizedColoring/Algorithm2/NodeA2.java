package com.bgu.RandomizedColoring.Algorithm2;

import com.bgu.RandomizedColoring.Experiments;
import com.bgu.RandomizedColoring.Main;
import com.bgu.RandomizedColoring.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NodeA2 extends Node {

    public NodeA2(int id){
        super(id);
    }

    public boolean checkColor(){
        color = new Random().nextInt(2); // drawn color from [0,1]
        if (color == 0){
            Experiments.phaser.arriveAndAwaitAdvance(); // discard color and continue to the next round
            return true;
        }
        return false;
    }

    // Chose from [1,...,DELTA+1]\F_v
    public int chooseColor(){
        List<Integer> colors = new ArrayList<>();
        for (int i = 1; i<= Experiments.DELTA+1; i++){
            if (!finalColors.contains(i)) // add color that not in F_v
                colors.add(i);
        }
        int colorIndex = new Random().nextInt(colors.size());
        return colors.get(colorIndex);
    }

}
