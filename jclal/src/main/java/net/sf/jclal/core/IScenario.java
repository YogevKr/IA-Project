/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jclal.core;

/**
 * Interface for Active Learning Scenarios.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Maria del Carmen Rodriguez Hernandez
 * @author Eduardo Perez Perdomo
 *
 */
public interface IScenario extends JCLAL {

    /**
     * Do selection by scenario and query strategy.
     */
    public void instancesSelection();

    /**
     *
     * @return Query strategy.
     */
    public IQueryStrategy getQueryStrategy();

    /**
     *
     * @param queryStrategy Query strategy.
     */
    public void setQueryStrategy(IQueryStrategy queryStrategy);

    /**
     * Label instances.
     */
    public void labelInstances();

    /**
     * Update labeled data.
     */
    public void updateLabelledData();

    /**
     * Evaluation test.
     */
    public void evaluationTest();

    /**
     * It establishes the training phase.
     */
    public void training();

    /**
     *
     * @return The representation of the scenario like a string.
     */
    @Override
    public String toString();

    /**
     * The oracle used, it allows to define as the chosen instances will be
     * labeled.
     * 
     * @return The Oralce to use.
     */
    public IOracle getOracle();

    /**
     * The oracle used, it allows to define as the chosen instances will be
     * labeled.
     * 
     * @param oracle The oracle to use
     */
    public void setOracle(IOracle oracle);

    /**
     * The batch mode strategy used, it allows to define wich of the analyzed
     * instances will be selected, Ej: the best instances.
     * 
     * @return The batch mode used
     */
    public IBatchMode getBatchMode();

    /**
     * The batch mode strategy used, it allows to define wich of the analyzed
     * instances will be selected, Ej: the best instances.
     * 
     * @param batchMode The bacth mode to use
     */
    public void setBatchMode(IBatchMode batchMode);

    /**
     * It is used after to complete the algorithm, executing any necessary
     * action.
     */
    public void algorithmFinished();
}
