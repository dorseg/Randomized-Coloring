package com.bgu.RandomizedColoring;

import com.bgu.RandomizedColoring.Algorithm1.NodeA1VertexFactory;
import com.bgu.RandomizedColoring.Algorithm2.NodeA2VertexFactory;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphUnion;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

public class Experiments {

    public final static int ALGO = 2; // 1 - first algorithm. 2 - second algorithm.

    // global variables
    public static int DELTA = -1;
    public static int round = 0;
    public static Phaser roundPhaser = null;
    public static Phaser colorPhaser = null;
    public static Phaser finalColorPhaser = null;

    public static void main(String[] args) {
        // modify for execution
        final int numOfGraphs = 50;
        int numOfNodes = 500;
        double p = 0.5;

//        testPartialColor(10, 100, 0.5, 0.5);

        Stats stats = runExperiment(numOfGraphs, numOfNodes, p);
        System.out.println("\n======== Results ========");
        if (stats == null) {
            System.err.println("Test Result: Fail. Exiting...");
            return;
        }
        System.out.println("Test Result: " + ANSI_GREEN + "PASS" + ANSI_RESET);
        System.out.println("Algorithm number: " + ALGO);
        System.out.println("Number of tested graphs: " + numOfGraphs);
        System.out.println("Number of nodes: " + numOfNodes);
        System.out.println("probability: " + p);
        System.out.println("log(numOfNodes) = " +Math.log(numOfNodes)/Math.log(2));
        System.out.println(stats);
        System.out.println("========== END ==========");
    }

    private static Stats runExperiment(int numOfGraphs, int numOfNodes, double p) {
        boolean allTestsPassed = true;
        double avgDistinctColors = 0, avgDelta = 0, avgRounds = 0;
        for (int i=0; i<numOfGraphs; i++) {
            System.out.println("\n===================== Graph number "+ i +" =====================");
            Graph<Node,DefaultEdge> graph = makeGraph(numOfNodes, p);
            graphColoring(graph);
            Set<Node> nodes = graph.vertexSet();
            if (!test(nodes)) {
                allTestsPassed = false;
                System.err.println("!!!! Failed with graph number " + i + " !!!!");
            }
            avgDistinctColors += numOfDistinctColors(nodes);
            avgDelta += DELTA;
            avgRounds += round;
            round = 0;
        }

        if (!allTestsPassed) return null;
        return new Stats(avgDistinctColors/numOfGraphs,
                avgDelta/numOfGraphs, avgRounds/numOfGraphs);
    }

    private static void graphColoring(Graph<Node, DefaultEdge> graph) {
        Set<Node> nodes = graph.vertexSet();

        Set<Node> nodesWithoutColor = new HashSet<>(); // for partial coloring
        for (Node v: nodes)
            if (v.getColor() == -1)
                nodesWithoutColor.add(v);
        System.out.println(">>>>>> Node without color: " +nodesWithoutColor.size()); // remove

        round = 0;
        roundPhaser = new Phaser(nodesWithoutColor.size()){
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("======= Phase " + phase + " finished. Number of nodes for next phase: "+registeredParties+" =======");
                round++;
                return registeredParties == 0;
            }
        };
        colorPhaser = new Phaser(nodesWithoutColor.size());
        finalColorPhaser = new Phaser(nodesWithoutColor.size());
        setNeighborsForEachNode(graph);
        DELTA = calculateDelta(nodes);
        runAlgo(nodesWithoutColor);
    }

    private static Graph<Node,DefaultEdge> makeGraph(int numOfNodes, double p) {
        GnpRandomGraphGenerator<Node,DefaultEdge> graphGen = new GnpRandomGraphGenerator<>(numOfNodes, p);
        Graph<Node,DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
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

    private static void setNeighborsForEachNode(Graph<Node, DefaultEdge> graph) {
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
        ExecutorService e = Executors.newFixedThreadPool(nodes.size());
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

    private static int numOfDistinctColors (Set<Node> nodes) {
        Set<Integer> colors = new HashSet<>();
        for (Node v: nodes)
            colors.add(v.getColor());
        return colors.size();
    }

    private static void testPartialColor(int numOfNodes1, int numOfNodes2, double p1, double p2) {
        Graph<Node,DefaultEdge> graph1 = makeGraph(numOfNodes1, p1);
        Graph<Node,DefaultEdge> graph2 = makeGraph(numOfNodes2, p2);

        graphColoring(graph1);
        Set<Node> nodes1 = graph1.vertexSet();

        if (!test(nodes1))
            System.err.println("!!!! Failed with the first graph !!!!");

        // create a union graph and color it
        Graph<Node, DefaultEdge> union = new GraphUnion<>(graph1, graph2);
        graphColoring(union);
        System.out.println("Test Partial Color Result: " + (test(union.vertexSet()) ? ANSI_GREEN+"PASS"+ANSI_RESET:ANSI_RED+"FAIL"+ANSI_RESET));

    }

    static class Stats {
        public double averageDistinctColors;
        public double averageDelta;
        public double averageRounds;

        public Stats (double avgDistinctColors, double avgDelta, double avgRounds) {
            this.averageDistinctColors = avgDistinctColors;
            this.averageDelta = avgDelta;
            this.averageRounds = avgRounds;
        }

        public String toString() {
            String head = "####### Stats #######\n";
            String strRounds = "Average Rounds: " + averageRounds + "\n";
            String strDelta = "Average Delta: " + averageDelta + "\n";
            String strDistinctColors = "Average distinct colors: " + averageDistinctColors + "\n";
            String strTotalColors = "Average Total Colors: " + (ALGO == 1 ? 2*averageDelta : averageDelta+1);
            return head + strRounds + strDelta + strDistinctColors + strTotalColors;
        }
    }

    // colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

}
