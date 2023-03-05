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
import edu.cmu.tetrad.graph.Edge.Property;
import edu.cmu.tetrad.graph.EdgeListGraph;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Mar 2, 2023 4:15:50 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class Graphs {

    private Graphs() {
    }

    public static Graph createGraphWithHighestProbabilityEdges(List<Graph> graphs) {
        // filter out null graphs
        graphs = graphs.stream()
                .filter(graph -> graph != null)
                .collect(Collectors.toList());

        if (graphs.isEmpty()) {
            return new EdgeListGraph();
        }

        // set edge properties for PAGs
        graphs = addPagColorings(graphs);

        // create new graph
        Graph graph = createNewGraph(graphs.get(0).getNodes());
        for (Edge edge : getAllEdgesAsUndirected(graphs)) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();

            List<EdgeTypeProbability> edgeTypeProbabilities = getEdgeTypeProbabilities(n1, n2, graphs);
            EdgeTypeProbability highestEdgeTypeProbability = getHighestEdgeTypeProbability(edgeTypeProbabilities);
            Edge highestEdge = createEdge(highestEdgeTypeProbability, n1, n2);
            if (highestEdge != null) {
                // copy over edgetype probabilities
                edgeTypeProbabilities.forEach(highestEdge::addEdgeTypeProbability);

                // copy over edgetype properties
                highestEdgeTypeProbability.getProperties().forEach(highestEdge::addProperty);

                graph.addEdge(highestEdge);
            }
        }

        setEdgeProbabilities(graph);

        return graph;
    }

    public static Graph removeNullEdgeType(Graph graph) {
        Graph myGraph = new EdgeListGraph(graph.getNodes());
        graph.getEdges().stream()
                .filter(edge -> !(edge.getEndpoint1() == Endpoint.NULL || edge.getEndpoint2() == Endpoint.NULL))
                .forEach(myGraph::addEdge);

        return myGraph;
    }

    private static void setEdgeProbabilities(Graph graph) {
        graph.getEdges().forEach(edge -> {
            double probability = edge.getEdgeTypeProbabilities().stream()
                    .filter(etp -> etp.getEdgeType() != EdgeType.nil)
                    .mapToDouble(EdgeTypeProbability::getProbability)
                    .sum();
            edge.setProbability(probability);
        });
    }

    private static Edge createEdge(EdgeTypeProbability edgeTypeProbability, Node n1, Node n2) {
        if (edgeTypeProbability == null) {
            return null;
        }

        switch (edgeTypeProbability.getEdgeType()) {
            case ta:
                return new Edge(n1, n2, Endpoint.TAIL, Endpoint.ARROW);
            case at:
                return new Edge(n1, n2, Endpoint.ARROW, Endpoint.TAIL);
            case ca:
                return new Edge(n1, n2, Endpoint.CIRCLE, Endpoint.ARROW);
            case ac:
                return new Edge(n1, n2, Endpoint.ARROW, Endpoint.CIRCLE);
            case cc:
                return new Edge(n1, n2, Endpoint.CIRCLE, Endpoint.CIRCLE);
            case aa:
                return new Edge(n1, n2, Endpoint.ARROW, Endpoint.ARROW);
            case tt:
                return new Edge(n1, n2, Endpoint.TAIL, Endpoint.TAIL);
            default:
                return new Edge(n1, n2, Endpoint.NULL, Endpoint.NULL);
        }
    }

    private static EdgeTypeProbability getHighestEdgeTypeProbability(List<EdgeTypeProbability> edgeTypeProbabilities) {
        if (edgeTypeProbabilities.isEmpty()) {
            return null;
        }

        // sort in descending order
        EdgeTypeProbability[] etps = edgeTypeProbabilities.stream().toArray(EdgeTypeProbability[]::new);
        Arrays.sort(etps, (etp1, etp2) -> {
            if (etp1.getProbability() > etp2.getProbability()) {
                return -1;
            } else if (etp1.getProbability() < etp2.getProbability()) {
                return 1;
            } else {
                return 0;
            }
        });

        return etps[0];
    }

    private static List<EdgeTypeProbability> getEdgeTypeProbabilities(Node node1, Node node2, List<Graph> graphs) {
        List<EdgeTypeProbability> edgeTypeProbabilities = new LinkedList<>();

        // frequency counts
        int nullEdgeCounts = 0;
        Map<EdgeType, Integer> edgeTypeCounts = new HashMap<>();
        Map<EdgeType, Edge> edgeTypeEdges = new HashMap<>();
        for (Graph graph : graphs) {
            Node graphNode1 = graph.getNode(node1.getName());
            Node graphNode2 = graph.getNode(node2.getName());
            Edge edge = graph.getEdge(graphNode1, graphNode2);
            if (edge == null) {
                nullEdgeCounts++;
            } else {
                EdgeType edgeType = getEdgeType(edge, graphNode1, graphNode2);

                edgeTypeEdges.put(edgeType, edge);

                Integer edgeCounts = edgeTypeCounts.get(edgeType);
                edgeCounts = (edgeCounts == null) ? 1 : edgeCounts + 1;
                edgeTypeCounts.put(edgeType, edgeCounts);
            }
        }

        // compute probabilities
        for (EdgeType edgeType : edgeTypeCounts.keySet()) {
            Edge edge = edgeTypeEdges.get(edgeType);
            List<Property> properties = edge.getProperties();
            double probability = (double) edgeTypeCounts.get(edgeType) / graphs.size();

            edgeTypeProbabilities.add(new EdgeTypeProbability(edgeType, properties, probability));
        }
        if ((nullEdgeCounts > 0) && (nullEdgeCounts < graphs.size())) {
            edgeTypeProbabilities.add(new EdgeTypeProbability(EdgeType.nil, (double) nullEdgeCounts / graphs.size()));
        }

        // sort in descending order
        EdgeTypeProbability[] etps = edgeTypeProbabilities.stream().toArray(EdgeTypeProbability[]::new);
        Arrays.sort(etps, (etp1, etp2) -> {
            if (etp1.getProbability() > etp2.getProbability()) {
                return -1;
            } else if (etp1.getProbability() < etp2.getProbability()) {
                return 1;
            } else {
                return 0;
            }
        });

        return Arrays.asList(etps);
    }

    private static EdgeType getEdgeType(Edge edge, Node node1, Node node2) {
        Endpoint endpoint1 = edge.getProximalEndpoint(node1);
        Endpoint endpoint2 = edge.getProximalEndpoint(node2);

        if (endpoint1 == Endpoint.TAIL && endpoint2 == Endpoint.ARROW) {
            return EdgeType.ta;
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.TAIL) {
            return EdgeType.at;
        } else if (endpoint1 == Endpoint.CIRCLE && endpoint2 == Endpoint.ARROW) {
            return EdgeType.ca;
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.CIRCLE) {
            return EdgeType.ac;
        } else if (endpoint1 == Endpoint.CIRCLE && endpoint2 == Endpoint.CIRCLE) {
            return EdgeType.cc;
        } else if (endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.ARROW) {
            return EdgeType.aa;
        } else if (endpoint1 == Endpoint.TAIL && endpoint2 == Endpoint.TAIL) {
            return EdgeType.tt;
        } else {
            return EdgeType.nil;
        }
    }

    /**
     * Collect all the edges in the graph as undirected edges.
     *
     * @param graphs list of graphs
     * @return set of undirected edges
     */
    private static Set<Edge> getAllEdgesAsUndirected(List<Graph> graphs) {
        Set<Edge> edges = new HashSet();
        graphs.forEach(graph -> {
            graph.getEdges().forEach(edge -> {
                edges.add(new Edge(edge.getNode1(), edge.getNode2(), Endpoint.NULL, Endpoint.NULL));
            });
        });

        return edges;
    }

    private static Graph createNewGraph(List<Node> graphNodes) {
        Node[] nodes = graphNodes.stream().toArray(Node[]::new);
        Arrays.sort(nodes);

        return new EdgeListGraph(Arrays.asList(nodes));
    }

    private static List<Graph> addPagColorings(List<Graph> graphs) {
        if (graphs == null) {
            return Collections.EMPTY_LIST;
        }

        return graphs.stream()
                .filter(graph -> graph != null)
                .map(graph -> {
                    GraphUtils.addPagColoring(graph);
                    return graph;
                })
                .collect(Collectors.toList());
    }

}
