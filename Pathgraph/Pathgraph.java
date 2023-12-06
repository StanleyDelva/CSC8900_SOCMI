package Pathgraph;

import java.util.*;
import java.util.stream.Collectors;
import Graph.*;

public class Pathgraph {
    // Implementation of Pathgraph, as interpreted from paper
    private Graph graph;
    private HashMap<Integer, Integer> pCount; // count of each node label in the pathgraph
    private ArrayList<Integer> path; // list of node labels in the pathgraph, to get idea of pattern
    private HashMap<Integer, HashMap<Integer, Double>> reachableNodes; // key is node id, value is map of reachable
                                                                       // nodes and their distance

    public Pathgraph(Graph graph) {
        this.graph = graph;
        this.pCount = new HashMap<>();
        this.path = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            this.pCount.put(node.getLabel(), pCount.getOrDefault(node.getLabel(), 0) + 1);
            if (!this.path.contains(node.getLabel())) {
                this.path.add(node.getLabel());
            }

        }
    }

    public Pathgraph(Edge edge) {
        this.graph = new Graph();
        this.graph.addEdge(edge);

        if (edge.getSource().getLabel() != edge.getDestination().getLabel()) {
            this.pCount = new HashMap<>();
            this.pCount.put(edge.getSource().getLabel(), 1);
            this.pCount.put(edge.getDestination().getLabel(), 1);

            this.path = new ArrayList<>();
            this.path.add(edge.getSource().getLabel());
            this.path.add(edge.getDestination().getLabel());
        } else {
            this.pCount = new HashMap<>();
            this.pCount.put(edge.getSource().getLabel(), 2);
        }
    }

    public Pathgraph(Edge edge, int maxDistance) {
        this.graph = new Graph();
        this.graph.addEdge(edge);
        this.pCount = new HashMap<>();
        this.pCount.put(edge.getSource().getLabel(), this.pCount.getOrDefault(edge.getSource().getLabel(), 0) + 1);
        this.pCount.put(edge.getDestination().getLabel(),
                this.pCount.getOrDefault(edge.getDestination().getLabel(), 0) + 1);

        this.reachableNodes = new HashMap<>();
        this.path = new ArrayList<>();
        this.path.add(edge.getSource().getLabel());
        this.path.add(edge.getDestination().getLabel());

        for (Edge e : this.graph.getEdges()) {
            double dist = e.getWeight();
            for (Edge neighbor : e.getDestination().getEdges()) {
                if (neighbor.getDestination().getLabel() != e.getSource().getLabel()
                        && neighbor.getDestination().getLabel() != e.getDestination().getLabel()
                        && neighbor.getWeight() <= maxDistance) {

                    dist += neighbor.getWeight();
                    this.reachableNodes.putIfAbsent(e.getSource().getId(), new HashMap<>());
                    this.reachableNodes.get(e.getSource().getId()).put(neighbor.getDestination().getId(), dist);

                } else {
                    dist += neighbor.getWeight();
                }
            }
        }
    }

    public Pathgraph() {
        this.graph = new Graph();
        this.pCount = new HashMap<>();
        this.path = new ArrayList<>();
        this.reachableNodes = new HashMap<>();
    }

    public void addEdge(Node source, Node destination, double weight) {
        this.graph.addEdge(source, destination, 1.0);
        if (this.graph.getNodes().contains(source) == false) {
            this.graph.addNode(source.getId(), source.getLabel());
            this.pCount.put(source.getLabel(), pCount.getOrDefault(source.getLabel(), 0) + 1);
        }
        if (this.graph.getNodes().contains(destination) == false) {
            this.graph.addNode(destination.getId(), destination.getLabel());
            this.pCount.put(destination.getLabel(), pCount.getOrDefault(destination.getLabel(), 0) + 1);
        }

    }

    public void addEdge(Edge edge) {
        this.graph.addEdge(edge);

        if (this.graph.getNodes().contains(edge.getSource()) == false) {
            this.graph.addNode(edge.getSource().getId(), edge.getSource().getLabel());
            this.pCount.put(edge.getSource().getLabel(), pCount.get(edge.getSource().getLabel()) + 1);
        }
        if (this.graph.getNodes().contains(edge.getDestination()) == false) {
            this.graph.addNode(edge.getDestination().getId(), edge.getDestination().getLabel());
            this.pCount.put(edge.getDestination().getLabel(), pCount.get(edge.getDestination().getLabel()) + 1);
        }
    }

    public void removeEdge(Edge edge) {
        this.graph.getEdges().remove(edge);
        this.pCount.remove(edge.getSource().getLabel());
        this.pCount.remove(edge.getDestination().getLabel());
    }

    public void merge(Pathgraph pathgraph, int maxDistance) {
        this.pCount = new HashMap<>();
        for (int key : pathgraph.pCount.keySet()) {
            this.pCount.put(key, pathgraph.pCount.getOrDefault(this.pCount.get(key), 0) + 1);
        }

        if (!this.path.equals(pathgraph.getPath())) {

            for (Edge edge : pathgraph.getGraph().getEdges()) {
                // Find the nodes that are different between the two pathgraphs but have same
                // label
                if (this.graph.getNodes().contains(edge.getSource())
                        || this.graph.getNodes().contains(edge.getDestination())) {
                    if (this.graph.getNodes().contains(edge.getSource())) {
                        List<Edge> sourceIsBetween = this.graph.getEdges().stream()
                                .filter(e -> e.getDestination().getId() == edge.getSource().getId())
                                .collect(Collectors.toList());
                        if (!sourceIsBetween.isEmpty()) {
                            for (Edge e : sourceIsBetween) {
                                addEdge(e.getSource(), edge.getDestination(), e.getWeight() + edge.getWeight());
                            }
                        } else {
                            addEdge(edge);
                        }

                    }

                    else if (this.graph.getNodes().contains(edge.getDestination())) {
                        List<Edge> DestIsDest = this.graph.getEdges().stream()
                                .filter(e -> e.getDestination().getId() == edge.getDestination().getId())
                                .collect(Collectors.toList());
                        if (!DestIsDest.isEmpty()) {
                            for (Edge e : DestIsDest) {
                                addEdge(e.getSource(), edge.getDestination(), e.getWeight() + edge.getWeight());
                            }
                        }

                        List<Edge> DestIsSource = this.graph.getEdges().stream()
                                .filter(e -> e.getSource().getLabel() == edge.getDestination().getLabel())
                                .collect(Collectors.toList());
                    }

                } else if (!this.graph.getNodes().contains(edge.getSource())
                        && !this.graph.getNodes().contains(edge.getDestination())) {
                    return;
                }

                else {

                }
            }

            this.path.addAll(pathgraph.getPath());
            for (Integer key : pathgraph.pCount.keySet()) {
                this.pCount.put(key, this.pCount.getOrDefault(key, 0) + pathgraph.pCount.get(key));
            }

            this.path.removeIf(n -> Collections.frequency(this.path, n) > 1);
        } else {
            this.getGraph().getEdges().addAll(pathgraph.getGraph().getEdges());

            for (Integer key : this.pCount.keySet()) {
                this.pCount.put(key, this.pCount.get(key) + pathgraph.pCount.get(key));
            }
        }

        Graph temp = new Graph(this.graph);
        this.graph = temp;

        //
        List<Edge> edgesToRemove = this.getGraph().getEdges().stream()
                .filter(e -> e.getSource().getLabel() == path.get(0)).collect(Collectors.toList());

    }

    public void merge(Pathgraph pg) {
        if (pg.getPath().equals(this.path)) {
            this.graph.getEdges().addAll(pg.getGraph().getEdges());

            for (Integer key : pg.pCount.keySet()) {
                this.pCount.put(key, this.pCount.get(key) + pg.pCount.get(key));
            }

            Graph temp = new Graph(this.graph);
            this.graph = temp;

            return;
        }

        for (int label : pg.path) {
            if (!this.path.contains(label)) {
                this.path.add(label);
            }
        }

        for (Integer key : pg.pCount.keySet()) {
            this.pCount.put(key, this.pCount.getOrDefault(key, 0) + pg.pCount.get(key));
        }
        for (Node n : pg.getGraph().getNodes()) {
            if (!this.graph.getNodes().contains(n)) {
                this.reachableNodes.put(n.getId(), pg.reachableNodes.get(n.getId()));
            } else {
                this.reachableNodes.get(n.getId()).putAll(pg.reachableNodes.get(n.getId()));
            }
        }

        for (Edge e : this.getGraph().getEdges()) {
            if (this.reachableNodes.get(e.getDestination().getId()) != null) {

            }
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

    public ArrayList<Integer> getPath() {
        return path;
    }

    public Graph getGraph() {
        return this.graph;
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public List<Node> getNodes() {
        return graph.getNodes();
    }

}
