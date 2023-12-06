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
        final BufferedReader rows = new BufferedReader(new FileReader(new File(filename)));

        // read graph from rows
        // nodes
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
                final double weight = Double.parseDouble(parts[3]);

                Node node1 = nodes.get(index1);
                Node node2 = nodes.get(index2);
                addEdge(node1, node2, weight);
            } while ((line = rows.readLine()) != null && (line.charAt(0) == 'e'));
        }
    }

    public void readFromFileNoLabels(String filename) throws IOException {
        // Create graph from file with no weights or labels;
        // weights will default to 1.0, labels will be same as node id
        final BufferedReader rows = new BufferedReader(new FileReader(new File(filename)));
        int numberOfNodes = 0;
        String line;
        rows.readLine();

        // To generate 1K random node IDs and random weights b/w 0 and 30
        Random rand = new Random();

        while ((line = rows.readLine()) != null) {
            final String[] parts = line.split("\\s+");
            final int node1 = Integer.parseInt(parts[0]);
            final int node2 = Integer.parseInt(parts[1]);
            int node1Id = rand.nextInt(1000);
            int node2Id = rand.nextInt(1000);

            // Random Gaussian distribution between 0 and 30
            double weight = (rand.nextGaussian() * 5) + 15;
            if (weight < 0) {
                weight = 0;
            } else if (weight > 30) {
                weight = 30;
            }

            addNode(node1, node1Id);
            addNode(node2, node2Id);

            // System.out.println("READING NODE IDs: \n" + "node1: " + node1 + " node2: " +
            // node2);
            // System.out.println("Node 1: " + nodes.get(node1).getId() + " Node 2: " +
            // nodes.get(node2).getId());

            numberOfNodes += 2;

            addEdge(nodes.get(node1), nodes.get(node2), weight);

        }

        nodeCount = numberOfNodes;
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
