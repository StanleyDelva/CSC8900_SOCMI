// Stanley Delva
// CSC 8900: Network Science

import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.text.ParseException;

public class SOCMI {

    private Pathgraph pathgraph;

    public SOCMI(Graph graph) {
        this.pathgraph = new Pathgraph(graph);
    }

    public List<Pathgraph> findFrequentPathgraphs(int minSupport) {
        List<Pathgraph> frequentPathgraphs = new ArrayList<>();
        Map<Pathgraph, Integer> pathgraphCounts = new HashMap<>();

        // Find all Pathgraphs with at least minSupport support
        for (Node node : pathgraph.getGraph().keySet()) {
            findFrequentPathgraphsDFS(node, new Pathgraph(), pathgraphCounts, minSupport);
        }

        // Filter for frequent Pathgraphs based on support threshold
        for (Map.Entry<Pathgraph, Integer> entry : pathgraphCounts.entrySet()) {
            Pathgraph pathgraph = entry.getKey();
            int support = entry.getValue();

            if (support >= minSupport) {
                frequentPathgraphs.add(pathgraph);
            }
        }

        return frequentPathgraphs;
    }

    private void findFrequentPathgraphsDFS(Node node, Pathgraph currentPathgraph,
            Map<Pathgraph, Integer> pathgraphCounts, int minSupport) {
        if (currentPathgraph.getGraph().isEmpty()) {
            // Check support for single-node Pathgraph
            if (!pathgraphCounts.containsKey(currentPathgraph)) {
                pathgraphCounts.put(currentPathgraph, 0);
            }

            pathgraphCounts.put(currentPathgraph, pathgraphCounts.get(currentPathgraph) + 1);
            return;
        }

        // Expand Pathgraph by adding edges from current node
        for (Edge edge : pathgraph.getOriginalGraph().getNodes().get(node.getId()).getEdges()) {
            Node destination = edge.getDestination();

            if (!currentPathgraph.getGraph().containsKey(node)
                    || currentPathgraph.getGraph().get(node).get(node).stream().filter(
                            path -> path.getEdges().stream()
                                    .anyMatch(listedEdge -> listedEdge.getDestination().equals(destination)))
                            .collect(Collectors.toList()).isEmpty()) {
                Pathgraph newPathgraph = new Pathgraph(currentPathgraph.getGraph());
                newPathgraph.addEdge(node, destination, new ArrayList<>());

                // Check support for expanded Pathgraph
                if (!pathgraphCounts.containsKey(newPathgraph)) {
                    pathgraphCounts.put(newPathgraph, 0);
                }

                pathgraphCounts.put(newPathgraph, pathgraphCounts.get(newPathgraph) + 1);

                // Recursively expand Pathgraph
                findFrequentPathgraphsDFS(destination, newPathgraph, pathgraphCounts, minSupport);
            }
        }
    }

    public static void main(String args[]) {
        Graph graph = new Graph();
        try {
            graph.readFromFile("data/citeseer.lg");
        } catch (IOException e) {
            System.out.println("Error reading citeseer.lg");
            System.exit(1);
        }

        SOCMI socmi = new SOCMI(graph);
        List<Pathgraph> frequentPathgraphs = socmi.findFrequentPathgraphs(160);

        System.out.println("Frequent Pathgraphs:");
        for (Pathgraph pathgraph : frequentPathgraphs) {
            System.out.println("Support: " + pathgraph.getGraph().get(pathgraph.getNodes().get(0))
                    .get(pathgraph.getNodes().get(0)).size());
            pathgraph.printGraph();
        }
    }
}

class Node {
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

class Edge {
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

class Graph {
    private Map<Integer, Node> nodes;
    private ArrayList<Edge> edges;
    private HashMap<Integer, HashMap<Integer, Node>> nodesByLabel;
    private int nodeCount = 0;

    public Graph() {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
    }

    public void addNode(Integer id) {
        addNode(id, -1);
    }

    public void addNode(Integer id, Integer label) {
        if (!nodes.containsKey(id)) {
            Node node = new Node(id, label);
            nodes.put(id, node);
        }
    }

    public void addEdge(Node source, Node destination, Double weight) {

        if (source == null || destination == null) {
            throw new IllegalArgumentException("Invalid nodes: " + source.getId() + ", " + destination);
        }

        Edge edge = new Edge(source, destination, weight);
        edges.add(edge);
        source.addEdge(edge);
        destination.addEdge(edge);
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

    public void readNodeLabelsFromTxtFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                int label = Integer.parseInt(parts[1].trim());

                nodes.get(id).setLabel(label);
            }
        }
    }

