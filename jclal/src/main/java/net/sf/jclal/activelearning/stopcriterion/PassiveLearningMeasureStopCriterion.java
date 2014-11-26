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
package net.sf.jclal.activelearning.stopcriterion;

import java.util.ArrayList;
import java.util.List;
import net.sf.jclal.activelearning.algorithm.ClassicalALAlgorithm;
import net.sf.jclal.core.IAlgorithm;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IEvaluation;
import net.sf.jclal.core.IStopCriterion;
import org.apache.commons.configuration.Configuration;

/**
 * Stop Criterion that it uses to supervised learning. The active learning
 * algorithm would stop on having reached the results of supervised learning
 * according to the definite measurements.
 *
 * @author Oscar Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class PassiveLearningMeasureStopCriterion implements IStopCriterion, IConfigure {

    /**
     * List of measures.
     */
    private List<String> measureNames;
    /**
     * List if the measurements are maximum or minimal
     */
    private List<Boolean> measureMaximales;
    /**
     * In the case of disjunctive form at least one expression must be true.
     */
    private boolean disjunctive = true;

    /**
     * In the case of disjunctive form at least one expression must be true.
     *
     * @return Flag that indicates if the measures are evaluated in disjunction form
     */
    public boolean isDisjunctive() {
        return disjunctive;
    }

    /**
     * In the case of disjunctive form at least one expression must be true.
     * 
     * @param disjunctive Flag that indicates if the measures are evaluated in disjunction form
     */
    public void setDisjunctive(boolean disjunctive) {
        this.disjunctive = disjunctive;
    }

    /**
     * Used evaluation measurements.
     *
     * @return A list of meaures
     */
    public List<String> getMeasureNames() {
        return measureNames;
    }

    /**
     * Used evaluation measurements.
     * 
     * @param measureNames The list of the measure names
     *
     */
    public void setMeasureNames(List<String> measureNames) {
        this.measureNames = measureNames;
    }

    /**
     * Establishes if the evaluation measurements are maximum or minimal.
     *
     * @return The list of the maximal measures
     */
    public List<Boolean> getMeasureMaximales() {
        return measureMaximales;
    }

    /**
     * Establishes if the evaluation measurements are maximum or minimal.
     *
     *@param measureMaximales The list of maximal measures
     */
    public void setMeasureMaximales(List<Boolean> measureMaximales) {
        this.measureMaximales = measureMaximales;
    }

    /**
     * Default constructor.
     */
    public PassiveLearningMeasureStopCriterion() {
        measureNames = new ArrayList<String>();
        measureMaximales = new ArrayList<Boolean>();

    }

    /**
     * @param algorithm The algorithm to evaluate
     * @return true if is reached a stop condition, false otherwise
     */
    @Override
    public boolean stop(IAlgorithm algorithm) {

        int last = algorithm.getScenario().getQueryStrategy().getEvaluations().size();

        if (last == 0) {
            return false;
        }

        IEvaluation eval = algorithm.getScenario().getQueryStrategy().getEvaluations().get(last - 1);

        IEvaluation supervised = ((ClassicalALAlgorithm) algorithm).getPassiveLearningEvaluation();

        for (int i = 0; i < measureNames.size(); i++) {

            double actEval = eval.getMetricValue(measureNames.get(i));

            double supEval = supervised.getMetricValue(measureNames.get(i));

            if (disjunctive) {
                //In the case of disjunctive form at least one expression must be true
                if ((measureMaximales.get(i) && actEval >= supEval) || (!measureMaximales.get(i) && actEval <= supEval)) {
                    return true;
                }
            } else {
                //In the case of conjunctive form all the expressions must be true
                if ((measureMaximales.get(i) && actEval < supEval) || (!measureMaximales.get(i) && actEval > supEval)) {
                    return false;
                }

            }
        }

        return !disjunctive;
    }

    /**
     * The measures that are actually recognize for single label data are
     * the following:
     * <ul>
     * <li>Correctly Classified Instances</li>
     * <li>Incorrectly Classified Instances</li>
     * <li>Kappa statistic</li>
     * <li>Mean absolute error</li>
     * <li>Root mean squared error</li>
     * <li>Relative absolute error</li>
     * <li>Root relative squared error</li>
     * <li>Coverage of cases</li>
     * <li>Mean region size</li>
     * <li>Weighted Precision</li>
     * <li>Weighted Recall</li>
     * <li>Weighted FMeasure</li>
     * <li>Weighted TruePositiveRate</li>
     * <li>Weighted FalsePositiveRate</li>
     * <li>Weighted MatthewsCorrelation</li>
     * <li>Weighted AreaUnderROC</li>
     * <li>Weighted AreaUnderPRC</li>
     * </ul>
     * <br>
     * The measures that are actually recognize for multi label data are the
     * following:
     * <ul>
     * <li>Hamming Loss</li>
     * <li>Subset Accuracy</li>
     * <li>Example-Based Precision</li>
     * <li>Example-Based Recall</li>
     * <li>Example-Based F Measure</li>
     * <li>Example-Based Accuracy</li>
     * <li>Example-Based Specificity</li>
     * <li>Micro-averaged Precision</li>
     * <li>Micro-averaged Recall</li>
     * <li>Micro-averaged F-Measure</li>
     * <li>Micro-averaged Specificity</li>
     * <li>Macro-averaged Precision</li>
     * <li>Macro-averaged Recall</li>
     * <li>Macro-averaged F-Measure</li>
     * <li>Macro-averaged Specificity</li>
     * <li>Average Precision</li>
     * <li>Coverage</li>
     * <li>OneError</li>
     * <li>IsError</li>
     * <li>ErrorSetSize</li>
     * <li>Ranking Loss</li>
     * <li>Mean Average Precision</li>
     * <li>Geometric Mean Average Precision</li>
     * <li>Mean Average Interpolated Precision</li>
     * <li>Geometric Mean Average Interpolated Precision</li>
     * <li>Micro-averaged AUC</li>
     * <li>Macro-averaged AUC</li>
     * </ul>
     *
     * If more than one measure is settled then by default the set of
     * expressions are evaluated in disjunctive form.
     *
     *The XML labels supported are:
     *
     * <ul>
     * <li><b>disjunctive-form= boolean</b></li>
     * <li><b>measure= String</b>, attribute: maximal= boolean</li>
     * </ul>
     *
     * @param settings the object that stores the configuration
     */
    @Override
    public void configure(Configuration settings) {

        boolean disjunctiveForm = settings.getBoolean("disjunctive-form", disjunctive);

        setDisjunctive(disjunctiveForm);

        // Number of defined measures
        int numberMeasures = settings.getList("measure").size();

        // For each listener in list
        for (int i = 0; i < numberMeasures; i++) {
            String header = "measure(" + i + ")";

            // measure classname
            String measureNameT = settings.getString(header, "");
            boolean maximalT = settings.getBoolean(header + "[@maximal]");

            // Add this measure
            addMeasure(measureNameT, maximalT);
        }
    }

    /**
     * Adds an evaluation measurement and establishes if it is maximum or
     * minimal.
     *
     * @param measureName The measure name
     * @param maximal Flag that indicates if the measure is maximal or minimal
     */
    public void addMeasure(String measureName, boolean maximal) {
        measureNames.add(measureName);
        measureMaximales.add(maximal);
    }
}
