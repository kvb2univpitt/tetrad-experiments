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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.apache.commons.math3.random.Well44497b;

/**
 *
 * Feb 27, 2023 9:51:31 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class DataSampling {

    private DataSampling() {
    }

    private static RandomGenerator createRandomGenerator(long seed) {
        return (seed < 0)
                ? new SynchronizedRandomGenerator(new Well44497b(System.nanoTime()))
                : new SynchronizedRandomGenerator(new Well44497b(seed));
    }

    public static List<DataSet> sampleWithReplacement(DataSet dataSet, long seed, int numberResampling, boolean includeOriginalDataset) {
        List<DataSet> dataSets = new LinkedList<>();

        RandomGenerator randomGenerator = createRandomGenerator(seed);
        int sampleSize = dataSet.getNumRows();
        for (int i = 0; i < numberResampling; i++) {
            dataSets.add(DataUtils.getBootstrapSample(dataSet, sampleSize, randomGenerator));
        }

        if (includeOriginalDataset) {
            dataSets.add(dataSet);
        }

        return dataSets;
    }

}
