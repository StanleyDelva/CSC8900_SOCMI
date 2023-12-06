package Graph;

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.nio.file.Path;

public class Graph {
    private Map<Integer, Node> nodes;
    private ArrayList<Edge> edges;
    // adjacency list: key is node id, value is list of node labels
    private HashMap<Integer, ArrayList<Integer>> adjList;

    private int nodeCount = 0;

    public Graph() {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.adjList = new HashMap<>();
    }

    public Graph(Edge edge) {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.adjList = new HashMap<>();
        this.addEdge(edge.getSource(), edge.getDestination(), edge.getWeight());
    }

    public Graph(Graph graph) {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.nodes = graph.nodes;
        this.edges = graph.edges;
        this.adjList = graph.adjList;
    }

    public void addNode(Integer id) {
        addNode(id, -1);
    }

    public void addNode(Integer id, Integer label) {
        if (!nodes.containsKey(id)) {
            Node node = new Node(id, label);
            nodes.put(id, node);
            nodeCount++;
        }
    }

    public void addEdge(Node source, Node destination, Double weight) {

        if (source == null || destination == null) {
            throw new IllegalArgumentException("Invalid nodes: " + source.getId() + ", " + destination);
        }

        Edge edge = new Edge(source, destination, weight);
        edges.add(edge);
        source.addEdge(edge);

        if (this.adjList.get(source.getId()) == null) {
            this.adjList.put(source.getId(), new ArrayList<Integer>());
            this.adjList.get(source.getId()).add(destination.getLabel());
        } else {
            this.adjList.get(source.getId()).add(destination.getLabel());
        }
    }

    public void addEdge(Edge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
        }
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public void extendGraph(Graph graph) {
        for (Node node : graph.getNodes()) {
            this.addNode(node.getId(), node.getLabel());
        }

        this.edges.addAll(graph.getEdges());
    }

    public void readFromFile(String filename) throws IOException {
        String text = "";
        final BufferedReader rows = new BufferedReader(new FileReader(new File(filename)));

        // read graph from rows
        // nodes
        int counter = 0;
        int numberOfNodes = 0;
        String line;
        String tempLine;
        rows.readLine();
        while ((line = rows.readLine()) != null && (line.charAt(0) == 'v')) {
            final String[] parts = line.split("\\s+");
            final int id = Integer.parseInt(parts[1]);
            final int label = Integer.parseInt(parts[2]);

            addNode(id, label);
            if (nodes.get(id) == null) {
                addNode(id, label);
            }

            numberOfNodes++;
            counter++;
        }
        nodeCount = numberOfNodes;
        tempLine = line;

        // edges

        // use the first edge line
        if (tempLine.charAt(0) == 'e')
            line = tempLine;
        else
            line = rows.readLine();

        if (line != null) {
            do {
                final String[] parts = line.split("\\s+");
                final int index1 = Integer.parseInt(parts[1]);
                final int index2 = Integer.parseInt(parts[2]);
                final double label = Double.parseDouble(parts[3]);

                Node node1 = nodes.get(index1);
                Node node2 = nodes.get(index2);
                addEdge(node1, node2, label);
            } while ((line = rows.readLine()) != null && (line.charAt(0) == 'e'));
        }
    }

    public HashMap<Integer, ArrayList<Integer>> getAdjList() {
        return adjList;
    }

    public double getMaxDistance() {
        double maxDistance = 0.0;
        for (Edge edge : edges) {
            if (edge.getWeight() > maxDistance) {
                maxDistance = edge.getWeight();
            }
        }
        return maxDistance;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Edge> getEdges(Node node) {
        return edges.stream().filter(edge -> edge.getSource().equals(node)).collect(Collectors.toList());
    }

    public String printGraph() {
        String text = "";
        // for (Node node : nodes.values()) {
        // text += node.getId() + " " + node.getLabel() + "\n";
        // }
        for (Edge edge : edges) {
            text += edge.getSource().getId() + " (" + edge.getSource().getLabel() + ") "
                    + edge.getDestination().getId() + " (" + edge.getDestination().getLabel() + ") "
                    + " Wt: " + edge.getWeight() + "\n";
        }
        return text;
    }
}
