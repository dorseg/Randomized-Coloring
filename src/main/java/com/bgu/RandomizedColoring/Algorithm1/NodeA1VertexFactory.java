package com.bgu.RandomizedColoring.Algorithm1;

import com.bgu.RandomizedColoring.Node;
import org.jgrapht.VertexFactory;

/**
 * Factory for <b>NodeA1</b>, starting with <code>id = 0</code>
 */
public class NodeA1VertexFactory implements VertexFactory<Node> {

    private int id = 0;

    /**
     * Constructs a new NodeA1VertexFactory. The first vertex has id 0.
     */
    public NodeA1VertexFactory(){
        this(0);
    }

    /**
     * Constructs a new NodeA1VertexFactory.
     * @param id Starting id of the first vertex returned.
     */
    public NodeA1VertexFactory(int id){
        this.id = id;
    }

    @Override
    public Node createVertex(){
        Node newNode = new NodeA1(id);
        id++;
        return newNode;
    }
}
