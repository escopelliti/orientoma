package it.asp.orientoma.navigation;

/**
 * Created by Filippo on 23/05/2015.
 */
public class MapEdge implements IGraphEdge {

    private IGraphVertex _src, _dest;
    float _weight;

    public MapEdge(IGraphVertex src, IGraphVertex dest, float weight) {
        _src =src;
        _dest = dest;
        _weight = weight;
    }

    @Override
    public IGraphVertex getDestination() {
        return _dest;
    }

    @Override
    public IGraphVertex getSource() {
        return _src;
    }

    @Override
    public float getWeight() {
        return _weight;
    }
}
