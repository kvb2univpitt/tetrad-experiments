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
package edu.pitt.dbmi.causal.experiment.util;

/**
 *
 * Feb 27, 2023 3:31:53 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class DataFiles {

    public static final String SIM_DISC_20VAR_1KCASE = DataFiles.class
            .getResource("/data/discrete_20var_1kcase/simulation/data/data.1.txt").getFile();

    public static final String SIM_DISC_20VAR_1KCASE_2 = DataFiles.class
            .getResource("/data/discrete_20var_1kcase_2/simulation/data/data.1.txt").getFile();

    private DataFiles() {
    }

}
