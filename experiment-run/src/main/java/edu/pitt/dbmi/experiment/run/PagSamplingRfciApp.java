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
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.Rfci;
import edu.cmu.tetrad.search.SearchGraphUtils;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.Params;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.experiment.run.util.GraphPrintUtil;
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
 * Mar 4, 2023 9:41:02 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class PagSamplingRfciApp {

    private static void run(Path dataFile, Path dirOut) throws IOException {
        DataModel dataModel = ResourceLoader.readInDataModel(dataFile, Delimiter.TAB);

        // algorithm parameters
        Parameters parameters = new Parameters();
        setPagSamplingRficParameters(parameters);
        setRfciParameters(parameters);
        setProbabilisticTestParameters(parameters);

        int numOfSearchRuns = 0;
        List<Graph> graphs = new LinkedList<>();
        int numRandomizedSearchModels = parameters.getInt(Params.NUM_RANDOMIZED_SEARCH_MODELS);
        while (graphs.size() < numRandomizedSearchModels) {
            Graph graph = runSearch(dataModel, parameters);
            if (SearchGraphUtils.isLegalPag(graph).isLegalPag()) {
                graphs.add(graph);
            }
            numOfSearchRuns++;
        }

        Graph graph = Graphs.createGraphWithHighestProbabilityEdges(graphs);

        // write out details
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), "run_details.txt").toFile())) {
            writer.println("Algorithm");
            writer.println("================================================================================");
            writer.println("Algorithm: PAG Sampling RFCI");
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

            writer.println("Dataset");
            writer.println("----------------------------------------");
            writer.printf("Variables: %d%n", ((DataSet) dataModel).getNumColumns());
            writer.printf("Cases: %d%n", ((DataSet) dataModel).getNumRows());
            writer.println();

            writer.println("Search Run Details");
            writer.println("----------------------------------------");
            writer.printf("Number of Runs: %d%n", numOfSearchRuns);
            writer.printf("Number of Valid PAGs: %d%n", graphs.size());
            writer.printf("Number of Invalid PAGs: %d%n", numOfSearchRuns - graphs.size());
            writer.println();

            writer.println("Graph (null edges included)");
            writer.println("----------------------------------------");
            writer.println(graph.toString().trim());
        }

        // write out graph
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), "graph_details.txt").toFile())) {
            GraphPrintUtil.printDetails(graph, writer);
        }

        // write out graph
        try (PrintStream writer = new PrintStream(Paths.get(dirOut.toString(), "graph.txt").toFile())) {
            writer.println(Graphs.removeNullEdgeType(graph).toString().trim());
        }
    }

    private static List<Graph> runSearches(DataModel dataModel, Parameters parameters) {
        List<Graph> graphs = new LinkedList<>();
        int numRandomizedSearchModels = parameters.getInt(Params.NUM_RANDOMIZED_SEARCH_MODELS);
        while (graphs.size() < numRandomizedSearchModels) {
            Graph graph = runSearch(dataModel, parameters);
            if (SearchGraphUtils.isLegalPag(graph).isLegalPag()) {
                graphs.add(graph);
            }
        }

        return graphs;
    }

    private static Graph runSearch(DataModel dataModel, Parameters parameters) {
        Rfci rfci = new Rfci((new ProbabilisticTest()).getTest(dataModel, parameters));
        rfci.setDepth(parameters.getInt(Params.DEPTH));
        rfci.setMaxPathLength(parameters.getInt(Params.MAX_PATH_LENGTH));
        rfci.setVerbose(parameters.getBoolean(Params.VERBOSE));

        return rfci.search();
    }

    private static void setRfciParameters(Parameters parameters) {
        int maxPathLength = -1;
        int depth = -1;

        parameters.set(Params.MAX_PATH_LENGTH, maxPathLength);
        parameters.set(Params.DEPTH, depth);
    }

    private static void setProbabilisticTestParameters(Parameters parameters) {
        double cutoffIndTest = 0.5;
        double priorEquivalentSampleSize = 100;
        boolean noRandomlyDeterminedIndependence = false;

        parameters.set(Params.CUTOFF_IND_TEST, cutoffIndTest);
        parameters.set(Params.PRIOR_EQUIVALENT_SAMPLE_SIZE, priorEquivalentSampleSize);
        parameters.set(Params.NO_RANDOMLY_DETERMINED_INDEPENDENCE, noRandomlyDeterminedIndependence);
    }

    private static void setPagSamplingRficParameters(Parameters parameters) {
        int numRandomizedSearchModels = 100;
        boolean verbose = false;

        parameters.set(Params.NUM_RANDOMIZED_SEARCH_MODELS, numRandomizedSearchModels);
        parameters.set(Params.VERBOSE, verbose);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("PAG Sampling RFCI");
        System.out.println("================================================================================");
        try {
            run(Paths.get(args[0]), Paths.get(args[1]));
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        System.out.println("================================================================================");
    }

}
