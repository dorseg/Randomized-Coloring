package com.bgu.Algorithm1;

import org.jgrapht.VertexFactory;

/**
 * Factory for NodeA1, starting with <code>id = 0</code>
 */
public class NodeA1VertexFactory implements VertexFactory<NodeA1> {

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
    public NodeA1 createVertex(){
        NodeA1 newNode = new NodeA1(id);
        id++;
        return newNode;
    }
}
