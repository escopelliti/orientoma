package it.asp.orientoma.navigation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import it.asp.orientoma.navigation.dummies.DummyEdge;
import it.asp.orientoma.navigation.dummies.DummyVertex;

/**
 * Created by Filippo on 16/05/2015.
 */
public class Graph {
    private List<IGraphVertex> _vertexes;
    private List<IGraphEdge> _edges;

    public Graph(List<IGraphVertex> vertexes, List<IGraphEdge> edges) {
        _vertexes = vertexes;
        _edges = edges;
    }

    /**
     * Loads the graph from the specified xml file
     * @param path to an xml file containing a graph description
     */
    public Graph(String path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        IGraphVertex[] __vertexes;
        IGraphEdge[] __edges;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(path);

            NodeList nodes = dom.getElementsByTagName("node");
            NodeList edges = dom.getElementsByTagName("edge");

            __vertexes = new IGraphVertex[nodes.getLength()];
            __edges = new IGraphEdge[edges.getLength()];

            Map<String, IGraphVertex> map = new HashMap<>();

            for(int i=0; i<nodes.getLength(); i++){
                Element e = (Element)nodes.item(i);
                DummyVertex tmp = new DummyVertex(e.getAttribute("id"));
                __vertexes[i] = tmp;
                map.put(tmp.getId(), tmp);
            }

            for(int i=0; i<edges.getLength(); i++) {
                Element e = (Element)edges.item(i);
                __edges[i] = new DummyEdge(map.get(e.getAttribute("src")),
                        map.get(e.getAttribute("dest")), Float.parseFloat(e.getAttribute("weight")));
            }

            _vertexes = Arrays.asList(__vertexes);
            _edges = Arrays.asList(__edges);
        } catch(Exception e) {
            _vertexes = null;
            _edges = null;
        }
    }

    public List<IGraphVertex> getVertexes() {
        return _vertexes;
    }

    public List<IGraphEdge> getEdges() {
        return _edges;
    }
}
