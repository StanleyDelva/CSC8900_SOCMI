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
            candidate.add(pg_E);
        }

        while (fEdges.isEmpty() == false) {
            for (Edge edge : fEdges) {
                Graph p = new Graph(edge);
                Stack<Graph> S = new Stack<>();
                S.push(p);

                while (!S.empty()) {
                    Graph ext = S.peek();
                    // System.out.println("EXTENDING: " + ext.printGraph());
                    List<Edge> extEdges = ext.getEdges().get(0).getDestination().getEdges().stream()
                            .filter(e -> e.getDestination().getLabel() != ext.getEdges().get(0).getSource().getLabel())
                            .filter(e -> e.getDestination().getLabel() != ext.getEdges().get(0).getDestination()
                                    .getLabel())
                            .collect(Collectors.toList());

                    System.out.println("Current pattern: " + ext.printGraph());
                    System.out.println("MAX DISTANCE: " + ext.getMaxDistance());
                    // if pattern is fully extended or cannot be extended further, add to result
                    if (new Pathgraph(ext).get_pCount() >= minSupport || ext.getMaxDistance() >= maxDistance
                            || extEdges.isEmpty()) {
                        System.out.println("Pattern added to result list");
                        result.add(S.pop());
                        continue;
                    }

                    Graph p_ext = new Graph(ext);

                    Pathgraph p_ext_pathgraph = PathGraphExtension(candidate, p_ext, minSupport, maxDistance);

                    System.out.println(p_ext_pathgraph.get_pCount());
                    if (p_ext_pathgraph.get_pCount() >= minSupport) {
                        S.push(p_ext_pathgraph.getGraph());
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
