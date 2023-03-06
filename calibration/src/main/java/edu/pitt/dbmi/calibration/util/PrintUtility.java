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

import edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.aa;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ac;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.at;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ca;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.cc;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.ta;
import static edu.cmu.tetrad.graph.EdgeTypeProbability.EdgeType.tt;
import edu.pitt.dbmi.calibration.EdgeValue;
import java.io.PrintStream;
import java.util.Set;

/**
 *
 * Feb 16, 2023 7:43:43 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class PrintUtility {

    private PrintUtility() {
    }

    public static void displayCSV(Set<EdgeValue> edgeValues, PrintStream writer) {
        edgeValues.forEach(edgeValue -> writer.println(displayCSV(edgeValue)));
    }

    private static String displayCSV(EdgeValue edgeValue) {
        String node1 = edgeValue.getNode1();
        String node2 = edgeValue.getNode2();
        double probability = edgeValue.getPredictedValue();
        int observedValue = edgeValue.getObservedValue();

        if (probability > 0) {
            switch (edgeValue.getEdgeType()) {
                case aa:
                    return String.format("%s <-> %s,%f,%d", node1, node2, probability, observedValue);
                case ac:
                    return String.format("%s <-o %s,%f,%d", node1, node2, probability, observedValue);
                case at:
                    return String.format("%s <-- %s,%f,%d", node1, node2, probability, observedValue);
                case ca:
                    return String.format("%s o-> %s,%f,%d", node1, node2, probability, observedValue);
                case cc:
                    return String.format("%s o-o %s,%f,%d", node1, node2, probability, observedValue);
                case ta:
                    return String.format("%s --> %s,%f,%d", node1, node2, probability, observedValue);
                case tt:
                    return String.format("%s --- %s,%f,%d", node1, node2, probability, observedValue);
                default:
                    return String.format("no edge,%f,%d", probability, observedValue);
            }
        } else {
            switch (edgeValue.getEdgeType()) {
                case aa:
                    return String.format("%s <-> %s,0,%d", node1, node2, observedValue);
                case ac:
                    return String.format("%s <-o %s,0,%d", node1, node2, observedValue);
                case at:
                    return String.format("%s <-- %s,0,%d", node1, node2, observedValue);
                case ca:
                    return String.format("%s o-> %s,0,%d", node1, node2, observedValue);
                case cc:
                    return String.format("%s o-o %s,0,%d", node1, node2, observedValue);
                case ta:
                    return String.format("%s --> %s,0,%d", node1, node2, observedValue);
                case tt:
                    return String.format("%s --- %s,0,%d", node1, node2, observedValue);
                default:
                    return String.format("no edge,0,%d", observedValue);
            }
        }
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
            default:
                return "no edge";
        }
    }

}
