import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.*;

/**
 * Created by dorse on 14/07/2017.
 */
public class Main {

    public static CyclicBarrier barrier;
    public static int DELTA;

    public static void main(String[] args){

        List<NodeA1> nodes = new ArrayList<>();
        for (int i=0; i<7; i++)
            nodes.add(new NodeA1(i));

        nodes.get(0).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(1), nodes.get(3), nodes.get(6)}));
        nodes.get(1).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(0), nodes.get(2)}));
        nodes.get(2).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(1), nodes.get(3), nodes.get(5), nodes.get(6)}));
        nodes.get(3).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(2), nodes.get(0), nodes.get(4)}));
        nodes.get(4).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(3), nodes.get(5)}));
        nodes.get(5).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(4), nodes.get(6), nodes.get(2)}));
        nodes.get(6).setNeighbors(Arrays.asList(new NodeA1[]{nodes.get(5), nodes.get(2), nodes.get(0)}));

        DELTA = 4;

        int numOfNodes = nodes.size();

        barrier = new CyclicBarrier(numOfNodes, () -> System.out.println("BarrierAction executed: last thread entering the barrier"));
        ExecutorService e = Executors.newFixedThreadPool(numOfNodes);
        for(NodeA1 node: nodes){
            e.execute(new Thread(node));
        }

        e.shutdown();

        try {
            e.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        for(NodeA1 node: nodes){
            System.out.println("Node: " + node.getId() + ", Color: " + node.getColor());
        }
    }
}
