package com.bgu.RandomizedColoring.Algorithm1;

import com.bgu.RandomizedColoring.Experiments;
import com.bgu.RandomizedColoring.Node;

import java.util.Random;

/**
 * This class represents a node in algorithm 1 - <i><Rand-2Delta</i>
 */
public class NodeA1 extends Node {

    public NodeA1(int id){
        super(id);
    }

    @Override
    public int chooseColor() {
        return new Random().nextInt(2* Experiments.DELTA) + 1; // drawn color from [1,...,2*Delta]
    }

    @Override
    public boolean checkColor() {
        return false;
    }

}
