package lingfeng.BluetoothReader.Demo.navigation.dummies;


import lingfeng.BluetoothReader.Demo.navigation.IGraphVertex;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Dummy class used during buildup and testing of the navigation system.
 * It provides a basic vertex object to use, which has only a name.
 */
public class DummyVertex extends IGraphVertex {
    private String _name;

    public DummyVertex(String name) {
        _name = name;
    }

    @Override
    public String getId() {
        return _name;
    }

    @Override
    public String toString() {
        return _name;
    }
}
