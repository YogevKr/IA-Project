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
package net.sf.jclal.activelearning.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.querystrategy.AbstractQueryStrategy;
import net.sf.jclal.core.IClassifier;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.core.IEvaluation;
import net.sf.jclal.core.IScenario;
import net.sf.jclal.core.IStopCriterion;
import net.sf.jclal.core.ISystem;
import net.sf.jclal.core.ITool;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * Class that represents a Classical Active Learning Algorithm.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class ClassicalALAlgorithm extends AbstractALAlgorithm implements ITool {

    private static final long serialVersionUID = 4075956096492062508L;

    /**
     * Max of iterations, by default it is equal to 50
     */
    private int maxIteration = 50;

    /**
     * Current iteration
     */
    private int iteration;

    /**
     * Active Learning scenario
     */
    private IScenario scenario;

    /**
     * Store the extra stop criterion
     */
    private List<IStopCriterion> stopCriterionList;

    /**
     * The evaluation of supervised learning having in mind the information of
     * training dataset and test dataset
     */
    private IEvaluation passiveLearningEvaluation;

    /**
     *
     * @return Returns the evaluation if there is applied supervised learning to
     * the present algorithm
     */
    public IEvaluation getPassiveLearningEvaluation() {

        if (passiveLearningEvaluation == null) {
            executePasiveLearning();
        }

        return passiveLearningEvaluation;
    }

    /**
     * Establishes the evaluation if there is applied supervised learning to the
     * present algorithm
     *
     * @param passiveLearningEvaluation The evaluation of the passive learning
     */
    public void setPassiveLearningEvaluation(IEvaluation passiveLearningEvaluation) {
        this.passiveLearningEvaluation = passiveLearningEvaluation;
    }

    /**
     *
     * @return The Scenario used
     */
    @Override
    public IScenario getScenario() {
        return scenario;
    }

    /**
     *
     * @param scenario The scenario to be used
     */
    @Override
    public void setScenario(IScenario scenario) {
        this.scenario = scenario;
    }

    // ///////////////////////////////////////////////////////////////
    // ------------------------------------------------- Constructors
    // ///////////////////////////////////////////////////////////////
    /**
     * Empty (default) constructor
     *
     */
    public ClassicalALAlgorithm() {

        super();

        this.stopCriterionList = new ArrayList<IStopCriterion>();

    }

    // ///////////////////////////////////////////////////////////////
    // -------------------------------------------- Protected methods
    // ///////////////////////////////////////////////////////////////
    // Execution methods
    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void doInit() {

        // Do Control
        doControl();
    }

    /**
     * Establishes the different elements contained in an iteration in the
     * ambience of active learning. In case of the classic algorithm of active
     * learning it includes the training of the model, his evaluation, the
     * selection of instances, the labeling of the same ones and the update of
     * the dataset
     */
    @Override
    protected void doIterate() {

        ++iteration;

        // Do training with base classifier over labeled instances
        doTraining();

        // Do evaluation with base classifier over unlabeled instances
        doEvaluationTest();

        // Do selection by scenario and query strategy
        doSelectionIntances();

        // Do label the selected instances
        doLabelInstances();

        // Do update the labeled set and unlabeled instances
        doUpdateLabelledData();

        // Do control
        doControl();

        //Run the GC
        System.gc();
    }

    /**
     * @return The current iteration
     */
    public int getIteration() {
        return iteration;
    }

    /**
     *
     * @param iteration The current iteration
     */
    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    /**
     * Label the selected instances by query strategy
     */
    private void doLabelInstances() {
        scenario.labelInstances();
    }

    /**
     * Executes the evaluation of the experiment
     */
    private void doEvaluationTest() {
        scenario.evaluationTest();
        scenario.getQueryStrategy().getEvaluations().get(iteration - 1).setIteration(iteration);
    }

    /**
     * Do update the training set and unlabeled instances
     */
    private void doUpdateLabelledData() {
        scenario.updateLabelledData();
    }

    /**
     * Selected instances by query strategy and Scenario
     */
    private void doSelectionIntances() {

        scenario.instancesSelection();

    }

    /**
     * Executes the training phase. Train the base classifier
     */
    private void doTraining() {
        scenario.training();
    }

    /**
     * Check if algorithm is finished
     *
     *By default the implementation of this method performs the following
     * operations:
     * <ul>
     * <li>If number of iterations exceeds the maximum allowed, then the
     * algorithm is stopped</li>
     * <li>If unlabeled set is empty, then the algorithm is stopped</li>
     * </ul>
     */
    protected void doControl() {
        // If maximum number of iterations is exceeded, the algorithm is
        // finished
        if (iteration >= maxIteration
                || ((AbstractQueryStrategy) (scenario.getQueryStrategy()))
                .getUnlabelledData().isEmpty()) {
            state = FINISHED;
            return;
        }

        //the extra stop criteria are verified
        for (IStopCriterion iStopCriterion : stopCriterionList) {
            if (iStopCriterion.stop(this)) {
                state = FINISHED;
                return;
            }
        }
    }

    /**
     *
     * @return The max iteration
     */
    public int getMaxIteration() {
        return maxIteration;
    }

    /**
     *
     * @param maxIteration Set the max iteration
     */
    public void setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
    }

    /**
     *
     * @param testDataSet The dataset to test
     */
    @Override
    public void setTestDataSet(IDataset testDataSet) {
        scenario.getQueryStrategy().setTestData(testDataSet);
    }

    /**
     *
     * @return The dataset used to test
     */
    @Override
    public IDataset getTestDataSet() {
        return scenario.getQueryStrategy().getTestData();
    }

    /**
     *
     * @param labeledDataSet The labeled dataset used
     */
    @Override
    public void setLabeledDataSet(IDataset labeledDataSet) {
        scenario.getQueryStrategy().setLabelledData(labeledDataSet);
    }

    /**
     *
     * @param unlabeledDataSet The unlabeled dataset used
     */
    @Override
    public void setUnlabeledDataSet(IDataset unlabeledDataSet) {
        scenario.getQueryStrategy().setUnlabelledData(unlabeledDataSet);
    }

    /**
     *
     * @return The labeled dataset used
     */
    @Override
    public IDataset getLabeledDataSet() {
        return scenario.getQueryStrategy().getLabelledData();
    }

    /**
     *
     * @return The unlabeled dataset used
     */
    @Override
    public IDataset getUnlabeledDataSet() {
        return scenario.getQueryStrategy().getUnlabelledData();
    }

    /**
     * @param configuration The configuration object for the classic algorithm
     *
     *The XML labels supported are:
     * <ul>
     * <li><p>
     * <b>max-iteration= int</b></p>
     * </li>
     * <li>
     * <p>
     * <b>scenario type= class.</b></p>
     * <p>
     * Package: net.sf.jclal.activelearning.scenario</p>
     * <p>
     * Class: All</p>
     * </li>
     * <li>
     * <p>
     * <b>stop-criterio type= class.</b>
     * <p>
     * Package: net.sf.jclal.activelearning.stopcriterion</p>
     * <p>
     * Class: All</p>
     * </li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        super.configure(configuration);

        // Set max iteration
        int maxIterationT = configuration.getInt("max-iteration", 50);
        setMaxIteration(maxIterationT);

        //Set the stop criterion configure
        setStopCriterionConfigure(configuration);

        //Set the scenario configuration
        setScenarioConfiguration(configuration);
    }

    /**
     * Establishes the configuration of the scenario
     * 
     * @param configuration The configuration object to use 
     */
    public void setScenarioConfiguration(Configuration configuration) {

        String scenarioError = "scenario type= ";
        // scenario
        try {
            // scenario classname
            String scenarioClassname = configuration
                    .getString("scenario[@type]");

            scenarioError += scenarioClassname;
            // scenario class
            Class<? extends IScenario> scenarioClass = (Class<? extends IScenario>) Class
                    .forName(scenarioClassname);
            // scenario instance
            IScenario scenario = scenarioClass.newInstance();
            // Configure scenario (if necessary)
            if (scenario instanceof IConfigure) {
                ((IConfigure) scenario).configure(configuration
                        .subset("scenario"));
            }
            // Add this scenario to the algorithm
            setScenario(scenario);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal scenario classname: " + scenarioError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal scenario classname: " + scenarioError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal scenario classname: " + scenarioError, e);
        }
    }

    /**
     * Establishes parameters for context in the algorithm
     *
     * @param context The context to use
     */
    @Override
    public void contextualize(ISystem context) {

        super.contextualize(context);

        // Attach a random generator to this object
        if (getScenario() instanceof ITool) {
            ((ITool) getScenario()).contextualize(context);
        }

        // Attach a random generator to this object
        if (getScenario().getQueryStrategy() instanceof ITool) {
            ((ITool) getScenario().getQueryStrategy()).contextualize(context);
        }

    }

    /**
     * It executes the passive learning, i.e, it trains the classifiers on the
     * whole training set and test the model on the test set
     *
     */
    public void executePasiveLearning() {

        try {

            IClassifier classifier = getScenario()
                    .getQueryStrategy().getClassifier().makeCopy();

            IDataset trainingDataset = getLabeledDataSet().copy();

            trainingDataset.addAll(getUnlabeledDataSet());

            classifier.buildClassifier(trainingDataset);

            this.passiveLearningEvaluation = classifier
                    .testModel(getTestDataSet());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.getLogger(ClassicalALAlgorithm.class.getName()).log(
                    Level.SEVERE, null, e);
        }
    }

    /**
     * Establishes the configuration of the stop criterion
     * @param configuration The configuration object to use
     */
    public void setStopCriterionConfigure(Configuration configuration) {

        // Number of defined stopCriterio
        int stopCriterioValue = configuration.getList("stop-criterion[@type]").size();
        // For each listener in list
        for (int i = 0; i < stopCriterioValue; i++) {
            String header = "stop-criterion(" + i + ")";
            //stopCriterio
            String stopError = "stop-criterion type= ";
            try {
                // stopCriterio classname
                String stopCriterioClassname = configuration
                        .getString(header + "[@type]");

                stopError += stopCriterioClassname;
                // stopCriterio class
                Class<? extends IStopCriterion> stopCriterioClass = (Class<? extends IStopCriterion>) Class
                        .forName(stopCriterioClassname);
                // stopCriterio instance
                IStopCriterion stopCriterio = stopCriterioClass.newInstance();
                // Configure stopCriterio (if necessary)
                if (stopCriterio instanceof IConfigure) {
                    ((IConfigure) stopCriterio).configure(configuration
                            .subset(header));
                }
                // Add this stopCriterio to the algorithm
                addStopCriterion(stopCriterio);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationRuntimeException(
                        "\nIllegal stopCriterion classname: " + stopError, e);
            } catch (InstantiationException e) {
                throw new ConfigurationRuntimeException(
                        "\nIllegal stopCriterion classname: " + stopError, e);
            } catch (IllegalAccessException e) {
                throw new ConfigurationRuntimeException(
                        "\nIllegal stopCriterion classname: " + stopError, e);
            }

        }

    }

    /**
     * @param stopCriterion The stop criterion to add
     */
    public void addStopCriterion(IStopCriterion stopCriterion) {
        this.stopCriterionList.add(stopCriterion);
    }

    /**
     * {@inheritDoc}
     *
     * @param stopCriterion The stop criterion to remove
     * @return If the criterion was successfully removed
     */
    public boolean removeStopCriterion(IStopCriterion stopCriterion) {
        return this.stopCriterionList.remove(stopCriterion);
    }

}
