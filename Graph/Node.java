package Graph;

import java.util.*;

public class Node {
    private int label;
    private int id;
    private List<Edge> edges;

    public Node(int id, int label) {
        this.label = label;
        this.id = id;
        this.edges = new ArrayList<>();
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
