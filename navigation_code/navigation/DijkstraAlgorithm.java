package it.asp.orientoma.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Filippo on 16/05/2015.
 */
public class DijkstraAlgorithm {

    //TODO: Implement optimizations (low priority)

    private final List<IGraphVertex> nodes;
    private final List<IGraphEdge> edges;
    private Set<IGraphVertex> settledNodes;
    private Set<IGraphVertex> unSettledNodes;
    private Map<IGraphVertex, IGraphVertex> predecessors;
    private Map<IGraphVertex, Float> distance;

    public DijkstraAlgorithm(Graph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<IGraphVertex>(graph.getVertexes());
        this.edges = new ArrayList<IGraphEdge>(graph.getEdges());
    }

    public void execute(IGraphVertex source) {
        settledNodes = new HashSet<IGraphVertex>();
        unSettledNodes = new HashSet<IGraphVertex>();
        distance = new HashMap<IGraphVertex, Float>();
        predecessors = new HashMap<IGraphVertex, IGraphVertex>();
        distance.put(source, 0.0f);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            IGraphVertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(IGraphVertex node) {
        List<IGraphVertex> adjacentNodes = getNeighbors(node);
        for (IGraphVertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private float getDistance(IGraphVertex node, IGraphVertex target) {
        for (IGraphEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<IGraphVertex> getNeighbors(IGraphVertex node) {
        List<IGraphVertex> neighbors = new ArrayList<IGraphVertex>();
        for (IGraphEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private IGraphVertex getMinimum(Set<IGraphVertex> IGraphVertexes) {
        IGraphVertex minimum = null;
        for (IGraphVertex IGraphVertex : IGraphVertexes) {
            if (minimum == null) {
                minimum = IGraphVertex;
            } else {
                if (getShortestDistance(IGraphVertex) < getShortestDistance(minimum)) {
                    minimum = IGraphVertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(IGraphVertex IGraphVertex) {
        return settledNodes.contains(IGraphVertex);
    }

    private float getShortestDistance(IGraphVertex destination) {
        Float d = distance.get(destination);
        if (d == null) {
            return Float.MAX_VALUE;
        } else {
            return d;
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<IGraphVertex> getPath(IGraphVertex target) {
        LinkedList<IGraphVertex> path = new LinkedList<IGraphVertex>();
        IGraphVertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}
