package com.bgu.RandomizedColoring;

/**
 * Class represents a message between two nodes. If the message is final
 * than <code>isFinal = True</code>
 */
public class ColorMessage {
    private int color;
    private boolean isFinal;

    public ColorMessage(int color, boolean isFinal){
        this.color = color;
        this.isFinal = isFinal;
    }

    public int getColor() {
        return color;
    }

    public boolean isFinal() {
        return isFinal;
    }
}
