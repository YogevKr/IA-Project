/*
 * Copyright (C) 2014 oscar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jclal.activelearning.oracle;

import net.sf.jclal.core.IQueryStrategy;

/**
 * Class that representes an oracle in simulated manner.
 *
 * The class of the selected instances are known previously to the AL process,
 * ie. the class of the unlabeled instances are hidden.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 *
 */
public class SimulatedOracle extends AbstractOracle {

    /**
     * Do nothing due to the label of the selected instances are known. The
     * labels are only reveal.
     *
     * @param queryStrategy The uery strategy to use.
     */
    @Override
    public void labelInstances(IQueryStrategy queryStrategy) {

        //do nothing due to the label of the selected instances are known. The labels are only reveal.
    }

}