    public int[][] getAdjacencyMatrix() {
        int numNodes = nodes.size();
        int[][] adjacencyMatrix = new int[numNodes][numNodes];

        for (Edge edge : edges) {
            int sourceId = edge.getSource().getId();
            int destinationId = edge.getDestination().getId();

            adjacencyMatrix[sourceId][destinationId] = 1;
        }

        return adjacencyMatrix;
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

}

class Path {

    private List<Node> nodes;
    private int support;
    private int length;
    private List<Edge> edges;

    public Path() {
        this.nodes = new ArrayList<>();
        this.support = 0;
        this.length = 0;
        this.edges = new ArrayList<>();
    }

    public Path(Path other) {
        this.nodes = new ArrayList<>(other.nodes);
        this.support = other.support;
        this.length = other.length;
        this.edges = new ArrayList<>(other.edges);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getSupport() {
        return support;
    }

    public int getLength() {
        return length;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public String printPath() {
        String path = "";
        for (Node node : nodes) {
            path += node.getId() + " -> ";
        }
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;

        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Path other = (Path) obj;
        return nodes.equals(other.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    public boolean containsNode(Node node) {
        return nodes.contains(node);
    }

}

class Pathgraph {

    private Map<Node, Map<Node, List<Path>>> graph;
    private Graph originalGraph;

    public Pathgraph(Graph originalGraph) {
        this.graph = new HashMap<>();
        this.originalGraph = originalGraph;

        for (Node node : originalGraph.getNodes()) {
            Map<Node, List<Path>> neighbors = new HashMap<>();

            for (Edge edge : originalGraph.getEdges(node)) {
                Node destination = edge.getDestination();
                List<Path> paths = new ArrayList<>();

                Path path = new Path();
                path.addNode(node);
                path.addNode(destination);
                paths.add(path);

                findAllPathsDFS(originalGraph, node, destination, path, paths);
                neighbors.put(destination, paths);
            }

            this.graph.put(node, neighbors);
        }
    }

    public Pathgraph(Map<Node, Map<Node, List<Path>>> graph) {
        this.graph = graph;
    }

    public Pathgraph() {
        this.graph = new HashMap<>();
    }

    private void findAllPathsDFS(Graph graph, Node source, Node destination, Path currentPath, List<Path> paths) {
        if (source.equals(destination)) {
            paths.add(new Path(currentPath));
            return;
        }

        for (Edge edge : graph.getEdges(source)) {
            Node nextNode = edge.getDestination();

            if (!currentPath.containsNode(nextNode)) {
                Path newPath = new Path(currentPath);
                newPath.addNode(nextNode);

                findAllPathsDFS(graph, nextNode, destination, newPath, paths);
            }
        }
    }

    public List<Path> getPaths(Node source, Node destination) {
        if (!this.graph.containsKey(source) || !this.graph.get(source).containsKey(destination)) {
            return new ArrayList<>();
        }

        return this.graph.get(source).get(destination);
    }

    public boolean hasPath(Node source, Node destination) {

        return this.graph.containsKey(source) && this.graph.get(source).containsKey(destination)
                && !this.graph.get(source).get(destination).isEmpty();
    }

    public Map<Node, Map<Node, List<Path>>> getGraph() {
        return graph;
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public List<Node> getNodes() {
        return new ArrayList<>(graph.keySet());
    }

    public void addEdge(Node source, Node destination, List<Path> paths) {
        if (!this.graph.containsKey(source)) {
            this.graph.put(source, new HashMap<>());
        }

        this.graph.get(source).put(destination, paths);
    }

    public Graph getOriginalGraph() {
        return originalGraph;
    }

    public void printGraph() {
        String filename = "output.txt";
        System.out.println("Writing to file '" + filename + "'");

        try {
            FileWriter fileWriter = new FileWriter(filename);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (Map.Entry<Node, Map<Node, List<Path>>> entry : this.graph.entrySet()) {
                String line = "";
                Node source = entry.getKey();
                Map<Node, List<Path>> neighbors = entry.getValue();

                System.out.println("Node: " + source);
                line = "Node: " + source + "\n";
                for (Map.Entry<Node, List<Path>> neighborEntry : neighbors.entrySet()) {
                    Node destination = neighborEntry.getKey();
                    List<Path> paths = neighborEntry.getValue();

                    System.out.println("  Neighbor: " + destination);
                    line += "  Neighbor: " + destination + "\n";
                    for (Path path : paths) {
                        System.out.println("    Path: " + path.printPath());
                        line += "    Path: " + path.printPath() + "\n";
                    }

                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }

            }
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to file '" + filename + "'");
            e.printStackTrace();
        }
    }

}
