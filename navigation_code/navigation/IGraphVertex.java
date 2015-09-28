package it.asp.orientoma.navigation;

/**
 * Created by Filippo on 16/05/2015.
 *
 * Interface used by the navigation algorithm.
 * Any object implementing this interface can be used as vertex in the graph.
 */
public abstract class IGraphVertex {

    public abstract String getId();

     @Override
    public boolean equals(Object o) {
         return o instanceof IGraphVertex && this.getId().equals(((IGraphVertex) o).getId());
     }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
