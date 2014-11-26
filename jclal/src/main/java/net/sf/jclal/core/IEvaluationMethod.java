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

import java.util.List;
import net.sf.jclal.activelearning.algorithm.AbstractALAlgorithm;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;

/**
 * Evaluation method used to analyze the performance of the experiment.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public interface IEvaluationMethod {

    /**
     * Evaluate the experiment.
     */
    public void evaluate();

    /**
     *
     * @return The algorithm of the experiment.
     */
    public IAlgorithm getAlgorithm();

    /**
     *
     * @param algorithm The algorithm of the experiment.
     */
    public void setAlgorithm(AbstractALAlgorithm algorithm);

    /**
     *
     * @return The final evaluations.
     */
    public List<AbstractEvaluation> getFinalEvaluations();

    /**
     * Single-label single-instance learning.
     */
    public final int SINGLE_LABEL = 0;
    /**
     * multi-label single-instance learning.
     */
    public final int MULTI_LABEL = 1;
    /**
     * Single-label multi-instance learning.
     */
    public final int MULTI_INSTANCE = 2;
    /**
     * Multi-label multi-instance learning.
     */
    public final int MULTI_INSTANCE_MULTI_LABEL = 3;
}
