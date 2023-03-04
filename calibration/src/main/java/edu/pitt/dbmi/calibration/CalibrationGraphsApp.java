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

import edu.cmu.tetrad.graph.Graph;
import edu.pitt.dbmi.calibration.util.GraphCalibration;
import edu.pitt.dbmi.calibration.util.PrintUtility;
import edu.pitt.dbmi.calibration.util.ResourceLoader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 *
 * Mar 4, 2023 2:22:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class CalibrationGraphsApp {

    private static void runCalibrationTestEdges(Path trueGraphFile, Path searchGraphFile, Path dirOut) throws IOException {
        Graph trueGraph = ResourceLoader.loadGraph(trueGraphFile);
        Graph searchGraph = ResourceLoader.loadGraph(searchGraphFile);

        Set<EdgeValue> edgeValues = GraphCalibration.examineDirectEdge(searchGraph, trueGraph);
        String outFileName = searchGraphFile.getFileName().toString().replaceAll(".txt", ".csv");
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), outFileName).toFile())) {
            PrintUtility.displayCSV(edgeValues, writer);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("Calibration Graphs Test");
        System.out.println("================================================================================");
        try {
            runCalibrationTestEdges(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2]));
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        System.out.println("================================================================================");
    }

}
