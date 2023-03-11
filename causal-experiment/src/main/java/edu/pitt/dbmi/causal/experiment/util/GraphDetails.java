/*
 * Copyright (C) 2023 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.causal.experiment.util;

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeTypeProbability;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.pitt.dbmi.causal.experiment.tetrad.Edges;
import static edu.pitt.dbmi.causal.experiment.tetrad.Edges.getReversed;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * Mar 10, 2023 2:55:33 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class GraphDetails {

    private GraphDetails() {
    }

    public static void saveDetails(Graph trueGraph, Graph graph, Path file) throws IOException {
        try (PrintStream writer = new PrintStream(file.toFile())) {
            writer.println(GraphUtils.graphNodesToText(graph,
                    "Graph Nodes:\n================================================================================",
                    ','));
            writer.println();
            writer.println();
            writer.println("Graph Edge Type Probabilities:");
            writer.println("================================================================================");
            writer.println();

            Set<Edge> edges = getUniqueEdges(Arrays.asList(trueGraph, graph));
            edges.forEach(edge -> {
                String node1 = edge.getNode1().getName();
                String node2 = edge.getNode2().getName();

                // header
                Edge trueEdge = trueGraph.getEdge(trueGraph.getNode(node1), trueGraph.getNode(node2));
                if (trueEdge == null) {
                    writer.printf("True: %s ... %s%n", node1, node2);
                } else {
                    writer.printf("True: %s%n", Edges.toString(trueEdge));
                    node1 = trueEdge.getNode1().toString();
                    node2 = trueEdge.getNode2().toString();
                }
                writer.println("--------------------");

                // body
                Edge predictedEdge = graph.getEdge(graph.getNode(node1), graph.getNode(node2));
                if (predictedEdge == null) {
                    writer.printf("%s ... %s: 1.000000%n", node1, node2);
                } else {
                    List<EdgeTypeProbability> etps = predictedEdge.getEdgeTypeProbabilities();
                    if (etps != null) {
                        for (EdgeTypeProbability etp : etps) {
                            String edgeType = (node1.equals(predictedEdge.getNode1().getName()))
                                    ? Edges.toString(etp.getEdgeType())
                                    : Edges.toString(getReversed(etp.getEdgeType()));
                            writer.printf("%s %s %s: %f%n",
                                    node1,
                                    edgeType,
                                    node2,
                                    etp.getProbability());
                        }
                    }
                }
                writer.println();
            });
        }
    }

    public static Set<Edge> getUniqueEdges(List<Graph> graphs) {
        Set<Edge> edges = new HashSet<>();

        Map<String, Node> nodes = new HashMap<>();
        graphs.forEach(graph -> {
            graph.getEdges().forEach(edge -> {
                String node1 = edge.getNode1().toString();
                String node2 = edge.getNode2().toString();

                Node n1 = nodes.get(node1);
                if (n1 == null) {
                    n1 = new DiscreteVariable(node1);
                    nodes.put(node1, n1);
                }
                Node n2 = nodes.get(node2);
                if (n2 == null) {
                    n2 = new DiscreteVariable(node2);
                    nodes.put(node2, n2);
                }

                edges.add(new Edge(n1, n2, Endpoint.TAIL, Endpoint.TAIL));
            });
        });

        return new TreeSet<>(edges);
    }

}
