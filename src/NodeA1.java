import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by dorse on 14/07/2017.
 */
public class NodeA1 implements Runnable{

    private int id;
    private List<NodeA1> neighbors;
    private Queue<ColorMessage> inMessages;
    private Set<Integer> tempColors; // set of temporary colors selected by neighbors of this Node.
    private Set<Integer> finalColors; // set of final colors selected by neighbors of this Node.
    private int color; // the drawn color each round
    private boolean terminated;
    private int round;

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
            tempColors.clear();
            color = new Random().nextInt(2*Main.DELTA) + 1;
            broadcastMessage(new ColorMessage(color, false));
            Queue<ColorMessage> copyMessages = new ConcurrentLinkedQueue(inMessages);
            for (ColorMessage msg : copyMessages){
                if (!msg.isFinal()) {
                    tempColors.add(msg.getColor());
                    inMessages.remove(msg);
                }
            }
            if (!tempColors.contains(color) && !finalColors.contains(color)){
                broadcastMessage(new ColorMessage(color, true));
                terminated = true;
            }
            else {
                copyMessages = new ConcurrentLinkedQueue(inMessages);
                for (ColorMessage msg : copyMessages){
                    if (msg.isFinal()) {
                        finalColors.add(msg.getColor());
                        inMessages.remove(msg);
                    }
                }
            }
            try {
                System.out.println("Node " + id + " finished round " + round);
                Main.barrier.await();
                round++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Node " + id + " terminated with color " + color);
    }

    private void receiveMessage(ColorMessage msg){
        inMessages.add(msg);
    }

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

}
