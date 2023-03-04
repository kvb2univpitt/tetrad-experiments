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

import edu.cmu.tetrad.data.DataSet;
import edu.pitt.dbmi.data.reader.Delimiter;
import edu.pitt.dbmi.experiment.run.util.DataSampling;
import edu.pitt.dbmi.experiment.run.util.ResourceLoader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Create dataset for bootstrapping.
 *
 * Feb 27, 2023 9:28:18 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class BootstrapSampleDataApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Path dataFile = Paths.get(args[0]);
        Path dataDir = Paths.get(args[1]);
        System.out.println("================================================================================");
        System.out.println("Bootstrapping Data Sampling");
        System.out.println("================================================================================");
        try {
            DataSet dataSet = (DataSet) ResourceLoader.readInDataModel(dataFile, Delimiter.TAB);
            long seed = 1673588774198L;
            int numberResampling = 99;
            List<DataSet> dataSets = DataSampling.sampleWithReplacement(dataSet, seed, numberResampling, true);
            writeOut(dataSets, dataDir);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
        System.out.println("================================================================================");
    }

    private static void writeOut(List<DataSet> dataSets, Path dataDir) throws IOException {
        int count = 0;
        for (DataSet dataSet : dataSets) {
            String fileName = String.format("data_%03d.txt", ++count);
            Path dataFile = Paths.get(dataDir.toString(), fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardOpenOption.CREATE)) {
                writer.write(dataSet.toString());
            }
        }
    }

}
