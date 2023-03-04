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
package edu.pitt.dbmi.experiment.run;

import edu.cmu.tetrad.algcomparison.independence.ProbabilisticTest;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.Rfci;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.Params;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.experiment.run.util.DataSampling;
import edu.pitt.dbmi.experiment.run.util.Graphs;
import edu.pitt.dbmi.experiment.run.util.ResourceLoader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Feb 27, 2023 3:14:43 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class RfciBootstrappingApp {

    private static void run(Path dataFile, Path dirOut) throws IOException {
        DataSet dataSet = (DataSet) ResourceLoader.readInDataModel(dataFile, Delimiter.TAB);

        // bootstrap data sample
        long seed = 1673588774198L;
        int numberResampling = 100;
        boolean includeOriginalDataset = true;
        List<DataSet> dataSets = DataSampling.sampleWithReplacement(dataSet, seed, numberResampling, includeOriginalDataset);

        // algorithm parameters
        Parameters parameters = new Parameters();
        setRfciParameters(parameters);
        setProbabilisticTestParameters(parameters);

        List<Graph> graphs = runSearches(dataSets, parameters);
        Graph graph = Graphs.createGraphWithHighestProbabilityEdges(graphs);

        // write out details
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), "run_details.txt").toFile())) {
            writer.println("Algorithm");
            writer.println("================================================================================");
            writer.println("Algorithm: RFCI");
            writer.println("Test of Independence: Probabilistic Test");
            writer.println();

            writer.println("Parameters");
            writer.println("----------------------------------------");
            ParamDescriptions paramDescriptions = ParamDescriptions.getInstance();
            parameters.getParametersNames().forEach(paramName -> {
                String paramDesc = paramDescriptions.get(paramName).getShortDescription();
                String paramValue = String.valueOf(parameters.get(paramName));
                if (paramValue.equals("true")) {
                    paramValue = "Yes";
                } else if (paramValue.equals("false")) {
                    paramValue = "No";
                }

                writer.printf("%s: %s%n", paramDesc, paramValue);
            });
            writer.println();

            writer.println("Bootstrapping");
            writer.println("----------------------------------------");
            writer.printf("Seed: %d%n", seed);
            writer.printf("Number of Sampling: %d%n", numberResampling);
            writer.printf("Include Original Dataset: %s%n", includeOriginalDataset ? "Yes" : "No");
            writer.println();

            writer.println("Dataset");
            writer.println("----------------------------------------");
            writer.printf("Variables: %d%n", dataSet.getNumColumns());
            writer.printf("Cases: %d%n", dataSet.getNumRows());
            writer.printf("Data Samples: %d%n", dataSets.size());
            writer.println();

            writer.println("Graph Details");
            writer.println("----------------------------------------");
            writer.println(graph.toString().trim());
        }

        // write out graph
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), "graph.txt").toFile())) {
            writer.println(Graphs.removeNullEdgeType(graph).toString().trim());
        }
    }

    private static List<Graph> runSearches(List<DataSet> dataSets, Parameters parameters) {
        List<Graph> graphs = new LinkedList<>();

        for (DataSet dataSet : dataSets) {
            graphs.add(runSearch(dataSet, parameters));
        }

        return graphs;
    }

    private static Graph runSearch(DataSet dataSet, Parameters parameters) {
        Rfci search = new Rfci((new ProbabilisticTest()).getTest(dataSet, parameters));
        search.setDepth(parameters.getInt(Params.DEPTH));
        search.setMaxPathLength(parameters.getInt(Params.MAX_PATH_LENGTH));
        search.setVerbose(parameters.getBoolean(Params.VERBOSE));

        return search.search();
    }

    private static void setRfciParameters(Parameters parameters) {
        parameters.set(Params.MAX_PATH_LENGTH, -1);
        parameters.set(Params.DEPTH, -1);
        parameters.set(Params.TIME_LAG, 0);
        parameters.set(Params.VERBOSE, false);
    }

    private static void setProbabilisticTestParameters(Parameters parameters) {
        parameters.set(Params.CUTOFF_IND_TEST, 0.5);
        parameters.set(Params.PRIOR_EQUIVALENT_SAMPLE_SIZE, 10);
        parameters.set(Params.NO_RANDOMLY_DETERMINED_INDEPENDENCE, true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("Rfci Bootstrapping");
        System.out.println("================================================================================");
        try {
            run(Paths.get(args[0]), Paths.get(args[1]));
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        System.out.println("================================================================================");
    }

}