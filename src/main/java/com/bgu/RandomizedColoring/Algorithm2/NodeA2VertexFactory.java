package com.bgu.RandomizedColoring.Algorithm2;

import com.bgu.RandomizedColoring.Node;
import org.jgrapht.VertexFactory;

/**
 * Factory for <b>NodeA2</b>, starting with <code>id = 0</code>
 */
public class NodeA2VertexFactory implements VertexFactory<Node> {

    private int id = 0;

    /**
     * Constructs a new NodeA2VertexFactory. The first vertex has id 0.
     */
    public NodeA2VertexFactory(){
        this(0);
    }

    /**
     * Constructs a new NodeA2VertexFactory.
     * @param id Starting id of the first vertex returned.
     */
    public NodeA2VertexFactory(int id){
        this.id = id;
    }

    @Override
    public Node createVertex(){
        Node newNode = new NodeA2(id);
        id++;
        return newNode;
    }
}
