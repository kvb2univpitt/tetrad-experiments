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
package edu.pitt.dbmi.causal.experiment.tetrad;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.LayoutUtil;
import edu.cmu.tetradapp.workbench.GraphWorkbench;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 *
 * Mar 11, 2023 12:49:21 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class Graphs {

    private Graphs() {
    }

    public static void exportAsPngImage(Graph graph, int width, int height, Path output) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics graphics = image.getGraphics();

        graph = removeNullEdges(graph);

        int radius = (int) (width * 0.40);
        LayoutUtil.circleLayout(graph, width / 2, height / 2, radius);

        GraphWorkbench graphWorkbench = new GraphWorkbench(graph);
        graphWorkbench.setBounds(0, 0, width, height);
        graphWorkbench.paint(graphics);

        try {
            ImageIO.write(image, "png", output.toFile());
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public static void saveGraph(Graph graph, Path file) throws IOException {
        try (PrintStream writer = new PrintStream(file.toFile())) {
            writer.println(removeNullEdges(graph).toString().trim());
        }
    }

    private static Graph removeNullEdges(Graph graph) {
        Graph myGraph = new EdgeListGraph(graph.getNodes());
        graph.getEdges().stream()
                .filter(edge -> !(edge.getEndpoint1() == Endpoint.NULL || edge.getEndpoint2() == Endpoint.NULL))
                .forEach(myGraph::addEdge);

        return myGraph;
    }

}
