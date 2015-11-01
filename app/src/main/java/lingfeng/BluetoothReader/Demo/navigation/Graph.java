package lingfeng.BluetoothReader.Demo.navigation;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class Graph {
    private List<? extends IGraphVertex> _vertexes;
    private List<? extends IGraphEdge> _edges;

    public Graph(List<? extends IGraphVertex> vertexes, List<? extends IGraphEdge> edges) {
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

            Map<String, IGraphVertex> map = new HashMap<String, IGraphVertex>();

            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                MapNode tmp = new MapNode(e.getAttribute("id"), e.getAttribute("north"), e.getAttribute("south"), e.getAttribute("east"), e.getAttribute("west"));
                __vertexes[i] = tmp;
                map.put(tmp.getId(), tmp);
            }

            for (int i = 0; i < edges.getLength(); i++) {
                Element e = (Element) edges.item(i);
                __edges[i] = new MapEdge(map.get(e.getAttribute("src")), map.get(e.getAttribute("dest")), Float.parseFloat(e.getAttribute("weight")));
            }

            _vertexes = Arrays.asList(__vertexes);
            _edges = Arrays.asList(__edges);
        } catch (IOException e) {
            Log.e("Map error", "map not found!");
        } catch(Exception e) {
            _vertexes = null;
            _edges = null;
        }
    }

    public List<? extends IGraphVertex> getVertexes() {
        return _vertexes;
    }

    public List<? extends IGraphEdge> getEdges() {
        return _edges;
    }

    //Hack per prendere il MapNode corretto in base alla stringa del suo id
    public MapNode findNode(String id)
    {
        for(int i=0; i<_vertexes.size(); i++) {
            if(_vertexes.get(i).getId().equals(id))
                return (MapNode)_vertexes.get(i);
        }
        return null;
    }
}
