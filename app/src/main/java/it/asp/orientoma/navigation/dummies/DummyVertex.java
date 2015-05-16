package it.asp.orientoma.navigation.dummies;

import it.asp.orientoma.navigation.IGraphVertex;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Dummy class used during buildup and testing of the navigation system.
 * It provides a basic vertex object to use, which has only a name.
 */
public class DummyVertex implements IGraphVertex {
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
