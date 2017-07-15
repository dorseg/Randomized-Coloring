package com.bgu.Algorithm1;

/**
 * Created by dorse on 14/07/2017.
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
