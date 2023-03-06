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
package edu.pitt.dbmi.calibration.util;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeTypeProbability;
import edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ac;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.pitt.dbmi.calibration.EdgeValue;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Feb 16, 2023 7:39:49 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class GraphCalibration {

    private GraphCalibration() {
    }

    public static Set<EdgeValue> examineDirectEdge(Graph searchGraph, Graph trueGraph) {
        Set<EdgeValue> edgeValues = createEdgeValues(trueGraph, EdgeType.ta);
        setObservedValues(trueGraph, edgeValues);
        setPredictedValues(searchGraph, edgeValues);

        return edgeValues;
    }

    private static void setPredictedValues(Graph searchGraph, Set<EdgeValue> edgeValues) {
        for (EdgeValue edgeValue : edgeValues) {
            Node node1 = searchGraph.getNode(edgeValue.getNode1());
            Node node2 = searchGraph.getNode(edgeValue.getNode2());
            Edge predictedEdge = searchGraph.getEdge(node1, node2);
            if (predictedEdge != null) {
                List<EdgeTypeProbability> edgeTypeProbs = predictedEdge.getEdgeTypeProbabilities();
                if (edgeTypeProbs != null) {
                    for (EdgeTypeProbability edgeTypeProb : edgeTypeProbs) {
                        if (node1 == predictedEdge.getNode1() && node2 == predictedEdge.getNode2()) {
                            if (edgeValue.getEdgeType() == edgeTypeProb.getEdgeType()) {
                                edgeValue.setPredictedValue(edgeTypeProb.getProbability());
                            }
                        } else if (node1 == predictedEdge.getNode2() && node2 == predictedEdge.getNode1()) {
                            if (edgeValue.getEdgeType() == getReversed(edgeTypeProb.getEdgeType())) {
                                edgeValue.setPredictedValue(edgeTypeProb.getProbability());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setObservedValues(Graph trueGraph, Set<EdgeValue> edgeValues) {
        for (EdgeValue edgeValue : edgeValues) {
            Node node1 = trueGraph.getNode(edgeValue.getNode1());
            Node node2 = trueGraph.getNode(edgeValue.getNode2());
            Edge trueEdge = trueGraph.getEdge(node1, node2);
            if (trueEdge != null) {
                if (edgeValue.getEdgeType() == getEdgeType(trueEdge, node1, node2)) {
                    edgeValue.setObservedValue(1);
                }
            }
        }
    }

    private static EdgeType getReversed(EdgeType edgeType) {
        switch (edgeType) {
            case ac:
                return EdgeType.ca;
            case at:
                return EdgeType.ta;
            case ca:
                return EdgeType.ac;
            case ta:
                return EdgeType.at;
            default:
                return edgeType;
        }
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

    private static Set<EdgeValue> createEdgeValues(Graph graph, EdgeType edgeType) {
        Set<EdgeValue> edgeValues = new LinkedHashSet<>();

        boolean isAsymmetric = !(edgeType == EdgeType.aa
                || edgeType == EdgeType.cc
                || edgeType == EdgeType.tt
                || edgeType == EdgeType.nil);

        Map<String, List<String>> variableCombinations = new LinkedHashMap<>();
        String[] nodeNames = graph.getNodeNames().stream().toArray(String[]::new);
        for (int i = 0; i < nodeNames.length - 1; i++) {
            for (int j = i + 1; j < nodeNames.length; j++) {
                String node1 = nodeNames[i];
                String node2 = nodeNames[j];
                List<String> variables = variableCombinations.get(node1);
                if (variables == null) {
                    variables = new LinkedList<>();
                    variableCombinations.put(node1, variables);
                }
                variables.add(node2);

                if (isAsymmetric) {
                    variables = variableCombinations.get(node2);
                    if (variables == null) {
                        variables = new LinkedList<>();
                        variableCombinations.put(node2, variables);
                    }
                    variables.add(node1);
                }
            }
        }

        variableCombinations.forEach((node1, variables) -> {
            variables.forEach(node2 -> edgeValues.add(new EdgeValue(node1, node2, edgeType)));
        });

        return edgeValues;
    }

}
