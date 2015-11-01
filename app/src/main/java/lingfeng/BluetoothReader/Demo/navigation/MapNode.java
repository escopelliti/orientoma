package lingfeng.BluetoothReader.Demo.navigation;

/**
 * Created by Filippo on 23/05/2015.
 */
public class MapNode extends IGraphVertex {

    private final String _id;
    private final String _id_north;
    private final String _id_south;
    private final String _id_east;
    private final String _id_west;

    public MapNode(String id, String id_north, String id_south, String id_east, String id_west) {
        _id = id;
        _id_north = id_north;
        _id_south = id_south;
        _id_east = id_east;
        _id_west = id_west;
    }

    @Override
    public String getId() {
        return _id;
    }

    public boolean isEastOf(MapNode o) {
        //True if o has an east node and that node is me
        return o.get_east() != null && o.get_east().equals(_id);
    }

    public boolean isWestOf(MapNode o) {
        //True if o has a west node and that node is me
        return o.get_west() != null && o.get_west().equals(_id);
    }

    public boolean isNorthOf(MapNode o) {
        //True if o has a north node and that node is me
        return o.get_north() != null && o.get_north().equals(_id);
    }

    public boolean isSouthOf(MapNode o) {
        //True if o has a south node and that node is me
        return o.get_south() != null && o.get_south().equals(_id);
    }

    public String get_north() {
        return _id_north;
    }

    public String get_south() {
        return _id_south;
    }

    public String get_east() {
        return _id_east;
    }

    public String get_west() {
        return _id_west;
    }
}
