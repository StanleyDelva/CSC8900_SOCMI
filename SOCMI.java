// Stanley Delva
// CSC 8900: Network Science

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.text.ParseException;

public class SOCMI {

    public static void main(String args[]) {
        Graph graph = new Graph();
        try {
            graph.readFromFile("data/citeseer.lg");
        } catch (IOException e) {
            System.out.println("Error reading citeseer.lg");
            System.exit(1);
        }

        int minSupport = 50;
        int maxDistance = 350;

        long startTime = System.nanoTime();
        List<Graph> frequentPatterns = SOCMI_fpm(graph, minSupport, maxDistance);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // time in milliseconds

        System.out.println("Frequent Pathgraphs:  Took " + duration + " milliseconds to find " + frequentPatterns.size()
                + " frequent pathgraphs with min support " + minSupport + " and max distance " + maxDistance + ".");
        for (Graph pattern : frequentPatterns) {
            System.out.println(pattern.printGraph());
        }
    }

    // Algorithm 1
    public static List<Graph> SOCMI_fpm(Graph graph, int minSupport, int maxDistance) {
        List<Graph> result = new ArrayList<>();
        List<Pathgraph> candidate = new ArrayList<>();

        List<Edge> fEdges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().getLabel() != edge.getDestination().getLabel() && edge.getWeight() <= maxDistance) {
                fEdges.add(edge);
            }
        }

        for (Edge edge : fEdges) {
            Pathgraph pg_E = new Pathgraph(edge);

            List<Pathgraph> inCandidate = candidate.stream().filter(pg -> pg.samePattern(pg_E))
                    .collect(Collectors.toList());

            if (inCandidate.isEmpty() == false) {
                System.out.println("Contains edge in candidate");
                for (Pathgraph pg : inCandidate) {
                    pg.merge(pg_E);
                }
            } else {
                candidate.add(new Pathgraph(edge));
            }

        }

        while (fEdges.isEmpty() == false) {
            for (Edge edge : fEdges) {
                Graph p = new Graph(edge);
                Stack<Graph> S = new Stack<>();
                S.push(p);

                while (S.empty() == false) {
                    Graph ext = S.peek();
                    List<Edge> extEdges = ext.getEdges().stream().filter(e -> e.getWeight() >= maxDistance)
                            .collect(Collectors.toList());
                    if (extEdges.isEmpty() == false || new Pathgraph(ext).get_pCount() >= minSupport) {
                        System.out.println("distance threshold reached");
                        result.add(ext);
                        S.pop();
                        continue;
                    }

                    Graph p_ext = new Graph(p);
                    p_ext.extendGraph(ext);

                    Pathgraph p_ext_pathgraph = PathGraphExtension(candidate, p_ext, minSupport, maxDistance);

                    System.out.println(p_ext_pathgraph.get_pCount());
                    if (p_ext_pathgraph.get_pCount() >= minSupport) {
                        S.push(p_ext);
                    }
                }

                fEdges.remove(edge);
            }
        }

        return result;
    }

    // Algorithm 2
    private static Pathgraph PathGraphExtension(List<Pathgraph> candidate, Graph p_ext, int minSupport,
            int maxDistance) {

        Pathgraph pg_ext = new Pathgraph();

        List<Edge> pEdges = new ArrayList<>();
        pEdges.addAll(p_ext.getEdges());

        for (Edge edge : pEdges) {
            Pathgraph edgePg = new Pathgraph(edge);
            pg_ext.merge(edgePg);

            // List<Pathgraph> inCandidate = candidate.stream()
            // .filter(pg -> pg.samePattern(edgePg))
            // .collect(Collectors.toList());

            // if (inCandidate.isEmpty() == false) {
            // System.out.println("Contains edgePg in candidate");
            // inCandidate.get(0).merge(edgePg);
            // pg_ext.merge(edgePg);
            // continue;
            // }
        }

        return pg_ext;

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
        destination.addEdge(edge);

        if (this.adjList.get(source.getId()) == null) {
            this.adjList.put(source.getId(), new ArrayList<Integer>());
            this.adjList.get(source.getId()).add(destination.getId());
        } else {
            this.adjList.get(source.getId()).add(destination.getId());
        }
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
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
        for (Node node : nodes.values()) {
            text += node.getId() + " " + node.getLabel() + "\n";
        }
        for (Edge edge : edges) {
            text += edge.getSource().getId() + " " + edge.getDestination().getId() + " " + edge.getWeight() + "\n";
        }
        return text;
    }
}

class Pathgraph {
    // Implementation of Pathgraph, as interpreted from paper
    private Graph graph;
    private HashMap<Integer, Integer> pCount;

    public Pathgraph(Graph graph) {
        this.graph = graph;
        this.pCount = new HashMap<>();
        for (Node node : graph.getNodes()) {
            this.pCount.put(node.getLabel(), pCount.getOrDefault(node.getLabel(), 0) + 1);
        }
    }

    public Pathgraph(Edge edge) {
        this.graph = new Graph();
        this.graph.addEdge(edge);

        this.pCount = new HashMap<>();
        this.pCount.put(edge.getSource().getLabel(), 1);
        this.pCount.put(edge.getDestination().getLabel(), 1);

    }

    public Pathgraph() {
        this.graph = new Graph();
        this.pCount = new HashMap<>();
    }

    public void addEdge(Node source, Node destination) {
        this.graph.addEdge(source, destination, 1.0);
        if (this.graph.getNodes().contains(source) == false) {
            this.graph.addNode(source.getId(), source.getLabel());
            this.pCount.put(source.getLabel(), 1);
        }
        if (this.graph.getNodes().contains(destination) == false) {
            this.graph.addNode(destination.getId(), destination.getLabel());
            this.pCount.put(destination.getLabel(), 1);
        }

    }

    public void merge(Pathgraph pathgraph) {

        this.getGraph().getEdges().addAll(pathgraph.getGraph().getEdges());

        if (this.pCount.keySet().equals(pathgraph.pCount.keySet()) == false) {
            this.pCount.putAll(pathgraph.pCount);

            Graph temp = new Graph(this.graph);
            this.graph = temp;

        } else {
            for (Integer key : this.pCount.keySet()) {
                this.pCount.put(key, this.pCount.get(key) + pathgraph.pCount.get(key));
            }

            Graph temp = new Graph(this.graph);
            this.graph = temp;
        }

    }

    public boolean contains(Pathgraph pathgraph) {
        for (Edge edge : pathgraph.getGraph().getEdges()) {
            if (this.graph.getEdges().contains(edge) == false) {
                return false;
            }

            if (this.pCount.keySet().equals(pathgraph.pCount.keySet()) == false) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Edge edge) {
        return this.pCount.containsKey(edge.getSource().getLabel())
                && this.pCount.containsKey(edge.getDestination().getLabel());
    }

    public boolean samePattern(Pathgraph pathgraph) {
        return this.pCount.keySet().equals(pathgraph.pCount.keySet());
    }

    public boolean equals(Pathgraph pathgraph) {
        return this.pCount.keySet().equals(pathgraph.pCount.keySet());
    }

    public int get_pCount() {
        if (this.pCount.isEmpty()) {
            return 0;
        }
        return Collections.min(this.pCount.values());
    }

    public Graph getGraph() {
        return graph;
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public List<Node> getNodes() {
        return graph.getNodes();
    }

    public Graph getOriginalGraph() {
        return this.graph;
    }

}
