package lingfeng.BluetoothReader.Demo.navigation;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Interface used by the navigation algorithm.
 * Any object implementing this interface can be used as an edge in the graph.
 */
public interface IGraphEdge {

    public IGraphVertex getDestination();

    public IGraphVertex getSource();

    public float getWeight();
}
