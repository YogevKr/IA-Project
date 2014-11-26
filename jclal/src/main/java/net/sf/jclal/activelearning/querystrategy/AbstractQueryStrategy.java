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
package net.sf.jclal.activelearning.querystrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.scenario.PoolBasedSamplingScenario;
import net.sf.jclal.core.IClassifier;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.core.IQueryStrategy;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.util.sort.Container;
import net.sf.jclal.util.sort.OrderUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Abstract class for active learning strategies. All AL query strategies must
 * extend this class.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 *
 */
public abstract class AbstractQueryStrategy implements IQueryStrategy,
        IConfigure {

    private static final long serialVersionUID = 1L;

    /**
     * Pointer to unlabeled data
     */
    private IDataset unlabelledData;

    /**
     * Pointer to labeled data
     */
    private IDataset labelledData;

    /**
     * The dataset used to test.
     */
    private IDataset testData;

    /**
     * Pointer to the classifier used
     */
    private IClassifier classifier;

    /**
     * Indicates whether the query strategy is maximum or minimal.By default is
     * maximal, i.e, it selects the k (batch) instances with the highest values.
     */
    private boolean maximal = true;

    /**
     * To store the evaluations for each iterations.
     */
    protected List<AbstractEvaluation> evaluations;

    /**
     * To store the indexes of the selected instances to remove from unlabeled
     * data
     */
    private ArrayList<Integer> selectedInstances;

    /**
     *Get the selected instances
     * 
     * @return Returns the indexes of the instances selected by the query
     * strategy.
     */
    public ArrayList<Integer> getSelectedInstances() {
        return selectedInstances;
    }

    /**
     * Sets the indexes of the selected instances.
     * 
     * @param selectedInstances the select instances
     *
     */
    public void setSelectedInstances(ArrayList<Integer> selectedInstances) {
        this.selectedInstances = selectedInstances;
    }

    /**
     * Empty (default) constructor.
     */
    public AbstractQueryStrategy() {

        super();

        evaluations = new ArrayList<AbstractEvaluation>();
        selectedInstances = new ArrayList<Integer>();
    }

    /**
     * Get the evaluations
     *
     * @return The evaluations.
     */
    @Override
    public List<AbstractEvaluation> getEvaluations() {
        return evaluations;
    }

    /**
     * Set the evaluations
     * 
     * @param evaluations The evaluations.
     */
    @Override
    public void setEvaluations(List<AbstractEvaluation> evaluations) {
        this.evaluations = evaluations;
    }

    /**
     * Get the classifier
     * 
     * @return Pointer to current the Classifier.
     */
    @Override
    public IClassifier getClassifier() {
        return classifier;
    }

    /**
     * Set the classifier to use on query strategy
     *
     * @param classifier The classifier to use
     */
    @Override
    public void setClassifier(IClassifier classifier) {

        try {
            this.classifier = classifier.makeCopy();
        } catch (Exception e) {

            Logger.getLogger(AbstractQueryStrategy.class.getName()).log(
                    Level.SEVERE, null, e);
        }

    }

    /**
     * Get the unlabeled data
     * 
     * @return Instances unlabeled.
     */
    @Override
    public IDataset getUnlabelledData() {
        return unlabelledData;
    }

    /**
     * Set the unlabeled data
     * @param unlabelledData Instances unlabeled
     */
    @Override
    public void setUnlabelledData(IDataset unlabelledData) {
        this.unlabelledData = unlabelledData;
    }

    /**
     * Get the labeled data
     *
     * @return Labeled data.
     */
    @Override
    public IDataset getLabelledData() {
        return labelledData;
    }

    /**
     *Set the labeled data.
     *
     * @param labelledData Labeled data.
     */
    @Override
    public void setLabelledData(IDataset labelledData) {
        this.labelledData = labelledData;
    }

    /**
     * Indicates whether the query strategy is maximal or minimal.
     *
     * @param max Set the flag indicating if the query strategy is maximal or not
     */
    @Override
    public void setMaximal(boolean max) {

        this.maximal = max;
    }

    /**
     * Gets whether the query strategy is maximal or minimal.
     *
     * @return Whether the query startegy is maximal or not 
     */
    @Override
    public boolean isMaximal() {
        return maximal;
    }

    /**
     * Train the classifier.
     */
    @Override
    public void training() {
        try {

            classifier.buildClassifier(getLabelledData());

        } catch (Exception ex) {
            Logger.getLogger(PoolBasedSamplingScenario.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

    }

    /**
     * Evaluates the classifier using the test dataset.
     */
    @Override
    public void testModel() {

        try {
            // test phase with the actual model
            AbstractEvaluation evaluation = classifier.testModel(testData);

            evaluation.setLabeledSetSize(getLabelledData().getNumInstances());

            evaluation.setUnlabeledSetSize(getUnlabelledData().getNumInstances());

            evaluations.add(evaluation);

        } catch (Exception e) {

            Logger.getLogger(AbstractQueryStrategy.class.getName()).log(
                    Level.SEVERE, null, e);
        }

    }

    /**
     * Function that returns the utility of each unlabeled instance.
     *
     * @return A array that stores the utility of each unlabeled instance
     */
    @Override
    public double[] testUnlabeledData() {

        // found values of all unlabeled instances
        double[] values = new double[getUnlabelledData().getNumInstances()];

        int pos = 0;

        Instances unlabeledInstances = getUnlabelledData().getDataset();

        for (Instance instance : unlabeledInstances) {
            values[pos++] = utilityInstance(instance);
        }

        return values;
    }

    /**
     * Set the test data.
     * @param testData The instances to prove the effectiveness of the model
     */
    @Override
    public void setTestData(IDataset testData) {
        this.testData = testData;
    }

    @Override
    /**
     * Get the test data.
     * @return The instances to prove the effectiveness of the model
     */
    public IDataset getTestData() {
        return testData;
    }

    /**
     * Returns the probability of the instance belongs to each class.
     *
     * @param instance The instance to test
     * @return The probability for each class
     */
    @Override
    public double[] distributionForInstance(Instance instance) {
        try {

            return classifier.distributionForInstance(instance);

        } catch (Exception ex) {
            Logger.getLogger(AbstractQueryStrategy.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @param configuration The configuration object for the Abstract query
     * strategy.
     *The XML labels supported are:
     * <ul>
     * <li>
     * <b>maximal= boolean</b>
     * </li>
     * <li>
     * <b>wrapper-classifier type= class</b>
     * <p>
     * Package: net.sf.jclal.classifier</p>
     * <p>
     * Class: All
     * </p>
     * </li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        // Set max iteration
        boolean maximalT = configuration.getBoolean("maximal", isMaximal());
        setMaximal(maximalT);

        String wrapperError = "wrapper-classifier type= ";
        try {
            // classifier classname
            String classifierClassname = configuration
                    .getString("wrapper-classifier[@type]");

            wrapperError += classifierClassname;
            // classifier class
            Class<? extends IClassifier> classifierClass = (Class<? extends IClassifier>) Class
                    .forName(classifierClassname);
            // classifier instance
            IClassifier classifierTemp = classifierClass.newInstance();
            // Configure classifier (if necessary)
            if (classifierTemp instanceof IConfigure) {
                ((IConfigure) classifierTemp).configure(configuration
                        .subset("wrapper-classifier"));
            }
            // Add this classifier to the query strategy
            setClassifier(classifierTemp);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "Illegal classifier classname: " + wrapperError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "Illegal classifier classname: " + wrapperError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "Illegal classifier classname: " + wrapperError, e);
        }
    }

    /**
     *
     * @return The simple name of a class.
     */
    @Override
    public String toString() {

        return this.getClass().getSimpleName();
    }

    /**
     * Updates the labeled data.
     */
    @Override
    public void updateLabeledData() {

        ArrayList<Container> ordered = new ArrayList<Container>();

        //Adds the instances to labeled set
        for (int index : selectedInstances) {
            labelledData.add(unlabelledData.instance(index));
            ordered.add(new Container(index, index));
        }

        //To order the array in descendent order
        OrderUtils.mergeSort(ordered, true);

        //Removes the instances from unlabeled set. The deleting operation must be in descendent order
        for (Container pairValue : ordered) {

            unlabelledData.remove(Integer.parseInt(pairValue.getValue().toString()));
        }

        //Clears the indexes of selected instances
        selectedInstances.clear();

    }

    @Override
    public void algorithmFinished() {
    }

}
