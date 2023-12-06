package Graph;

public class Edge {
    private Node source;
    private Node destination;
    private Double weight;

    public Edge(Node source, Node destination) {
        this.source = source;
        this.destination = destination;
        this.weight = 1.0;
    }

    public Edge(Node source, Node destination, Double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public Double getWeight() {
        return weight;
    }
}
