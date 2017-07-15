package com.bgu.Algorithm1;

import org.jgrapht.Graphs;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static Phaser phaser;
    public static int DELTA;
    public final static int NODESNUM = 1000;

    public static void main(String[] args){

        SimpleGraph<NodeA1, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        double p = 0.01 * (new Random().nextInt((85 - 50) + 1) + 50); // p=[0.5;0.85]

        GnpRandomGraphGenerator<NodeA1,DefaultEdge> graphGen = new GnpRandomGraphGenerator<>(NODESNUM, p);
        graphGen.generateGraph(graph, new NodeA1VertexFactory(0), null);

        Set<NodeA1> nodes = graph.vertexSet();

        // set neighbors for each node
        for (NodeA1 node: nodes){
            List<NodeA1> neighbors = Graphs.neighborListOf(graph, node);
            node.setNeighbors(neighbors);
        }

        calculateDelta(nodes);

        System.out.println("A graph with " + NODESNUM + " nodes" + ", probability " + p + " and delta " + DELTA + " created");

        phaser = new Phaser(NODESNUM){
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("======= Phase " + phase + " finished: last thread entered to the phaser =======");
                return registeredParties == 0;
            }
        };
        ExecutorService e = Executors.newFixedThreadPool(NODESNUM);
        for(NodeA1 node: nodes){
            e.execute(new Thread(node));
        }

        e.shutdown();

        try {
            // wait for all threads to terminate
            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        System.out.println();
        for(NodeA1 node: nodes){
            System.out.println("Node: " + node.getId() + ", Color: " + node.getColor());
        }

        System.out.println("Test Result: " + test(nodes));
        System.out.println("========== END ==========");
    }

    public static void calculateDelta(Set<NodeA1> nodes){
        int maxDeg = 0;
        for (NodeA1 node: nodes){
            int deg = node.getNeighbors().size();
            if (deg > maxDeg)
                maxDeg = deg;
        }
        DELTA = maxDeg;
    }

    public static String test(Set<NodeA1> nodes){
        for (NodeA1 v: nodes){
            int color = v.getColor();
            for (NodeA1 u: v.getNeighbors()){
                if (color == u.getColor())
                    return "FAIL";
            }
        }
        return "PASS";
    }
}
