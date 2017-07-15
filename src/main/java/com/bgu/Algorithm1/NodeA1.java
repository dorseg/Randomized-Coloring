package com.bgu.Algorithm1;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents a node in algorithm 1 - Rand-2Delta
 */
public class NodeA1 implements Runnable {

    private int id;
    private List<NodeA1> neighbors;
    private Queue<ColorMessage> inMessages; // income message - final and not final
    private Set<Integer> tempColors; // T_v - set of temporary colors selected by neighbors of this Node.
    private Set<Integer> finalColors; // F_v - set of final colors selected by neighbors of this Node.
    private int color; // the drawn color in each round
    private boolean terminated;
    private volatile int round;

    public NodeA1(int id){
        this.id = id;
        neighbors = new LinkedList<>();
        inMessages = new ConcurrentLinkedQueue<>();
        tempColors = new HashSet<>();
        finalColors = new HashSet<>();
        color = -1;
        terminated = false;
        round = 0;
    }

    @Override
    public void run() {
        while (!terminated){
            System.out.println("Node " + id + " executes round " + round);
            tempColors.clear(); // T_v = empty set
            color = new Random().nextInt(2*Main.DELTA) + 1; // drawn color from [1,...,2*Delta]
            broadcastMessage(new ColorMessage(color, false)); // send non final message to all neighbors
            Queue<ColorMessage> copyMessages = new ConcurrentLinkedQueue(inMessages);
            for (ColorMessage msg : copyMessages){
                if (!msg.isFinal()){
                    // add message to T_v and remove it from income messages
                    tempColors.add(msg.getColor());
                    inMessages.remove(msg);
                }
            }
            if (!tempColors.contains(color) && !finalColors.contains(color)){
                // send final message to all neighbors, and terminate. The color will be the current color.
                broadcastMessage(new ColorMessage(color, true));
                terminated = true;
            }
            else {
                copyMessages = new ConcurrentLinkedQueue(inMessages);
                for (ColorMessage msg : copyMessages){
                    if (msg.isFinal()) {
                        // add message to F_v and remove it from income messages
                        finalColors.add(msg.getColor());
                        inMessages.remove(msg);
                    }
                }
            }
            System.out.println("Node " + id + " finished round " + round);
            if (terminated)
                Main.phaser.arriveAndDeregister();
            else {
                Main.phaser.arriveAndAwaitAdvance(); // wait for all nodes to finish
                round++;
            }
        }
        System.out.println("Node " + id + " terminated with color " + color);
    }

    /**
     * receive message from neighbor u
     */
    private void receiveMessage(ColorMessage msg){
        inMessages.add(msg);
    }

    /**
     * send color message to all neighbors
     */
    private void broadcastMessage(ColorMessage msg){
        for(NodeA1 neighbor: neighbors)
            neighbor.receiveMessage(msg);
    }

    public void setNeighbors(List<NodeA1> nodes){
        neighbors.addAll(nodes);
    }

    public int getId(){
        return id;
    }

    public int getColor(){
        return color;
    }

    public List<NodeA1> getNeighbors(){
        return neighbors;
    }

}
