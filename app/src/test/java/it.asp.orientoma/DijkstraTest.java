package it.asp.orientoma;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.asp.orientoma.navigation.DijkstraAlgorithm;
import it.asp.orientoma.navigation.Graph;
import it.asp.orientoma.navigation.IGraphEdge;
import it.asp.orientoma.navigation.IGraphVertex;
import it.asp.orientoma.navigation.dummies.DummyEdge;
import it.asp.orientoma.navigation.dummies.DummyVertex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Test class for the Dijkstra implementation
 */
public class DijkstraTest {

    private List<IGraphVertex> nodes;
    private List<IGraphEdge> edges;

    @Test
    public void testExecute() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            IGraphVertex location = new DummyVertex("Node_" + i);
            nodes.add(location);
        }

        addLane(0, 1, 85);
        addLane(0, 2, 217);
        addLane(0, 4, 173);
        addLane(2, 6, 186);
        addLane(2, 7, 103);
        addLane(3, 7, 183);
        addLane(5, 8, 250);
        addLane(8, 9, 84);
        addLane(7, 9, 167);
        addLane(4, 9, 502);
        addLane(9, 10, 40);
        addLane(1, 10, 600);

        // Lets check from location Loc_1 to Loc_10
        Graph graph = new Graph(nodes, edges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(nodes.get(0));
        LinkedList<IGraphVertex> path = dijkstra.getPath(nodes.get(10));

        assertNotNull(path);
        assertTrue(path.size() > 0);
    }

    private void addLane(int sourceLocNo, int destLocNo,
                         int duration) {
        IGraphEdge lane = new DummyEdge(nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
        edges.add(lane);
    }

    @Test
    public void testXmlParser() {
        //TODO: Capisci come trasformare questo in un path relativo
        Graph g = new Graph("C:\\Users\\Filippo\\AndroidStudioProjects\\Orientoma\\app\\src\\main\\res\\raw\\test_scenario_1.xml");
        assertEquals("Wrong number of edges returned!", 24, g.getEdges().size());
        assertEquals("Wrong number of vertices returned!", 10, g.getVertexes().size());

        IGraphVertex src = g.getVertexes().get(7);  //Src = nodo 8
        IGraphVertex dest = g.getVertexes().get(4); //Dest = nodo 5
        DijkstraAlgorithm d = new DijkstraAlgorithm(g);
        d.execute(src);
        List<IGraphVertex> path = d.getPath(dest);
        for(IGraphVertex v : path){
            System.out.println(v);
        }
    }
}