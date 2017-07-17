package com.bgu.RandomizedColoring;

import com.bgu.RandomizedColoring.Algorithm1.NodeA1VertexFactory;
import com.bgu.RandomizedColoring.Algorithm2.NodeA2VertexFactory;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class Experiments {

    // modify for execution
    public final static int numOfGraphs = 50;
    public final static int ALGO = 1; // 1 - first algorithm. 2 - second algorithm.
    public static int numOfNodes = 100;
    public static double p = 0.5;

    // global variables
    public static int DELTA = -1;
    public static int round = 0;
    public static Phaser roundPhaser;
    public static Phaser colorPhaser;
    public static Phaser finalColorPhaser;

    public static void main(String[] args) {
        boolean allTestPassed = true;
        double averageRounds = 0;

        for (int i=0; i<numOfGraphs; i++) {
            System.out.println("\n===================== Graph number "+ i +" =====================");
            roundPhaser = new Phaser(numOfNodes){
                protected boolean onAdvance(int phase, int registeredParties) {
                    System.out.println("======= Phase " + phase + " finished. Number of nodes for next phase: "+registeredParties+" =======");
                    round++;
                    return registeredParties == 0;
                }
            };
            colorPhaser = new Phaser(numOfNodes);
            finalColorPhaser = new Phaser(numOfNodes);

            SimpleGraph<Node,DefaultEdge> graph = makeGraph();
            Set<Node> nodes = graph.vertexSet();
            setNeighborsForEachNode(graph);
            DELTA = calculateDelta(nodes);
            runAlgo(nodes);
            if (!test(nodes)) {
                allTestPassed = false;
                System.err.println("!!!! Failed with graph number " + i + " !!!!");
            }
            averageRounds += round;
            round = 0;
        }

        System.out.println("\n======== Results ========");
        System.out.println("Algorithm number: " + ALGO);
        System.out.println("Number of tested graphs: " + numOfGraphs);
        System.out.println("Number of nodes: " + numOfNodes);
        System.out.println("DELTA: " + DELTA);
        System.out.println("probability: " + p);
        System.out.println("Total rounds: " + averageRounds);
        System.out.println("Average Rounds: " + averageRounds/numOfGraphs);
        System.out.println("log(numOfNodes) = " +Math.log(numOfNodes)/Math.log(2));
        System.out.println("Test Result: " + (allTestPassed ? ANSI_GREEN + "PASS" + ANSI_RESET:ANSI_RED  +  "FAIL" + ANSI_RESET));
        System.out.println("========== END ==========");
    }

    private static SimpleGraph<Node,DefaultEdge> makeGraph() {
        GnpRandomGraphGenerator<Node,DefaultEdge> graphGen = new GnpRandomGraphGenerator<>(numOfNodes, p);
        SimpleGraph<Node,DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        VertexFactory<Node> vertexFactory;
        if (ALGO ==1 )
            vertexFactory = new NodeA1VertexFactory(0);
        else if (ALGO == 2)
            vertexFactory = new NodeA2VertexFactory(0);
        else {
            System.err.println("ALGO is " + ALGO + ". Exiting...");
            System.exit(1);
        }
        graphGen.generateGraph(graph, vertexFactory, null);
        return graph;
    }

    private static void setNeighborsForEachNode(SimpleGraph<Node, DefaultEdge> graph) {
        Set<Node> nodes = graph.vertexSet();
        for (Node node: nodes){
            List<Node> neighbors = Graphs.neighborListOf(graph, node);
            node.setNeighbors(neighbors);
        }
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

    private static void runAlgo(Set<Node> nodes) {
        ExecutorService e = Executors.newFixedThreadPool(numOfNodes);
        for(Node node: nodes)
            e.execute(new Thread(node));
        e.shutdown();
        try {
            // wait for all threads to terminate
            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private static boolean legalColor(int color){
        int upperBound;
        if (ALGO == 1)
            upperBound = 2*DELTA;
        else
            upperBound = DELTA+1;
        return color >= 1 && color <= upperBound;
    }

    private static boolean test(Set<Node> nodes){
        for (Node v: nodes){
            int color = v.getColor();
            if (!legalColor(color)) {
                System.err.println("Test failed: illegal color " + color + "!!!");
                return false;
            }
            for (Node u: v.getNeighbors()){
                if (color == u.getColor()) {
                    System.err.println("!!!! Test failed: a neighbor with same color " + color + " !!!!");
                    return false;
                }
            }
        }
        return true;
    }

    // colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

}
