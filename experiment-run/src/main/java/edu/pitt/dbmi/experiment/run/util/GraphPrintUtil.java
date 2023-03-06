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
package edu.pitt.dbmi.experiment.run.util;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeTypeProbability;
import edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.aa;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ac;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.at;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ca;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.cc;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ta;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.tt;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * Mar 5, 2023 9:42:41 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class GraphPrintUtil {

    private GraphPrintUtil() {
    }

    public static String toString(Endpoint endpoint1, Endpoint endpoint2) {
        if (endpoint1 == Endpoint.TAIL && endpoint2 == Endpoint.ARROW) {
            return "-->";
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.TAIL) {
            return "<--";
        } else if (endpoint1 == Endpoint.CIRCLE && endpoint2 == Endpoint.ARROW) {
            return "o->";
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.CIRCLE) {
            return "<-o";
        } else if (endpoint1 == Endpoint.CIRCLE && endpoint2 == Endpoint.CIRCLE) {
            return "o-o";
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.ARROW) {
            return "<->";
        } else if (endpoint1 == Endpoint.TAIL && endpoint2 == Endpoint.TAIL) {
            return "---";
        } else if (endpoint1 == Endpoint.NULL && endpoint2 == Endpoint.NULL) {
            return " - ";
        } else {
            return "   ";
        }
    }

    public static String toString(Edge edge) {
        String node1 = edge.getNode1().getName();
        String node2 = edge.getNode2().getName();
        Endpoint endpoint1 = edge.getEndpoint1();
        Endpoint endpoint2 = edge.getEndpoint2();

        return String.format("%s %s %s", node1, toString(endpoint1, endpoint2), node2);
    }

    public static String toString(EdgeType edgeType) {
        switch (edgeType) {
            case aa:
                return "<->";
            case ac:
                return "<-o";
            case at:
                return "<--";
            case ca:
                return "o->";
            case cc:
                return "o-o";
            case ta:
                return "-->";
            case tt:
                return "---";
            case nil:
                return " - ";
            default:
                return "   ";
        }
    }

    public static void printDetails(Graph graph, PrintStream writer) {
        writer.println(GraphUtils.graphNodesToText(graph,
                "Graph Nodes:\n================================================================================",
                ','));
        writer.println();
        writer.println();
        writer.println("Graph Edge Type Probabilities:");
        writer.println("================================================================================");
        writer.println();

        Node[] nodes = graph.getNodes().stream().toArray(Node[]::new);
        getEdgeNodeIndexes(graph, nodes).forEach((node1Index, setOfNode2Indexes) -> {
            Node node1 = nodes[node1Index];
            setOfNode2Indexes.forEach(node2Index -> {
                Node node2 = nodes[node2Index];

                Edge edge = graph.getEdge(node1, node2);
                writer.printf("Edge: %s, %s%n", node1, node2);
                List<EdgeTypeProbability> etps = edge.getEdgeTypeProbabilities();
                if (etps != null) {
                    writer.println("--------------------");
                    etps.forEach(etp -> {
                        writer.printf("%s %s %s: %f%n",
                                node1,
                                toString(etp.getEdgeType()),
                                node2,
                                etp.getProbability());
                    });
                }
                writer.println();
            });
        });
    }

    private static Map<Integer, Set<Integer>> getEdgeNodeIndexes(Graph graph, Node[] nodes) {
        Map<Integer, Set<Integer>> edgeNodes = new TreeMap<>();

        Map<Node, Integer> nodeIndexes = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) {
            nodeIndexes.put(nodes[i], i);
        }

        graph.getEdges().forEach(edge -> {
            Integer node1 = nodeIndexes.get(edge.getNode1());
            Integer node2 = nodeIndexes.get(edge.getNode2());

            Set<Integer> setOfNodes = edgeNodes.get(node1);
            if (setOfNodes == null) {
                setOfNodes = new TreeSet<>();
                edgeNodes.put(node1, setOfNodes);
            }

            setOfNodes.add(node2);
        });

        return edgeNodes;
    }

}
