package lingfeng.BluetoothReader.Demo.navigation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
    private Direction _lastDirection;
    private Graph map;

    public Navigator(InputStream map_file) throws MapNotFoundException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        IGraphVertex[] __vertexes;
        IGraphEdge[] __edges;
        Document dom = null;
        _lastNodeFound = null;

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(map_file);
        } catch (IOException e)
        {
            throw new MapNotFoundException("map not found.");
        } catch (SAXException e)
        {
            throw new MapNotFoundException("map not found.");
        } catch (ParserConfigurationException e)
        {
            throw new MapNotFoundException("map not found.");
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
        //Fix for the case where from == to
        if(id_from.equals(id_to)) {
            _path = new LinkedList<IGraphVertex>();
            _path.add(new MapNode(id_from, "", "", "", "")); //Faking the north, south, east and west nodes as we don't really care anymore of them here.
        }
    }

    public boolean isInitialized() {
        return _path != null;
    }

    public List<String> getNodeNames() {
        List<String> ret = new ArrayList<String>();
        List nodes = map.getVertexes();
        for(Iterator<IGraphVertex> i = nodes.listIterator(); i.hasNext(); ) {
            ret.add(i.next().getId());
        }
        return ret;
    }

    public Direction getNextDirection(String my_position_id) {
        if(_path == null)
            return null;

        if(_lastNodeFound != null && my_position_id.equals(_lastNodeFound.getId()))
            return _lastDirection; //If the same tag is read twice, return the last direction suggestion and don't do anything more.

        //Get the node from the nodes list
        MapNode my_position = map.findNode(my_position_id);

        //Check if I arrived
        if(my_position.equals(_path.get(_path.size()-1)))
            return Direction.TARGET;

        //Check if I'm still on path
        int index = _path.indexOf(my_position);

        if(index < 0) {
            //I got lost. Get a new path from here to the original destination
            this.initNavigation(my_position_id, _path.get(_path.size()-1).getId());
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
                _lastDirection = Direction.RIGHT;
            if(next.isWestOf(my_position))
                _lastDirection =  Direction.LEFT;
            if(next.isNorthOf(my_position))
                _lastDirection =  Direction.FORWARD;
            if(next.isSouthOf(my_position))
                _lastDirection =  Direction.BACKWARD;
            return _lastDirection; //We should never hit this line, it's a safety exit in case something goes wrong
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
                _lastDirection = Direction.RIGHT;
            if(next.isWestOf(my_position))
                _lastDirection = Direction.LEFT;
            if(next.isNorthOf(my_position))
                _lastDirection = Direction.FORWARD;
            if(next.isSouthOf(my_position))
                _lastDirection = Direction.BACKWARD;
            return _lastDirection;
        }
        if(my_position.isEastOf(prev)) {
            //I am facing east
            if(next.isEastOf(my_position))
                _lastDirection = Direction.FORWARD;
            if(next.isWestOf(my_position))
                _lastDirection = Direction.BACKWARD;
            if(next.isNorthOf(my_position))
                _lastDirection = Direction.LEFT;
            if(next.isSouthOf(my_position))
                _lastDirection = Direction.RIGHT;
            return _lastDirection;
        }
        if(my_position.isWestOf(prev)) {
            //I am facing west
            if(next.isEastOf(my_position))
                _lastDirection = Direction.BACKWARD;
            if(next.isWestOf(my_position))
                _lastDirection = Direction.FORWARD;
            if(next.isNorthOf(my_position))
                _lastDirection = Direction.RIGHT;
            if(next.isSouthOf(my_position))
                _lastDirection = Direction.LEFT;
            return _lastDirection;
        }
        if(my_position.isSouthOf(prev)) {
            //I am facing south
            if(next.isEastOf(my_position))
                _lastDirection = Direction.LEFT;
            if(next.isWestOf(my_position))
                _lastDirection = Direction.RIGHT;
            if(next.isNorthOf(my_position))
                _lastDirection = Direction.BACKWARD;
            if(next.isSouthOf(my_position))
                _lastDirection = Direction.FORWARD;
            return _lastDirection;
        }
        return _lastDirection;
    }

    public String getNextNodeInPath_debug(String cur_node) {
        for(int i=0; i<_path.size(); i++) {
            if(_path.get(i).getId().equals(cur_node))
                return i == _path.size()-1 ? "Target reached" : _path.get(i+1).getId();
        }
        return "Out of path";
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
