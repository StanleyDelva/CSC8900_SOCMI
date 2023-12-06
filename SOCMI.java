// Stanley Delva
// CSC 8900: Network Science

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.text.ParseException;
import Graph.*;
import Pathgraph.*;

public class SOCMI {

    public static void main(String args[]) {

        // Read citeseer.lg and create graph
        Graph graph = new Graph();
        try {
            graph.readFromFile("data/citeseer.lg");
        } catch (IOException e) {
            System.out.println("Error reading citeseer.lg");
            System.exit(1);
        }

        // Set min support and max distance
        int minSupport = 50;
        int maxDistance = 350;

        // Run SOCMI algorithm and time it
        long startTime = System.nanoTime();
        List<Graph> frequentPatterns = SOCMI_fpm(graph, minSupport, maxDistance);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // time in milliseconds

        // Print results
        for (Graph pattern : frequentPatterns) {
            System.out.println("FREQUENT PATTERN: \n" + pattern.printGraph());
        }
        System.out.println("Frequent Pathgraphs:  Took " + duration + " milliseconds to find " + frequentPatterns.size()
                + " frequent pathgraphs with min support " + minSupport + " and max distance " + maxDistance + ".");
    }

    // Algorithm 1: Frequent Pattern Mining
    public static List<Graph> SOCMI_fpm(Graph graph, int minSupport, int maxDistance) {
        // Create list of frequent patterns and candidate patterns
        List<Graph> result = new ArrayList<>();
        List<Pathgraph> candidate = new ArrayList<>();

        // find frequent edges of graph
        List<Edge> fEdges = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().getLabel() != edge.getDestination().getLabel() && edge.getWeight() <= maxDistance) {
                fEdges.add(edge);
            }
        }

        // add frequent edges to candidate patterns
        for (Edge edge : fEdges) {
            Pathgraph pg_E = new Pathgraph(edge);
            candidate.add(pg_E);
        }

        // extend candidate patterns
        while (fEdges.isEmpty() == false) {
            for (int i = 0; i < fEdges.size(); i++) {
                // create pattern from edge and push onto stack
                Graph p = new Graph(fEdges.get(i));
                Stack<Graph> S = new Stack<>();
                S.push(p);

                while (!S.empty()) {

                    // Determine if pattern can be extended further or not
                    Graph ext = S.peek();

                    // filter through edges of last node in pattern --- right-most expansion
                    List<Edge> extEdges = ext.getEdges().get(ext.getEdges().size() - 1).getDestination().getEdges()
                            .stream()
                            .filter(e -> e.getDestination().getLabel() != ext.getEdges().get(0).getSource().getLabel())
                            .filter(e -> e.getDestination().getLabel() != ext.getEdges().get(0).getDestination()
                                    .getLabel())
                            .filter(e -> ext.getEdges().contains(e) == false)
                            .collect(Collectors.toList());
                    System.out.println("Current Pattern: \n" + ext.printGraph() + "\n");
                    System.out.println("EXT EDGES SIZE: " + extEdges.size());
                    for (Edge e : extEdges) {
                        System.out.println("EXT EDGE: \n" + e.getSource().getId() + "( " +
                                e.getSource().getLabel() + ")  " + e.getDestination().getId() + " ( "
                                + e.getDestination().getLabel()
                                + ")" + e.getWeight() + "\n");
                    }
                    // System.out.println("Current pattern: " + ext.printGraph());
                    // System.out.println("MAX DISTANCE: " + ext.getMaxDistance());
                    // if pattern is fully extended or cannot be extended further, add to result
                    if (ext.getMaxDistance() >= maxDistance
                            || extEdges.isEmpty()) {
                        System.out.println("Pattern added to result list");
                        result.add(S.pop());
                        continue;
                    }

                    // if pattern can be extended, extend it
                    Graph p_ext = new Graph(ext);
                    p_ext.addEdge(extEdges.get(0));

                    Pathgraph p_ext_pathgraph = PathGraphExtension(candidate, p_ext, minSupport, maxDistance);

                    // if pathgraph of p' is frequent, push p' onto stack
                    System.out.println("Support calculation: " + p_ext_pathgraph.get_pCount());
                    if (p_ext_pathgraph.get_pCount() >= minSupport) {
                        S.push(p_ext_pathgraph.getGraph());
                    }
                }

                fEdges.remove(i);
            }
        }

        return result;
    }

    // Algorithm 2
    private static Pathgraph PathGraphExtension(List<Pathgraph> candidate, Graph p_ext, int minSupport,
            int maxDistance) {

        // create empty pathgraph for p'
        Pathgraph pg_ext = new Pathgraph();

        // for each edge in p', create pathgraph and merge with p'
        List<Edge> pEdges = new ArrayList<>();
        pEdges.addAll(p_ext.getEdges());

        for (Edge edge : pEdges) {
            Pathgraph edgePg = new Pathgraph(edge, maxDistance);
            pg_ext.merge(edgePg, maxDistance);

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
