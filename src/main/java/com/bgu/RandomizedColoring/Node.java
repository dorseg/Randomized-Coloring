package com.bgu.RandomizedColoring;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Node implements Runnable{

    protected int id; // unique id for each node.
    protected List<Node> neighbors; // List of node neighbors.
    protected Queue<ColorMessage> inMessages; // income messages - final and not final.
    protected Set<Integer> tempColors; // T_v - set of temporary colors selected by neighbors of this node.
    protected Set<Integer> finalColors; // F_v - set of final colors selected by neighbors of this node.
    protected int color; // the drawn color in each round.
    protected boolean terminated;

    public Node(){
        this(0);
    }

    public Node(int id){
        this.id = id;
        neighbors = new LinkedList<>();
        inMessages = new ConcurrentLinkedQueue<>();
        tempColors = new HashSet<>();
        finalColors = new HashSet<>();
        color = -1;
        terminated = false;
    }

    @Override
    public void run() {
        while (!terminated){
//            System.out.println("Node " + id + " executes round " + Main.round.get()); // <<<<<<<< REMOVE
            tempColors.clear(); // T_v = empty set
            color = new Random().nextInt(2); // drawn color from [0,1]
            if (checkColor()) // for NodeA2 algorithm (in NodeA1 it returns false).
                continue;
            color = chooseColor();
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
//            System.out.println("Node " + id + " finished round " + Main.round.get()); // <<<<<<<< REMOVE
            if (terminated)
                Main.phaser.arriveAndDeregister(); // reduces the number of nodes required to advance in the next round
            else
                Main.phaser.arriveAndAwaitAdvance(); // wait for all nodes to finish
        }
//        System.out.println("Node " + id + " terminated with color " + color); // <<<<<<<< REMOVE
    }

    public abstract int chooseColor();

    public abstract boolean checkColor();

    /**
     * receive message from neighbor u
     */
    protected void receiveMessage(ColorMessage msg){
        inMessages.add(msg);
    }

    /**
     * send color message to all neighbors
     */
    protected void broadcastMessage(ColorMessage msg){
        for(Node neighbor: neighbors)
            neighbor.receiveMessage(msg);
    }

    /**
     * update the neighbors for the node after the graph creation.
     */
    public void setNeighbors(List<Node> nodes){
        neighbors.addAll(nodes);
    }

    // Getters
    public int getId(){
        return id;
    }

    public int getColor(){
        return color;
    }

    public List<Node> getNeighbors(){
        return neighbors;
    }
}
