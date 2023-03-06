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
package edu.pitt.dbmi.calibration;

import java.nio.file.Paths;

/**
 *
 * Mar 4, 2023 2:42:21 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class TestData {

    public static final String PAG_SAMPLING_GRAPH = Paths
            .get("src", "test", "resources", "data", "graph", "experiments", "pag_sampling_rfci", "graph_pag_sampling_rfci.txt").toString();

    public static final String RFCI_BOOTSTRAPPING_GRAPH = Paths
            .get("src", "test", "resources", "data", "graph", "experiments", "rfci_bootstrapping", "graph_rfci_bootstrapping.txt").toString();

    public static final String TRUE_GRAPH = Paths
            .get("src", "test", "resources", "data", "graph", "true_graphs", "true_graph.txt").toString();

    public static final String TRUE_PAG_FROM_DAG_GRAPH = Paths
            .get("src", "test", "resources", "data", "graph", "true_graphs", "true_pag_from_dag.txt").toString();

    public static final String OUTPUT_DIR = Paths
            .get("src", "test", "resources", "results").toString();

    private TestData() {
    }

}
