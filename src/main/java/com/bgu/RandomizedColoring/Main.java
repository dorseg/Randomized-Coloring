package com.bgu.RandomizedColoring;

import com.bgu.RandomizedColoring.Algorithm1.NodeA1VertexFactory;
import com.bgu.RandomizedColoring.Algorithm2.NodeA2VertexFactory;
import org.jgrapht.Graphs;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static Phaser phaser; // in order to schedule the rounds
    public static int DELTA; // maximal node degree
    public final static int NODESNUM = 100;

    public static AtomicInteger round = new AtomicInteger(); // number of rounds

    public static void main(String[] args){
        SimpleGraph<Node, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        //double p = 0.01 * (new Random().nextInt((85 - 50) + 1) + 50); // p=[0.5;0.85]
        double p = 0.5;

        GnpRandomGraphGenerator<Node,DefaultEdge> graphGen = new GnpRandomGraphGenerator<>(NODESNUM, p);
        graphGen.generateGraph(graph, new NodeA2VertexFactory(0), null);
        Set<Node> nodes = graph.vertexSet();

        setNeighborsForEachNode(graph);
        DELTA = calculateDelta(nodes);

        phaser = new Phaser(NODESNUM){
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("======= Phase " + phase + " finished. number of nodes for next phase: "+registeredParties+" =======");
                round.incrementAndGet();
                return registeredParties == 0;
            }
        };

        ExecutorService e = Executors.newFixedThreadPool(NODESNUM);
        for(Node node: nodes)
            e.execute(new Thread(node));

        e.shutdown();

        try {
            // wait for all threads to terminate
            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        System.out.println();
        for(Node node: nodes){
            System.out.println("Node: " + node.getId() + ", Color: " + node.getColor());
        }

        System.out.println("\n======== Results ========");
        System.out.println("num of nodes: " + NODESNUM);
        System.out.println("Test Result: " + test(nodes));
        System.out.println("Total rounds: " + round.get());
        System.out.println("log(numOfNodes) = " +Math.log(NODESNUM)/Math.log(2));
        System.out.println("DELTA: " + DELTA);
        System.out.println("Max color: " + maxColor(nodes));
        System.out.println("probability: " + p);
        System.out.println("========== END ==========");
    }

    private static int calculateDelta(Set<Node> nodes){
        int maxDeg = 0;
        for (Node node: nodes){
            int deg = node.getNeighbors().size();
            if (deg > maxDeg)
                maxDeg = deg;
        }
        return maxDeg;
    }

    private static void setNeighborsForEachNode(SimpleGraph<Node, DefaultEdge> graph) {
        Set<Node> nodes = graph.vertexSet();
        for (Node node: nodes){
            List<Node> neighbors = Graphs.neighborListOf(graph, node);
            node.setNeighbors(neighbors);
        }
    }

    private static String test(Set<Node> nodes){
        for (Node v: nodes){
            int color = v.getColor();
            for (Node u: v.getNeighbors()){
                if (color == u.getColor())
                    return "FAIL";
            }
        }
        return "PASS";
    }

    private static int maxColor(Set<Node> nodes){
        int max = 0;
        for (Node v: nodes){
            int color = v.getColor();
            if (color > max)
                max = color;
        }
        return max;
    }
}
