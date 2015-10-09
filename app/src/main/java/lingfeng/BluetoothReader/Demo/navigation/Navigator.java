package lingfeng.BluetoothReader.Demo.navigation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Filippo on 23/05/2015.
 *
 * Class that wraps the navigation system of the application.
 */
public class Navigator {
    private DijkstraAlgorithm _alg;
    private List<IGraphVertex> _path;
    private MapNode _lastNodeFound;
    private Graph map;

    public Navigator(String map_path) throws MapNotFoundException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        IGraphVertex[] __vertexes;
        IGraphEdge[] __edges;
        Document dom = null;
        _lastNodeFound = null;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(map_path);
        } catch (IOException e)
        {
            throw new MapNotFoundException("map " + map_path + " was not found.");
        } catch (SAXException e)
        {
            throw new MapNotFoundException("map " + map_path + " was not found.");
        } catch (ParserConfigurationException e)
        {
            throw new MapNotFoundException("map " + map_path + " was not found.");
        }

        List[] storage = new List[2];
        getNodesAndEdges(dom, storage);

        map = new Graph(storage[0], storage[1]);
        _alg = new DijkstraAlgorithm(map);
    }

    public void initNavigation(String id_from, String id_to) {
        MapNode from = map.findNode(id_from);
        MapNode to = map.findNode(id_to);
        _alg.execute(from);
        _path = _alg.getPath(to);
    }

    public boolean isInitialized() {
        return _path != null;
    }

    public Direction getNextDirection(String my_position_id) {
        if(_path == null)
            return null;

        //Get the node from the nodes list
        MapNode my_position = map.findNode(my_position_id);

        //TODO: Make sure that this function is NOT called twice for the same node without moving! (If it happens, atm we fuck up the previous node reading)

        //Check if I arrived
        if(my_position.equals(_path.get(_path.size())))
            return Direction.TARGET;

        //Check if I'm still on path
        int index = _path.indexOf(my_position);

        if(index < 0) {
            //I got lost. Get a new path from here to the original destination
            this.initNavigation(my_position_id, _path.get(_path.size()).getId());
            index = 0;
            _lastNodeFound = null;
        }

        MapNode next = (MapNode)_path.get(index+1);

        //Check if it's the first node we encounter
        if(_lastNodeFound == null) {
            //We don't know which direction the blind guy is facing.
            //Send him in a random direction and then we'll pick up from there.
            //(We assume the guy is facing north)
            _lastNodeFound = my_position;
            if(next.isEastOf(my_position))
                return Direction.RIGHT;
            if(next.isWestOf(my_position))
                return Direction.LEFT;
            if(next.isNorthOf(my_position))
                return Direction.FORWARD;
            if(next.isSouthOf(my_position))
                return Direction.BACKWARD;
            return null; //We should never hit this line, it's a safety exit in case something goes wrong
        }

        //We now know the last node we came from and the one we're at. We can thus calculate the
        //direction we're facing and send the blind guy in the proper direction this time.

        //Update the last node found
        MapNode prev = _lastNodeFound;
        _lastNodeFound = my_position;

        //Now check for all directions where the new node is
        if(my_position.isNorthOf(prev)) {
            //I am facing north
            if(next.isEastOf(my_position))
                return Direction.RIGHT;
            if(next.isWestOf(my_position))
                return Direction.LEFT;
            if(next.isNorthOf(my_position))
                return Direction.FORWARD;
            if(next.isSouthOf(my_position))
                return Direction.BACKWARD;
            return null;
        }
        if(my_position.isEastOf(prev)) {
            //I am facing east
            if(next.isEastOf(my_position))
                return Direction.FORWARD;
            if(next.isWestOf(my_position))
                return Direction.BACKWARD;
            if(next.isNorthOf(my_position))
                return Direction.LEFT;
            if(next.isSouthOf(my_position))
                return Direction.RIGHT;
            return null;
        }
        if(my_position.isWestOf(prev)) {
            //I am facing west
            if(next.isEastOf(my_position))
                return Direction.BACKWARD;
            if(next.isWestOf(my_position))
                return Direction.FORWARD;
            if(next.isNorthOf(my_position))
                return Direction.RIGHT;
            if(next.isSouthOf(my_position))
                return Direction.LEFT;
            return null;
        }
        if(my_position.isSouthOf(prev)) {
            //I am facing south
            if(next.isEastOf(my_position))
                return Direction.LEFT;
            if(next.isWestOf(my_position))
                return Direction.RIGHT;
            if(next.isNorthOf(my_position))
                return Direction.BACKWARD;
            if(next.isSouthOf(my_position))
                return Direction.FORWARD;
            return null;
        }
        return null;
    }

    private void getNodesAndEdges(Document dom, List[] storage){
        NodeList nodes = dom.getElementsByTagName("node");
        NodeList edges = dom.getElementsByTagName("edge");

        MapNode[] _nodes = new MapNode[nodes.getLength()];
        MapEdge[] _edges = new MapEdge[edges.getLength()];

        Map<String, MapNode> map = new HashMap<String, MapNode>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            MapNode tmp = new MapNode(e.getAttribute("id"), e.getAttribute("north"), e.getAttribute("south"), e.getAttribute("east"), e.getAttribute("west"));
            _nodes[i] = tmp;
            map.put(tmp.getId(), tmp);
        }

        for (int i = 0; i < edges.getLength(); i++) {
            Element e = (Element) edges.item(i);
            _edges[i] = new MapEdge(map.get(e.getAttribute("src")), map.get(e.getAttribute("dest")), Float.parseFloat(e.getAttribute("weight")));
        }

        storage[0] = Arrays.asList(_nodes);
        storage[1] = Arrays.asList(_edges);
    }

    private class MapNotFoundException extends Exception {
        public MapNotFoundException(String s) {
            super(s);
        }
    }
}
