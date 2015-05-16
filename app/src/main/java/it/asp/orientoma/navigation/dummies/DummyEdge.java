package it.asp.orientoma.navigation.dummies;

import it.asp.orientoma.navigation.IGraphEdge;
import it.asp.orientoma.navigation.IGraphVertex;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Dummy class used during buildup and testing of the navigation system.
 * It provides a basic edge object to use, with all the basic capabilities required.
 */
public class DummyEdge implements IGraphEdge {

    private IGraphVertex _src, _dest;
    float _weight;

    public DummyEdge(IGraphVertex src, IGraphVertex dest, float weight) {
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
